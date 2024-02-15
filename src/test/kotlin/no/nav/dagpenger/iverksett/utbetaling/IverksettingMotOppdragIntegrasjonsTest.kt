package no.nav.dagpenger.iverksett.utbetaling

import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.utbetaling.domene.sakId
import no.nav.dagpenger.iverksett.utbetaling.task.IverksettMotOppdragTask
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingsresultatService
import no.nav.dagpenger.iverksett.utbetaling.util.enAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.util.enIverksetting
import no.nav.dagpenger.iverksett.utbetaling.util.lagAndelTilkjentYtelse
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.felles.somUUID
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.UUID

class IverksettingMotOppdragIntegrasjonsTest : ServerTest() {
    @Autowired
    lateinit var iverksettingsresultatService: IverksettingsresultatService

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var iverksettingService: IverksettingService

    @Autowired
    lateinit var iverksettMotOppdragTask: IverksettMotOppdragTask

    private val behandlingid = GeneriskIdSomUUID(UUID.randomUUID())
    private val sakId = GeneriskIdSomUUID(UUID.randomUUID())
    private val førsteAndel =
        lagAndelTilkjentYtelse(
            beløp = 1000,
            fraOgMed = LocalDate.of(2021, 1, 1),
            tilOgMed = LocalDate.of(2021, 1, 31),
        )
    private val iverksett =
        enIverksetting(sakId = sakId, behandlingId = behandlingid, andeler = listOf(førsteAndel))

    @BeforeEach
    internal fun setUp() {
        iverksettingService.startIverksetting(iverksett)
        iverksettMotOppdrag()
    }

    @Test
    internal fun `start iverksetting, forvent at andelerTilkjentYtelse er lik 1 og har periodeId 1`() {
        val tilkjentYtelse =
            iverksettingsresultatService.hentTilkjentYtelse(
                fagsystem = iverksett.fagsak.fagsystem,
                sakId = iverksett.sakId,
                behandlingId = behandlingid.somUUID,
                iverksettingId = iverksett.behandling.iverksettingId,
            )!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(0)
    }

    @Test
    internal fun `revurdering med en ny periode, forvent at den nye perioden har peker på den forrige`() {
        val behandlingIdRevurdering = GeneriskIdSomUUID(UUID.randomUUID())
        val iverksettRevurdering =
            enIverksetting(
                sakId = sakId,
                behandlingId = behandlingIdRevurdering,
                forrigeBehandlingId = behandlingid,
                andeler =
                    listOf(
                        førsteAndel,
                        lagAndelTilkjentYtelse(
                            beløp = 1000,
                            fraOgMed = LocalDate.now(),
                            tilOgMed = LocalDate.now().plusMonths(1),
                        ),
                    ),
            )

        taskService.deleteAll(taskService.findAll())
        iverksettingService.startIverksetting(iverksettRevurdering)
        iverksettMotOppdrag()

        val tilkjentYtelse =
            iverksettingsresultatService.hentTilkjentYtelse(
                fagsystem = iverksettRevurdering.fagsak.fagsystem,
                sakId = iverksettRevurdering.sakId,
                behandlingId = behandlingIdRevurdering.somUUID,
                iverksettingId = iverksettRevurdering.behandling.iverksettingId,
            )!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(0)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].periodeId).isEqualTo(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].forrigePeriodeId).isEqualTo(0)
    }

    @Test
    internal fun `revurdering der beløpet på den første endres, og en ny legges til, forvent at den første perioden erstattes`() {
        val behandlingIdRevurdering = GeneriskIdSomUUID(UUID.randomUUID())
        val iverksettRevurdering =
            enIverksetting(
                sakId = sakId,
                behandlingId = behandlingIdRevurdering,
                forrigeBehandlingId = behandlingid,
                andeler =
                    listOf(
                        førsteAndel.copy(
                            beløp = 299,
                        ),
                        lagAndelTilkjentYtelse(
                            beløp = 1000,
                            fraOgMed = LocalDate.now(),
                            tilOgMed = LocalDate.now().plusMonths(1),
                        ),
                    ),
            )

        taskService.deleteAll(taskService.findAll())
        iverksettingService.startIverksetting(iverksettRevurdering)
        iverksettMotOppdrag()

        val tilkjentYtelse =
            iverksettingsresultatService.hentTilkjentYtelse(
                fagsystem = iverksettRevurdering.fagsak.fagsystem,
                sakId = iverksettRevurdering.sakId,
                behandlingId = behandlingIdRevurdering.somUUID,
                iverksettingId = iverksettRevurdering.behandling.iverksettingId,
            )!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].periodeId).isEqualTo(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].forrigePeriodeId).isEqualTo(1)
    }

    @Test
    internal fun `iverksett med opphør, forventer ingen andeler`() {
        val opphørBehandlingId = GeneriskIdSomUUID(UUID.randomUUID())
        val iverksettMedOpphør =
            enIverksetting(
                sakId = sakId,
                behandlingId = opphørBehandlingId,
                forrigeBehandlingId = behandlingid,
                andeler = emptyList(),
            )

        taskService.deleteAll(taskService.findAll())
        iverksettingService.startIverksetting(iverksettMedOpphør)
        iverksettMotOppdrag()

        val tilkjentYtelse =
            iverksettingsresultatService.hentTilkjentYtelse(
                fagsystem = iverksettMedOpphør.fagsak.fagsystem,
                sakId = iverksettMedOpphør.sakId,
                behandlingId = opphørBehandlingId.somUUID,
                iverksettingId = iverksettMedOpphør.behandling.iverksettingId,
            )!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(0)
    }

    @Test
    internal fun `iverksett skal persisteres med korrekt fagsystem`() {
        val iverksett =
            enIverksetting(andeler = listOf(enAndelTilkjentYtelse(stønadstype = StønadTypeTiltakspenger.JOBBKLUBB)))

        iverksettingService.startIverksetting(iverksett)

        val iverksettPersistert =
            iverksettingService.hentIverksetting(
                fagsystem = iverksett.fagsak.fagsystem,
                sakId = iverksett.sakId,
                behandlingId = iverksett.behandling.behandlingId,
            )
        assertEquals(Fagsystem.TILTAKSPENGER, iverksettPersistert?.fagsak?.fagsystem)
    }

    private fun iverksettMotOppdrag() {
        val tasks = taskService.findAll()
        assertThat(tasks).hasSize(1)
        iverksettMotOppdragTask.doTask(tasks.first())
    }
}
