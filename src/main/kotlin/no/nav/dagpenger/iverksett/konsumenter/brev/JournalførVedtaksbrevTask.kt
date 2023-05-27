package no.nav.dagpenger.iverksett.konsumenter.brev

import no.nav.dagpenger.iverksett.api.IverksettingRepository
import no.nav.dagpenger.iverksett.api.domene.Iverksett
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.repository.findByIdOrThrow
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.JournalpostResultat
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.tilIdType
import no.nav.dagpenger.iverksett.konsumenter.opprettNesteTask
import no.nav.dagpenger.kontrakter.felles.BrukerIdType
import no.nav.dagpenger.kontrakter.felles.Tema
import no.nav.dagpenger.kontrakter.iverksett.journalføring.Bruker
import no.nav.dagpenger.kontrakter.iverksett.journalføring.JournalposterForBrukerRequest
import no.nav.dagpenger.kontrakter.iverksett.journalføring.dokarkiv.ArkiverDokumentRequest
import no.nav.dagpenger.kontrakter.iverksett.journalføring.dokarkiv.AvsenderMottaker
import no.nav.dagpenger.kontrakter.iverksett.journalføring.dokarkiv.Dokument
import no.nav.dagpenger.kontrakter.iverksett.journalføring.dokarkiv.Filtype
import no.nav.familie.http.client.RessursException
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = JournalførVedtaksbrevTask.TYPE,
    maxAntallFeil = 5,
    triggerTidVedFeilISekunder = 15,
    beskrivelse = "Journalfører vedtaksbrev.",
)
class JournalførVedtaksbrevTask(
    private val iverksettingRepository: IverksettingRepository,
    private val journalpostClient: JournalpostClient,
    private val taskService: TaskService,
    private val iverksettResultatService: IverksettResultatService,
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId)

        journalførVedtaksbrev(behandlingId, iverksett)

        validerJournalpostResultatErSatt(behandlingId, iverksett.data)
    }

    private fun journalførOgLagreResultat(
        behandlingId: UUID,
        arkiverDokumentRequest: ArkiverDokumentRequest,
        mottakerIdent: String,
        beslutterId: String,
    ) {
        val journalpostId = try {
            journalpostClient.arkiverDokument(arkiverDokumentRequest, beslutterId).journalpostId
        } catch (e: RessursException) {
            if (e.cause is HttpClientErrorException.Conflict) {
                val eksternReferanseId = arkiverDokumentRequest.eksternReferanseId
                logger.warn(
                    "Konflikt ved arkivering av dokument, " +
                        "prøver å hente journalpostId med eksternReferanseId=$eksternReferanseId",
                )
                finnJournalpostIdForEksternReferanseId(mottakerIdent, eksternReferanseId)
            } else {
                throw e
            }
        }
        iverksettResultatService.oppdaterJournalpostResultat(
            behandlingId = behandlingId,
            mottakerIdent = mottakerIdent,
            JournalpostResultat(journalpostId = journalpostId),
        )
    }

    private fun finnJournalpostIdForEksternReferanseId(
        mottakerIdent: String,
        eksternReferanseId: String?,
    ): String {
        val request = JournalposterForBrukerRequest(Bruker(mottakerIdent, BrukerIdType.FNR), 50, listOf(Tema.DP))
        return journalpostClient.finnJournalposter(request)
            .find { it.eksternReferanseId == eksternReferanseId }
            ?.journalpostId
            ?: error("Finner ikke journalpost med eksternReferanseId=$eksternReferanseId")
    }

    private fun journalførVedtaksbrev(
        behandlingId: UUID,
        iverksett: Iverksett,
    ) {
        val vedtaksbrev = iverksett.brev ?: error("Fant ikke brev for behandlingId : $behandlingId")
        val dokument = Dokument(
            vedtaksbrev.pdf,
            Filtype.PDFA,
            dokumenttype = vedtaksbrevForStønadType(iverksett.data.fagsak.stønadstype),
            tittel = lagDokumentTittel(iverksett.data),
        )

        val arkiverDokumentRequest = ArkiverDokumentRequest(
            fnr = iverksett.data.søker.personIdent,
            forsøkFerdigstill = true,
            hoveddokumentvarianter = listOf(dokument),
            fagsakId = iverksett.data.fagsak.fagsakId.toString(),
            journalførendeEnhet = iverksett.data.søker.tilhørendeEnhet,
            eksternReferanseId = "$behandlingId-vedtaksbrev",
        )

        val journalførteIdenter: List<String> =
            iverksettResultatService.hentJournalpostResultat(behandlingId)?.keys?.toList() ?: emptyList()

        if (iverksett.data.vedtak.brevmottakere?.mottakere?.isEmpty() == true) {
            journalførVedtaksbrevTilStønadmottaker(arkiverDokumentRequest, iverksett.data, behandlingId)
        } else {
            journalførVedtaksbrevTilBrevmottakere(iverksett.data, journalførteIdenter, arkiverDokumentRequest, behandlingId)
        }
    }

    private fun journalførVedtaksbrevTilBrevmottakere(
        iverksett: IverksettDagpenger,
        journalførteIdenter: List<String>,
        arkiverDokumentRequest: ArkiverDokumentRequest,
        behandlingId: UUID,
    ) {
        iverksett.vedtak.brevmottakere?.mottakere?.mapIndexed { indeks, mottaker ->
            if (!journalførteIdenter.contains(mottaker.ident)) {
                val arkiverDokumentRequestForMottaker = arkiverDokumentRequest.copy(
                    eksternReferanseId = "$behandlingId-vedtaksbrev-mottaker$indeks",
                    avsenderMottaker = AvsenderMottaker(
                        id = mottaker.ident,
                        idType = mottaker.identType.tilIdType(),
                        navn = mottaker.navn,
                    ),
                )

                journalførOgLagreResultat(
                    behandlingId = behandlingId,
                    arkiverDokumentRequest = arkiverDokumentRequestForMottaker,
                    mottakerIdent = mottaker.ident,
                    beslutterId = iverksett.vedtak.beslutterId,
                )
            }
        }
    }

    private fun journalførVedtaksbrevTilStønadmottaker(
        arkiverDokumentRequest: ArkiverDokumentRequest,
        iverksett: IverksettDagpenger,
        behandlingId: UUID,
    ) {
        journalførOgLagreResultat(
            behandlingId = behandlingId,
            arkiverDokumentRequest = arkiverDokumentRequest,
            mottakerIdent = iverksett.søker.personIdent,
            beslutterId = iverksett.vedtak.beslutterId,
        )
    }

    private fun validerJournalpostResultatErSatt(behandlingId: UUID, iverksett: IverksettDagpenger) {
        val antallJournalføringer = iverksettResultatService.hentJournalpostResultat(behandlingId)?.size
            ?: error("Ingen journalføringer for behandling=[$behandlingId]")

        val satteBrevmottakere = iverksett.vedtak.brevmottakere?.mottakere?.size ?: 0

        // Hvis ingen brevmottakere er satt skal bare stønadsmottaker ha vedtaksbrevet
        val forventetJournalføringer = if (satteBrevmottakere > 0) satteBrevmottakere else 1

        if (forventetJournalføringer != antallJournalføringer) {
            error(
                "Feil ved journalføring av vedtaksbrev. Forventet $forventetJournalføringer journalføringsreultat, " +
                    "fant $antallJournalføringer.",
            )
        }
    }

    private fun lagDokumentTittel(iverksett: IverksettDagpenger): String =
        lagVedtakstekst(iverksett) + lagStønadtypeTekst(iverksett.fagsak.stønadstype)

    override fun onCompletion(task: Task) {
        taskService.save(task.opprettNesteTask())
    }

    companion object {

        const val TYPE = "journalførVedtaksbrev"
    }
}
