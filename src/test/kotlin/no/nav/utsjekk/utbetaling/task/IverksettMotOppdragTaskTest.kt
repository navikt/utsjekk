package no.nav.utsjekk.utbetaling.task

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.utsjekk.felles.oppdrag.OppdragClient
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.objectMapper
import no.nav.utsjekk.kontrakter.oppdrag.OppdragStatus
import no.nav.utsjekk.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.utsjekk.utbetaling.domene.Iverksettingsresultat
import no.nav.utsjekk.utbetaling.domene.OppdragResultat
import no.nav.utsjekk.utbetaling.domene.TilkjentYtelse
import no.nav.utsjekk.utbetaling.domene.sakId
import no.nav.utsjekk.utbetaling.domene.transformer.RandomOSURId
import no.nav.utsjekk.utbetaling.domene.transformer.tomTilkjentYtelse
import no.nav.utsjekk.utbetaling.tilstand.IverksettingService
import no.nav.utsjekk.utbetaling.tilstand.IverksettingsresultatService
import no.nav.utsjekk.utbetaling.util.lagIverksettingsdata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Properties

internal class IverksettMotOppdragTaskTest {
    private val oppdragClient = mockk<OppdragClient>()
    private val taskService = mockk<TaskService>()
    private val iverksettingService = mockk<IverksettingService>()
    private val iverksettingsresultatService = mockk<IverksettingsresultatService>()

    private val behandlingId = RandomOSURId.generate()
    private val sakId = RandomOSURId.generate()
    private val taskPayload =
        objectMapper.writeValueAsString(
            TaskPayload(
                fagsystem = Fagsystem.DAGPENGER,
                sakId = sakId,
                behandlingId = behandlingId,
            ),
        )
    private val iverksettMotOppdragTask =
        IverksettMotOppdragTask(
            iverksettingService = iverksettingService,
            oppdragClient = oppdragClient,
            taskService = taskService,
            iverksettingsresultatService = iverksettingsresultatService,
        )

    @BeforeEach
    fun setup() {
        every { iverksettingService.publiserStatusmelding(any()) } just Runs
    }

    @Test
    internal fun `skal sende utbetaling til oppdrag`() {
        val oppdragSlot = slot<Utbetalingsoppdrag>()

        every { iverksettingService.hentIverksetting(any(), any(), any()) } returns
            lagIverksettingsdata(
                sakId = sakId,
                behandlingId = behandlingId,
                andelsdatoer = listOf(LocalDate.now(), LocalDate.now().minusDays(15)),
            )
        every { oppdragClient.iverksettOppdrag(capture(oppdragSlot)) } just Runs
        every {
            iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
                any(),
                sakId,
                behandlingId,
                any(),
                null,
            )
        } returns Unit
        every { iverksettingsresultatService.hentTilkjentYtelse(any(), any(), any<String>(), any()) } returns null

        iverksettMotOppdragTask.doTask(Task(IverksettMotOppdragTask.TYPE, taskPayload, Properties()))

        verify(exactly = 1) { oppdragClient.iverksettOppdrag(any()) }
        verify(exactly = 1) {
            iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
                any(),
                sakId,
                behandlingId,
                any(),
                null,
            )
        }

        assertEquals(Fagsystem.DAGPENGER, oppdragSlot.captured.fagsystem)
        assertTrue(oppdragSlot.captured.erFørsteUtbetalingPåSak)
    }

    @Test
    internal fun `skal sende opphør av utbetaling til oppdrag`() {
        val oppdragSlot = slot<Utbetalingsoppdrag>()

        every { iverksettingService.hentIverksetting(any(), any(), any()) } returns opphørAvUtbetaling()
        every { oppdragClient.iverksettOppdrag(capture(oppdragSlot)) } just Runs
        every { iverksettingsresultatService.hentIverksettingsresultat(any(), any(), any(), null) } returns iverksettingsresultat
        every { iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(any(), any(), any(), any(), null) } just Runs

        iverksettMotOppdragTask.doTask(Task(IverksettMotOppdragTask.TYPE, taskPayload, Properties()))

        verify(exactly = 1) { oppdragClient.iverksettOppdrag(any()) }
        verify(exactly = 1) {
            iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
                any(),
                sakId,
                behandlingId,
                any(),
                null,
            )
        }

        assertFalse(oppdragSlot.captured.erFørsteUtbetalingPåSak)
        assertEquals(1, oppdragSlot.captured.utbetalingsperiode.size)
        assertNotNull(oppdragSlot.captured.utbetalingsperiode.first().opphør)
    }

    @Test
    internal fun `skal ikke sende tom utbetaling som ikke skal iverksettes til oppdrag`() {
        every { iverksettingService.hentIverksetting(any(), any(), any()) } returns tomUtbetaling()
        every { iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(any(), any(), any(), any(), null) } just Runs
        every { iverksettingsresultatService.oppdaterOppdragResultat(any(), any(), any(), any(), any()) } just Runs

        iverksettMotOppdragTask.doTask(Task(IverksettMotOppdragTask.TYPE, taskPayload, Properties()))

        verify(exactly = 0) { oppdragClient.iverksettOppdrag(any()) }
    }

    @Test
    internal fun `skal opprette ny task når den er ferdig`() {
        val taskSlot = slot<Task>()
        val task = Task(IverksettMotOppdragTask.TYPE, taskPayload, Properties())

        every { iverksettingService.hentIverksetting(any(), any(), any()) } returns
            lagIverksettingsdata(
                behandlingId = behandlingId,
                sakId = sakId,
                andelsdatoer = listOf(LocalDate.now(), LocalDate.now().minusDays(15)),
            )
        every { taskService.save(capture(taskSlot)) } returns task

        iverksettMotOppdragTask.onCompletion(task)

        assertEquals(taskPayload, taskSlot.captured.payload)
        assertEquals(VentePåStatusFraØkonomiTask.TYPE, taskSlot.captured.type)
    }

    private fun tomUtbetaling() =
        lagIverksettingsdata(sakId = sakId).let {
            it.copy(vedtak = it.vedtak.copy(tilkjentYtelse = tomTilkjentYtelse()))
        }

    private fun opphørAvUtbetaling() =
        tomUtbetaling().let {
            it.copy(behandling = it.behandling.copy(forrigeBehandlingId = behandlingId))
        }

    private val iverksettingsresultat get(): Iverksettingsresultat {
        val iverksetting =
            lagIverksettingsdata(
                behandlingId = behandlingId,
                sakId = sakId,
                andelsdatoer = listOf(LocalDate.now(), LocalDate.now().minusDays(15)),
            )
        val sisteAndelIKjede = iverksetting.vedtak.tilkjentYtelse.andelerTilkjentYtelse.first().copy(periodeId = 0)
        return Iverksettingsresultat(
            fagsystem = iverksetting.fagsak.fagsystem,
            sakId = iverksetting.sakId,
            behandlingId = behandlingId,
            iverksettingId = iverksetting.behandling.iverksettingId,
            tilkjentYtelseForUtbetaling =
                TilkjentYtelse(
                    andelerTilkjentYtelse =
                        listOf(
                            sisteAndelIKjede,
                        ),
                    sisteAndelIKjede = sisteAndelIKjede,
                ),
            oppdragResultat = OppdragResultat(OppdragStatus.KVITTERT_OK),
        )
    }
}
