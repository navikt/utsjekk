package no.nav.dagpenger.iverksett.konsumenter.brev

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.iverksett.api.IverksettingRepository
import no.nav.dagpenger.iverksett.api.domene.Brev
import no.nav.dagpenger.iverksett.api.domene.Iverksett
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.repository.findByIdOrThrow
import no.nav.dagpenger.iverksett.infrastruktur.transformer.toDomain
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.JournalpostResultat
import no.nav.dagpenger.iverksett.util.opprettIverksettDto
import no.nav.dagpenger.kontrakter.iverksett.iverksett.Brevmottaker
import no.nav.dagpenger.kontrakter.iverksett.journalføring.Journalpost
import no.nav.dagpenger.kontrakter.iverksett.journalføring.Journalposttype
import no.nav.dagpenger.kontrakter.iverksett.journalføring.Journalstatus
import no.nav.dagpenger.kontrakter.iverksett.journalføring.dokarkiv.ArkiverDokumentRequest
import no.nav.dagpenger.kontrakter.iverksett.journalføring.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import java.util.Properties
import java.util.UUID

internal class JournalførVedtaksbrevTaskTest {

    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val journalpostClient = mockk<JournalpostClient>()
    private val taskService = mockk<TaskService>()
    private val iverksettResultatService = mockk<IverksettResultatService>()
    private val journalførVedtaksbrevTask =
        JournalførVedtaksbrevTask(
            iverksettingRepository,
            journalpostClient,
            taskService,
            iverksettResultatService,
        )
    private val behandlingId: UUID = UUID.randomUUID()
    private val behandlingIdString = behandlingId.toString()
    private val journalpostId = "123456789"
    private val arkiverDokumentRequestSlot = mutableListOf<ArkiverDokumentRequest>()
    private val journalpostResultatSlot = slot<JournalpostResultat>()
    private val taskSlot = slot<Task>()

    private val iverksettDto = opprettIverksettDto(behandlingId = behandlingId)

    val iverksettDagpenger = iverksettDto.toDomain()
    private val iverksett = Iverksett(
        iverksettDagpenger.behandling.behandlingId,
        iverksettDagpenger,
        Brev(ByteArray(256)),
    )

    @BeforeEach
    fun setUp() {
        arkiverDokumentRequestSlot.clear()
        journalpostResultatSlot.clear()
        taskSlot.clear()

        every { taskService.save(capture(taskSlot)) } answers { firstArg() }
    }

    @Test
    internal fun `skal journalføre brev og opprette ny task`() {
        every { journalpostClient.arkiverDokument(capture(arkiverDokumentRequestSlot), any()) } returns ArkiverDokumentResponse(
            journalpostId,
            true,
        )
        every { iverksettingRepository.findByIdOrThrow(behandlingId) }.returns(iverksett)
        every { iverksettResultatService.hentJournalpostResultat(behandlingId) } returns null andThen mapOf(
            "123" to JournalpostResultat(
                journalpostId,
            ),
        )
        every {
            iverksettResultatService.oppdaterJournalpostResultat(
                behandlingId,
                any(),
                capture(journalpostResultatSlot),
            )
        } returns Unit

        journalførVedtaksbrevTask.doTask(Task(JournalførVedtaksbrevTask.TYPE, behandlingIdString, Properties()))

        verify(exactly = 1) { journalpostClient.arkiverDokument(any(), any()) }
        verify(exactly = 1) { iverksettResultatService.oppdaterJournalpostResultat(behandlingId, any(), any()) }
        assertThat(arkiverDokumentRequestSlot).hasSize(1)
        assertThat(arkiverDokumentRequestSlot[0].hoveddokumentvarianter).hasSize(1)
        assertThat(journalpostResultatSlot.captured.journalpostId).isEqualTo(journalpostId)
    }

    @Test
    internal fun `skal journalføre brev til alle brevmottakere`() {
        val verge = Brevmottaker(
            "22222222222",
            "Mottaker Navn",
            Brevmottaker.MottakerRolle.VERGE,
            Brevmottaker.IdentType.PERSONIDENT,
        )
        val fullmektig = Brevmottaker(
            "333333333",
            "Mottaker B Navn",
            Brevmottaker.MottakerRolle.FULLMEKTIG,
            Brevmottaker.IdentType.ORGANISASJONSNUMMER,
        )

        val brevmottakere = listOf(
            verge,
            fullmektig,
        )
        val iverksettMedBrevmottakere = opprettIverksettDto(behandlingId).let {
            it.copy(vedtak = it.vedtak.copy(brevmottakere = brevmottakere)).toDomain()
        }

        every { iverksettingRepository.findByIdOrThrow(behandlingId) } returns iverksett.copy(data = iverksettMedBrevmottakere)
        every { iverksettResultatService.hentJournalpostResultat(behandlingId) } returns null andThen mapOf(
            "123" to JournalpostResultat(
                "journalpostId",
            ),
            "444" to JournalpostResultat("journalpostId"),
        )
        every { iverksettResultatService.oppdaterJournalpostResultat(behandlingId, any(), any()) } just Runs

        every {
            journalpostClient.arkiverDokument(capture(arkiverDokumentRequestSlot), any())
        } returns ArkiverDokumentResponse(journalpostId = UUID.randomUUID().toString(), ferdigstilt = true)

        journalførVedtaksbrevTask.doTask(Task(JournalførVedtaksbrevTask.TYPE, behandlingId.toString(), Properties()))

        verify(exactly = 2) { journalpostClient.arkiverDokument(any(), any()) }
        assertThat(arkiverDokumentRequestSlot).hasSize(2)
        assertThat(arkiverDokumentRequestSlot.map { it.avsenderMottaker!!.id }).containsAll(brevmottakere.map { it.ident })
    }

    @Test
    internal fun `skal opprette ny task når den er ferdig`() {
        val task = Task(JournalførVedtaksbrevTask.TYPE, behandlingId.toString(), Properties())

        journalførVedtaksbrevTask.onCompletion(task)

        assertThat(taskSlot.captured.payload).isEqualTo(behandlingId.toString())
        assertThat(taskSlot.captured.type).isEqualTo(DistribuerVedtaksbrevTask.TYPE)
    }

    @Test
    internal fun `skal finne journalpostId for eksternReferanseId vid konflikt ved arkivering`() {
        every { journalpostClient.arkiverDokument(capture(arkiverDokumentRequestSlot), any()) } throws
            RessursException(
                Ressurs.failure(""),
                HttpClientErrorException.create(HttpStatus.CONFLICT, "Feil", HttpHeaders(), byteArrayOf(), null),
            )
        every { journalpostClient.finnJournalposter(any()) } answers {
            listOf(
                Journalpost(
                    journalpostId,
                    Journalposttype.U,
                    Journalstatus.JOURNALFOERT,
                    eksternReferanseId = arkiverDokumentRequestSlot[0].eksternReferanseId,
                ),
            )
        }
        every { iverksettingRepository.findByIdOrThrow(behandlingId) }.returns(iverksett)

        every { iverksettResultatService.hentJournalpostResultat(behandlingId) } returns null andThen mapOf(
            "123" to JournalpostResultat(
                journalpostId,
            ),
        )
        justRun { iverksettResultatService.oppdaterJournalpostResultat(behandlingId, any(), capture(journalpostResultatSlot)) }

        journalførVedtaksbrevTask.doTask(Task(JournalførVedtaksbrevTask.TYPE, behandlingIdString, Properties()))

        verify(exactly = 1) { journalpostClient.arkiverDokument(any(), any()) }
        verify(exactly = 1) { journalpostClient.finnJournalposter(any()) }
        verify(exactly = 1) { iverksettResultatService.oppdaterJournalpostResultat(behandlingId, any(), any()) }

        assertThat(arkiverDokumentRequestSlot).hasSize(1)
        assertThat(arkiverDokumentRequestSlot[0].hoveddokumentvarianter).hasSize(1)
        assertThat(journalpostResultatSlot.captured.journalpostId).isEqualTo(journalpostId)
    }
}
