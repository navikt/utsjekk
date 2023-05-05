package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.api.domene.Brev
import no.nav.dagpenger.iverksett.api.domene.Iverksett
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.OppdragResultat
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.infrastruktur.util.tilFagsystem
import no.nav.dagpenger.iverksett.konsumenter.brev.JournalførVedtaksbrevTask
import no.nav.dagpenger.iverksett.konsumenter.hovedflyt
import no.nav.dagpenger.iverksett.konsumenter.oppgave.OpprettOppfølgingsOppgaveForDagpengerTask
import no.nav.dagpenger.iverksett.konsumenter.publiseringsflyt
import no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk.VedtakstatistikkTask
import no.nav.dagpenger.iverksett.konsumenter.økonomi.OppdragClient
import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import no.nav.dagpenger.iverksett.kontrakter.felles.Vedtaksresultat
import no.nav.dagpenger.iverksett.kontrakter.iverksett.IverksettStatus
import no.nav.dagpenger.iverksett.kontrakter.oppdrag.OppdragId
import no.nav.dagpenger.iverksett.kontrakter.oppdrag.OppdragStatus
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.error.TaskExceptionUtenStackTrace
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Properties
import java.util.UUID

@Service
class IverksettingService(
    val taskService: TaskService,
    val oppdragClient: OppdragClient,
    val iverksettingRepository: IverksettingRepository,
    val iverksettResultatService: IverksettResultatService,
    val featureToggleService: FeatureToggleService,
) {

    @Transactional
    fun startIverksetting(iverksett: IverksettDagpenger, brev: Brev?) {
        if (featureToggleService.isEnabled("dp.iverksett.stopp-iverksetting")) {
            error("Kan ikke iverksette akkurat nå")
        }
        iverksettingRepository.insert(
            Iverksett(
                iverksett.behandling.behandlingId,
                iverksett,
                iverksett.behandling.eksternId,
                brev,
            ),
        )

        iverksettResultatService.opprettTomtResultat(iverksett.behandling.behandlingId)

        taskService.save(
            Task(
                type = førsteHovedflytTask(iverksett),
                payload = iverksett.behandling.behandlingId.toString(),
                properties = Properties().apply {
                    this["personIdent"] = iverksett.søker.personIdent
                    this["behandlingId"] = iverksett.behandling.behandlingId.toString()
                    this["saksbehandler"] = iverksett.vedtak.saksbehandlerId
                    this["beslutter"] = iverksett.vedtak.beslutterId
                },
            ),
        )
    }

    private fun førstePubliseringsflytTask(iverksett: IverksettDagpenger) = when {
        iverksett.erGOmregning() || iverksett.erSatsendring() -> VedtakstatistikkTask.TYPE
        erIverksettingUtenVedtaksperioder(iverksett) -> OpprettOppfølgingsOppgaveForDagpengerTask.TYPE
        else -> publiseringsflyt().first().type
    }

    private fun førsteHovedflytTask(iverksett: IverksettDagpenger) = when {
        erIverksettingUtenVedtaksperioder(iverksett) -> JournalførVedtaksbrevTask.TYPE
        else -> hovedflyt().first().type
    }

    private fun erIverksettingUtenVedtaksperioder(iverksett: IverksettDagpenger) =
        iverksett.vedtak.tilkjentYtelse == null && iverksett.vedtak.vedtaksresultat == Vedtaksresultat.AVSLÅTT

    fun utledStatus(behandlingId: UUID): IverksettStatus? {
        val iverksettResultat = iverksettResultatService.hentIverksettResultat(behandlingId)
        return iverksettResultat?.let {
            if (it.vedtaksbrevResultat.isNotEmpty()) {
                return IverksettStatus.OK
            }
            if (it.journalpostResultat.isNotEmpty()) {
                return IverksettStatus.JOURNALFØRT
            }
            it.oppdragResultat?.let { oppdragResultat ->
                return when (oppdragResultat.oppdragStatus) {
                    OppdragStatus.KVITTERT_OK -> IverksettStatus.OK_MOT_OPPDRAG
                    OppdragStatus.LAGT_PAA_KOE -> IverksettStatus.SENDT_TIL_OPPDRAG
                    else -> IverksettStatus.FEILET_MOT_OPPDRAG
                }
            }
            it.tilkjentYtelseForUtbetaling?.let {
                if (it.utbetalingsoppdrag?.utbetalingsperiode?.isEmpty() == true) {
                    return IverksettStatus.OK_MOT_OPPDRAG
                }
                return IverksettStatus.SENDT_TIL_OPPDRAG
            }
            return IverksettStatus.IKKE_PÅBEGYNT
        }
    }

    fun sjekkStatusPåIverksettOgOppdaterTilstand(
        stønadstype: StønadType,
        personIdent: String,
        eksternBehandlingId: Long,
        behandlingId: UUID,
    ) {
        val oppdragId = OppdragId(
            fagsystem = stønadstype.tilFagsystem(),
            personIdent = personIdent,
            behandlingsId = eksternBehandlingId.toString(),
        )

        val (status, melding) = oppdragClient.hentStatus(oppdragId)

        if (status != OppdragStatus.KVITTERT_OK) {
            throw TaskExceptionUtenStackTrace("Status fra oppdrag er ikke ok, status=$status melding=$melding")
        }

        iverksettResultatService.oppdaterOppdragResultat(
            behandlingId = behandlingId,
            OppdragResultat(oppdragStatus = status),
        )
    }
}
