package no.nav.dagpenger.iverksett.utbetaling.task

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.iverksett.felles.oppdrag.OppdragClient
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksettingsresultat
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.domene.transformer.toDomain
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingsresultatService
import no.nav.dagpenger.iverksett.utbetaling.util.opprettIverksettDto
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.dagpenger.kontrakter.felles.somUUID
import no.nav.dagpenger.kontrakter.iverksett.ForrigeIverksettingDto
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.Properties
import java.util.UUID

internal class IverksettingMotOppdragTaskTest {
    private val oppdragClient = mockk<OppdragClient>()
    val taskService = mockk<TaskService>()
    val iverksettingService = mockk<IverksettingService>()
    val iverksettingsresultatService = mockk<IverksettingsresultatService>()
    val behandlingId = GeneriskIdSomUUID(UUID.randomUUID())
    val taskPayload = objectMapper.writeValueAsString(TaskPayload(fagsystem = Fagsystem.DAGPENGER, behandlingId = behandlingId))
    val sakId = GeneriskIdSomUUID(UUID.randomUUID())
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
        every { iverksettingService.hentIverksetting(any(), any()) } returns
            opprettIverksettDto(
                behandlingId,
                sakId,
            ).toDomain()
        every { oppdragClient.iverksettOppdrag(capture(oppdragSlot)) } just Runs
        every { iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId.somUUID, any()) } returns Unit
        every { iverksettingsresultatService.hentTilkjentYtelse(any<UUID>()) } returns null

        iverksettMotOppdragTask.doTask(Task(IverksettMotOppdragTask.TYPE, taskPayload, Properties()))

        verify(exactly = 1) { oppdragClient.iverksettOppdrag(any()) }
        verify(exactly = 1) { iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId.somUUID, any()) }

        assertThat(oppdragSlot.captured.fagsystem).isEqualTo(Fagsystem.DAGPENGER)
        assertThat(oppdragSlot.captured.kodeEndring).isEqualTo(Utbetalingsoppdrag.KodeEndring.NY)
    }

    @Test
    internal fun `skal sende opphør av utbetaling til oppdrag`() {
        val oppdragSlot = slot<Utbetalingsoppdrag>()
        every { iverksettingService.hentIverksetting(any(), any()) } returns opphørAvUtbetaling().toDomain()
        every { oppdragClient.iverksettOppdrag(capture(oppdragSlot)) } just Runs
        every { iverksettingsresultatService.hentIverksettResultat(any()) } returns iverksettResultat()
        every { iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(any(), any()) } just Runs

        iverksettMotOppdragTask.doTask(Task(IverksettMotOppdragTask.TYPE, taskPayload, Properties()))

        verify(exactly = 1) { oppdragClient.iverksettOppdrag(any()) }
        verify(exactly = 1) { iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId.somUUID, any()) }

        assertEquals(Utbetalingsoppdrag.KodeEndring.ENDR, oppdragSlot.captured.kodeEndring)
        assertEquals(1, oppdragSlot.captured.utbetalingsperiode.size)
        assertNotNull(oppdragSlot.captured.utbetalingsperiode.first().opphør)
    }

    @Test
    internal fun `skal ikke sende tom utbetaling som ikke skal iverksettes til oppdrag`() {
        every { iverksettingService.hentIverksetting(any(), any()) } returns tomUtbetaling().toDomain()

        iverksettMotOppdragTask.doTask(Task(IverksettMotOppdragTask.TYPE, taskPayload, Properties()))

        verify(exactly = 0) { oppdragClient.iverksettOppdrag(any()) }
    }

    @Test
    internal fun `skal opprette ny task når den er ferdig`() {
        val taskSlot = slot<Task>()
        val task = Task(IverksettMotOppdragTask.TYPE, taskPayload, Properties())
        every { iverksettingService.hentIverksetting(any(), any()) } returns
            opprettIverksettDto(behandlingId, sakId).toDomain()
        every { taskService.save(capture(taskSlot)) } returns task

        iverksettMotOppdragTask.onCompletion(task)

        assertThat(taskSlot.captured.payload).isEqualTo(taskPayload)
        assertThat(taskSlot.captured.type).isEqualTo(VentePåStatusFraØkonomiTask.TYPE)
    }

    private fun tomUtbetaling(): IverksettDto {
        val tmpIverksettDto = opprettIverksettDto()
        return tmpIverksettDto.copy(vedtak = tmpIverksettDto.vedtak.copy(utbetalinger = emptyList()))
    }

    private fun opphørAvUtbetaling(): IverksettDto {
        return tomUtbetaling().copy(forrigeIverksetting = ForrigeIverksettingDto(behandlingId = behandlingId))
    }

    private fun iverksettResultat(): Iverksettingsresultat {
        val iverksett = opprettIverksettDto(behandlingId, sakId).toDomain()
        val sisteAndelIKjede = iverksett.vedtak.tilkjentYtelse.andelerTilkjentYtelse.first().copy(periodeId = 0)
        return Iverksettingsresultat(
            fagsystem = iverksett.fagsak.stønadstype.tilFagsystem(),
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
