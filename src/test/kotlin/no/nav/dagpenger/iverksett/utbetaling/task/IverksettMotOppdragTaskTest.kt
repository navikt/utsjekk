package no.nav.dagpenger.iverksett.utbetaling.task

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.iverksett.felles.oppdrag.OppdragClient
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksettingsresultat
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.domene.sakId
import no.nav.dagpenger.iverksett.utbetaling.domene.transformer.tomTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.lagIverksettingsdata
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingsresultatService
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.dagpenger.kontrakter.felles.somUUID
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Properties
import java.util.UUID

internal class IverksettMotOppdragTaskTest {
    private val oppdragClient = mockk<OppdragClient>()
    private val taskService = mockk<TaskService>()
    private val iverksettingService = mockk<IverksettingService>()
    private val iverksettingsresultatService = mockk<IverksettingsresultatService>()

    private val behandlingId = GeneriskIdSomUUID(UUID.randomUUID())
    private val sakId = GeneriskIdSomUUID(UUID.randomUUID())
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

    @Test
    internal fun `skal sende utbetaling til oppdrag`() {
        val oppdragSlot = slot<Utbetalingsoppdrag>()

        every { iverksettingService.hentIverksetting(any(), any(), any()) } returns
            lagIverksettingsdata(
                sakId = sakId.somUUID,
                behandlingId = behandlingId.somUUID,
                andelsdatoer = listOf(LocalDate.now(), LocalDate.now().minusDays(15)),
            )
        every { oppdragClient.iverksettOppdrag(capture(oppdragSlot)) } just Runs
        every {
            iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
                any(),
                sakId,
                behandlingId.somUUID,
                any(),
            )
        } returns Unit
        every { iverksettingsresultatService.hentTilkjentYtelse(any(), any(), any<UUID>(), any()) } returns null

        iverksettMotOppdragTask.doTask(Task(IverksettMotOppdragTask.TYPE, taskPayload, Properties()))

        verify(exactly = 1) { oppdragClient.iverksettOppdrag(any()) }
        verify(exactly = 1) {
            iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
                any(),
                sakId,
                behandlingId.somUUID,
                any(),
            )
        }

        assertThat(oppdragSlot.captured.fagsystem).isEqualTo(Fagsystem.DAGPENGER)
        assertThat(oppdragSlot.captured.kodeEndring).isEqualTo(Utbetalingsoppdrag.KodeEndring.NY)
    }

    @Test
    internal fun `skal sende opphør av utbetaling til oppdrag`() {
        val oppdragSlot = slot<Utbetalingsoppdrag>()

        every { iverksettingService.hentIverksetting(any(), any(), any()) } returns opphørAvUtbetaling()
        every { oppdragClient.iverksettOppdrag(capture(oppdragSlot)) } just Runs
        every { iverksettingsresultatService.hentIverksettResultat(any(), any(), any()) } returns iverksettingsresultat()
        every { iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(any(), any(), any(), any()) } just Runs

        iverksettMotOppdragTask.doTask(Task(IverksettMotOppdragTask.TYPE, taskPayload, Properties()))

        verify(exactly = 1) { oppdragClient.iverksettOppdrag(any()) }
        verify(exactly = 1) {
            iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
                any(),
                sakId,
                behandlingId.somUUID,
                any(),
            )
        }

        assertEquals(Utbetalingsoppdrag.KodeEndring.ENDR, oppdragSlot.captured.kodeEndring)
        assertEquals(1, oppdragSlot.captured.utbetalingsperiode.size)
        assertNotNull(oppdragSlot.captured.utbetalingsperiode.first().opphør)
    }

    @Test
    internal fun `skal ikke sende tom utbetaling som ikke skal iverksettes til oppdrag`() {
        every { iverksettingService.hentIverksetting(any(), any(), any()) } returns tomUtbetaling()

        iverksettMotOppdragTask.doTask(Task(IverksettMotOppdragTask.TYPE, taskPayload, Properties()))

        verify(exactly = 0) { oppdragClient.iverksettOppdrag(any()) }
    }

    @Test
    internal fun `skal opprette ny task når den er ferdig`() {
        val taskSlot = slot<Task>()
        val task = Task(IverksettMotOppdragTask.TYPE, taskPayload, Properties())

        every { iverksettingService.hentIverksetting(any(), any(), any()) } returns
            lagIverksettingsdata(
                behandlingId = behandlingId.somUUID,
                sakId = sakId.somUUID,
                andelsdatoer = listOf(LocalDate.now(), LocalDate.now().minusDays(15)),
            )
        every { taskService.save(capture(taskSlot)) } returns task

        iverksettMotOppdragTask.onCompletion(task)

        assertThat(taskSlot.captured.payload).isEqualTo(taskPayload)
        assertThat(taskSlot.captured.type).isEqualTo(VentePåStatusFraØkonomiTask.TYPE)
    }

    private fun tomUtbetaling(): Iverksetting {
        val tmpIverksetting = lagIverksettingsdata(sakId = sakId.somUUID)
        return tmpIverksetting.copy(vedtak = tmpIverksetting.vedtak.copy(tilkjentYtelse = tomTilkjentYtelse()))
    }

    private fun opphørAvUtbetaling(): Iverksetting {
        val tmpIverksetting = tomUtbetaling()
        return tmpIverksetting.copy(behandling = tmpIverksetting.behandling.copy(forrigeBehandlingId = behandlingId))
    }

    private fun iverksettingsresultat(): Iverksettingsresultat {
        val iverksett =
            lagIverksettingsdata(
                behandlingId = behandlingId.somUUID,
                sakId = sakId.somUUID,
                andelsdatoer = listOf(LocalDate.now(), LocalDate.now().minusDays(15)),
            )
        val sisteAndelIKjede = iverksett.vedtak.tilkjentYtelse.andelerTilkjentYtelse.first().copy(periodeId = 0)
        return Iverksettingsresultat(
            fagsystem = iverksett.fagsak.fagsystem,
            sakId = iverksett.sakId,
            behandlingId = behandlingId.somUUID,
            iverksettingId = iverksett.behandling.iverksettingId,
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
