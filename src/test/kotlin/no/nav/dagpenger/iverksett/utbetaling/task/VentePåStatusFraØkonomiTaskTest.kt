package no.nav.dagpenger.iverksett.utbetaling.task

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.iverksett.felles.oppdrag.OppdragClient
import no.nav.dagpenger.iverksett.felles.util.mockFeatureToggleService
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.lagIverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.lagIverksettingsdata
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingRepository
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingsresultatService
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.GeneriskId
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.felles.Satstype
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.dagpenger.kontrakter.felles.somUUID
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatusDto
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsperiode
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Properties
import java.util.UUID

internal class VentePåStatusFraØkonomiTaskTest {
    private val oppdragClient = mockk<OppdragClient>()
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val taskService = mockk<TaskService>()
    private val iverksettingsresultatService = mockk<IverksettingsresultatService>()
    private val behandlingId: GeneriskId = GeneriskIdSomUUID(UUID.randomUUID())
    private val sakId: GeneriskId = GeneriskIdSomUUID(UUID.randomUUID())
    private val taskPayload =
        objectMapper.writeValueAsString(
            TaskPayload(
                fagsystem = Fagsystem.DAGPENGER,
                sakId = sakId,
                behandlingId = behandlingId,
            ),
        )
    private val iverksettingService =
        IverksettingService(
            taskService = taskService,
            oppdragClient = oppdragClient,
            iverksettingRepository = iverksettingRepository,
            iverksettingsresultatService = iverksettingsresultatService,
            featureToggleService = mockFeatureToggleService(),
        )

    private val ventePåStatusFraØkonomiTask =
        VentePåStatusFraØkonomiTask(
            iverksettingService,
            iverksettingsresultatService,
        )

    @BeforeEach
    internal fun setUp() {
        val iverksetting =
            lagIverksettingsdata(
                sakId = sakId.somUUID,
                behandlingId = behandlingId.somUUID,
                andelsdatoer = listOf(LocalDate.now(), LocalDate.now().minusDays(15)),
            )
        every { oppdragClient.hentStatus(any()) } returns OppdragStatusDto(OppdragStatus.KVITTERT_OK, null)
        every { iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(any(), any(), any()) } returns
            listOf(lagIverksettingEntitet(iverksetting))
        every { iverksettingsresultatService.oppdaterOppdragResultat(any(), any(), behandlingId.somUUID, any(), any()) } just runs
        every { taskService.save(any()) } answers { firstArg() }
    }

    @Test
    internal fun `kjør doTask for VentePåStatusFraØkonomiTask, forvent ingen unntak`() {
        val oppdragResultatSlot = slot<OppdragResultat>()
        every { iverksettingsresultatService.hentTilkjentYtelse(any(), any(), behandlingId.somUUID, any()) } returns
            tilkjentYtelse(
                listOf(
                    utbetalingsperiode,
                ),
            )

        runTask(Task(IverksettMotOppdragTask.TYPE, taskPayload, Properties()))

        verify(exactly = 1) {
            iverksettingsresultatService.oppdaterOppdragResultat(
                any(),
                sakId,
                behandlingId.somUUID,
                capture(oppdragResultatSlot),
                any(),
            )
        }

        assertEquals(OppdragStatus.KVITTERT_OK, oppdragResultatSlot.captured.oppdragStatus)
    }

    @Test
    internal fun `Skal ikke gjøre noe hvis ingen utbetalingoppdrag på tilkjent ytelse`() {
        every {
            iverksettingsresultatService.hentTilkjentYtelse(
                any(),
                sakId,
                behandlingId.somUUID,
                any(),
            )
        } returns tilkjentYtelse()

        runTask(Task(IverksettMotOppdragTask.TYPE, taskPayload, Properties()))

        verify(exactly = 0) { iverksettingsresultatService.oppdaterOppdragResultat(any(), sakId, behandlingId.somUUID, any(), any()) }
    }

    private fun runTask(task: Task) {
        ventePåStatusFraØkonomiTask.doTask(task)
    }

    private val utbetalingsperiode =
        Utbetalingsperiode(
            erEndringPåEksisterendePeriode = false,
            opphør = null,
            periodeId = 0,
            forrigePeriodeId = null,
            vedtaksdato = LocalDate.now(),
            klassifisering = "",
            fom = LocalDate.of(2021, 1, 1),
            tom = LocalDate.of(2021, 6, 1),
            sats = BigDecimal.TEN,
            satstype = Satstype.DAGLIG,
            utbetalesTil = "x",
            behandlingId = GeneriskIdSomUUID(UUID.randomUUID()),
            utbetalingsgrad = null,
        )

    private fun tilkjentYtelse(utbetalingsperioder: List<Utbetalingsperiode> = listOf()): TilkjentYtelse {
        return TilkjentYtelse(
            id = UUID.randomUUID(),
            utbetalingsoppdrag =
                Utbetalingsoppdrag(
                    erFørsteUtbetalingPåSak = true,
                    fagsystem = Fagsystem.DAGPENGER,
                    saksnummer = GeneriskIdSomUUID(UUID.randomUUID()),
                    aktør = "",
                    saksbehandlerId = "",
                    avstemmingstidspunkt = LocalDateTime.now(),
                    utbetalingsperiode = utbetalingsperioder,
                    iverksettingId = null,
                ),
            andelerTilkjentYtelse = listOf(),
        )
    }
}
