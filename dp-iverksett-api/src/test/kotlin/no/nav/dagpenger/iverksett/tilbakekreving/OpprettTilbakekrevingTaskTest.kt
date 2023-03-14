package no.nav.dagpenger.iverksett.tilbakekreving

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.iverksett.beriketSimuleringsresultat
import no.nav.dagpenger.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.dagpenger.iverksett.iverksetting.IverksettingRepository
import no.nav.dagpenger.iverksett.iverksetting.domene.TilbakekrevingResultat
import no.nav.dagpenger.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.dagpenger.iverksett.iverksetting.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.kontrakter.felles.Enhet
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Tilbakekrevingsvalg
import no.nav.dagpenger.iverksett.lagIverksett
import no.nav.dagpenger.iverksett.repository.findByIdOrThrow
import no.nav.dagpenger.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.dagpenger.iverksett.util.opprettTilbakekrevingMedVarsel
import no.nav.dagpenger.iverksett.util.opprettTilbakekrevingsdetaljer
import no.nav.dagpenger.iverksett.økonomi.IverksettMotOppdragTask
import no.nav.dagpenger.iverksett.økonomi.simulering.SimuleringService
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class OpprettTilbakekrevingTaskTest {

    private val iverksettResultatService = mockk<IverksettResultatService>()
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val tilbakekrevingClient = mockk<TilbakekrevingClient>()
    private val simuleringService = mockk<SimuleringService>()
    private val familieIntegrasjonerClient = mockk<FamilieIntegrasjonerClient>()
    private val taskService = mockk<TaskService>()

    private val opprettTilbakekrevingTask = OpprettTilbakekrevingTask(
        iverksettResultatService = iverksettResultatService,
        taskService = taskService,
        iverksettingRepository = iverksettingRepository,
        tilbakekrevingClient = tilbakekrevingClient,
        simuleringService = simuleringService,
        familieIntegrasjonerClient = familieIntegrasjonerClient,
    )

    @BeforeEach
    fun init() {
        every { tilbakekrevingClient.finnesÅpenBehandling(any()) } returns false
        every { familieIntegrasjonerClient.hentBehandlendeEnhetForBehandling(any()) } returns
            Enhet("1", "Oslo")
        every { tilbakekrevingClient.opprettBehandling(any()) } just Runs
    }

    @Test
    fun `uendret og ingen feilutbetaling - ikke opprett tilbakekreving`() {
        val tilbakekreving = null
        val behandlingsId = UUID.randomUUID()
        val iverksett = opprettIverksettOvergangsstønad(
            behandlingsId,
            tilbakekreving = tilbakekreving,
            forrigeBehandlingId = UUID.randomUUID(),
        )
        every { iverksettingRepository.findByIdOrThrow(behandlingsId) } returns lagIverksett(iverksett)

        doTask(behandlingsId)

        verify(exactly = 0) { simuleringService.hentBeriketSimulering(any()) }
        verify(exactly = 0) { tilbakekrevingClient.opprettBehandling(any()) }
    }

    @Test
    fun `Ingen tilbakekreving under saksbehandling - vi oppretter heller ingen ny nå`() {
        val behandlingsId = UUID.randomUUID()
        val tilbakekreving = null
        val iverksett = opprettIverksettOvergangsstønad(
            behandlingsId,
            tilbakekreving = tilbakekreving,
            forrigeBehandlingId = UUID.randomUUID(),
        )
        every { iverksettingRepository.findByIdOrThrow(behandlingsId) } returns lagIverksett(iverksett)

        doTask(behandlingsId)

        verify(exactly = 0) { tilbakekrevingClient.opprettBehandling(any()) }
    }

    @Test
    fun `Ignorer tilbakekreving under saksbehandling - vi oppretter heller ingen ny nå`() {
        val behandlingsId = UUID.randomUUID()
        val tilbakekrevingsdetaljer = Tilbakekrevingsdetaljer(
            tilbakekrevingsvalg = Tilbakekrevingsvalg.IGNORER_TILBAKEKREVING,
            tilbakekrevingMedVarsel = opprettTilbakekrevingMedVarsel(),
        )
        val iverksett = opprettIverksettOvergangsstønad(
            behandlingsId,
            tilbakekreving = tilbakekrevingsdetaljer,
            forrigeBehandlingId = UUID.randomUUID(),
        )
        every { iverksettingRepository.findByIdOrThrow(behandlingsId) } returns lagIverksett(iverksett)

        doTask(behandlingsId)

        verify(exactly = 0) { tilbakekrevingClient.opprettBehandling(any()) }
        // trenger heller ikke hente beriket simulering - skal stoppe før vi kommer dit
        verify(exactly = 0) { simuleringService.hentBeriketSimulering(any()) }
    }

    @Test
    fun `førstegangsbehandling skal ikke opprette tilbakekreving`() {
        val behandlingsId = UUID.randomUUID()
        val iverksett = opprettIverksettOvergangsstønad(behandlingsId, tilbakekreving = null, forrigeBehandlingId = null)

        every { iverksettingRepository.findByIdOrThrow(behandlingsId) } returns lagIverksett(iverksett)

        doTask(behandlingsId)

        verify(exactly = 0) { simuleringService.hentBeriketSimulering(any()) }
        verify(exactly = 0) { iverksettResultatService.oppdaterTilbakekrevingResultat(any(), any()) }
        verify(exactly = 0) { tilbakekrevingClient.opprettBehandling(any()) }
    }

    @Test
    fun `uendret, postiv feilutbetaling`() {
        val behandlingsId = UUID.randomUUID()
        val tilbakekreving = opprettTilbakekrevingsdetaljer().medFeilutbetaling(100)
        val iverksett = opprettIverksettOvergangsstønad(
            behandlingsId,
            tilbakekreving = tilbakekreving,
            forrigeBehandlingId = UUID.randomUUID(),
        )
        val tilbakekrevingResultatSlot = slot<TilbakekrevingResultat>()
        val beriketSimuleringsresultat = beriketSimuleringsresultat().medFeilutbetaling(100)

        every { iverksettingRepository.findByIdOrThrow(behandlingsId) } returns lagIverksett(iverksett)
        every { simuleringService.hentBeriketSimulering(any()) } returns beriketSimuleringsresultat
        every {
            iverksettResultatService.oppdaterTilbakekrevingResultat(
                behandlingsId,
                capture(tilbakekrevingResultatSlot),
            )
        } just Runs

        doTask(behandlingsId)

        verify(exactly = 1) { tilbakekrevingClient.opprettBehandling(any()) }

        val request = tilbakekrevingResultatSlot.captured.opprettTilbakekrevingRequest
        assertThat(request.faktainfo.tilbakekrevingsvalg).isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingsvalg)
        assertThat(request.varsel?.varseltekst).isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingMedVarsel?.varseltekst)
        assertThat(request.varsel?.sumFeilutbetaling).isEqualTo(tilbakekreving.tilbakekrevingMedVarsel?.sumFeilutbetaling)
    }

    @Test
    fun `feilutbetaling forsvinner på iverksett`() {
        val behandlingsId = UUID.randomUUID()
        val tilbakekreving = opprettTilbakekrevingsdetaljer().medFeilutbetaling(100)
        val iverksett = opprettIverksettOvergangsstønad(
            behandlingsId,
            tilbakekreving = tilbakekreving,
            forrigeBehandlingId = UUID.randomUUID(),
        )
        val beriketSimuleringsresultat = beriketSimuleringsresultat().medFeilutbetaling(0)

        every { iverksettingRepository.findByIdOrThrow(behandlingsId) } returns lagIverksett(iverksett)
        every { simuleringService.hentBeriketSimulering(any()) } returns beriketSimuleringsresultat

        doTask(behandlingsId)

        verify(exactly = 0) { tilbakekrevingClient.opprettBehandling(any()) }
    }

    @Test
    fun `feilutbetaling endres`() {
        val behandlingsId = UUID.randomUUID()
        val tilbakekreving = opprettTilbakekrevingsdetaljer().medFeilutbetaling(100)
        val iverksett = opprettIverksettOvergangsstønad(
            behandlingsId,
            tilbakekreving = tilbakekreving,
            forrigeBehandlingId = UUID.randomUUID(),
        )
        val tilbakekrevingResultatSlot = slot<TilbakekrevingResultat>()
        val beriketSimuleringsresultat = beriketSimuleringsresultat().medFeilutbetaling(200)

        every { iverksettingRepository.findByIdOrThrow(behandlingsId) } returns lagIverksett(iverksett)
        every { simuleringService.hentBeriketSimulering(any()) } returns beriketSimuleringsresultat
        every {
            iverksettResultatService.oppdaterTilbakekrevingResultat(
                behandlingsId,
                capture(tilbakekrevingResultatSlot),
            )
        } just Runs

        doTask(behandlingsId)

        verify(exactly = 1) { tilbakekrevingClient.opprettBehandling(any()) }

        val request = tilbakekrevingResultatSlot.captured.opprettTilbakekrevingRequest
        assertThat(request.faktainfo.tilbakekrevingsvalg).isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingsvalg)
        assertThat(request.varsel?.varseltekst).isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingMedVarsel?.varseltekst)
        assertThat(request.varsel?.sumFeilutbetaling).isEqualTo(beriketSimuleringsresultat.oppsummering.feilutbetaling)
    }

    @Test
    internal fun `skal opprette IverksettMotOppdragTask når OpprettTilbakekrevingTask er ferdig`() {
        val taskSlot = slot<Task>()
        val behandlingId = UUID.randomUUID().toString()
        val task = Task(OpprettTilbakekrevingTask.TYPE, payload = behandlingId)
        every { taskService.save(capture(taskSlot)) } returns task
        opprettTilbakekrevingTask.onCompletion(task)
        assertThat(taskSlot.captured.payload).isEqualTo(behandlingId)
        assertThat(taskSlot.captured.type).isEqualTo(IverksettMotOppdragTask.TYPE)
    }

    private fun doTask(behandlingsId: UUID) {
        val task = Task(OpprettTilbakekrevingTask.TYPE, payload = behandlingsId.toString())
        opprettTilbakekrevingTask.doTask(task)
    }

    fun Tilbakekrevingsdetaljer.medFeilutbetaling(beløp: Long) =
        this.copy(tilbakekrevingMedVarsel = this.tilbakekrevingMedVarsel?.copy(sumFeilutbetaling = beløp.toBigDecimal()))

    fun BeriketSimuleringsresultat.medFeilutbetaling(beløp: Long) =
        this.copy(oppsummering = this.oppsummering.copy(feilutbetaling = beløp.toBigDecimal()))
}
