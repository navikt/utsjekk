package no.nav.dagpenger.iverksett.konsumenter.økonomi

import java.time.LocalDate
import java.util.UUID
import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.api.IverksettingService
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.util.opprettAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.infrastruktur.util.opprettIverksett
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class IverksettMotOppdragIntegrasjonsTest : ServerTest() {

    @Autowired
    lateinit var iverksettResultatService: IverksettResultatService

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var iverksettingService: IverksettingService

    @Autowired
    lateinit var iverksettMotOppdragTask: IverksettMotOppdragTask

    private val behandlingid: UUID = UUID.randomUUID()
    private val førsteAndel = lagAndelTilkjentYtelse(
        beløp = 1000,
        fraOgMed = LocalDate.of(2021, 1, 1),
        tilOgMed = LocalDate.of(2021, 1, 31),
    )
    private val iverksett =
        opprettIverksett(behandlingid, andeler = listOf(førsteAndel))

    @BeforeEach
    internal fun setUp() {
        iverksettingService.startIverksetting(iverksett)
        iverksettMotOppdrag()
    }

    @Test
    internal fun `start iverksetting, forvent at andelerTilkjentYtelse er lik 1 og har periodeId 1`() {
        val tilkjentYtelse = iverksettResultatService.hentTilkjentYtelse(behandlingid)!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(0)
    }

    @Test
    internal fun `revurdering med en ny periode, forvent at den nye perioden har peker på den forrige`() {
        val behandlingIdRevurdering = UUID.randomUUID()
        val iverksettRevurdering = opprettIverksett(
            behandlingIdRevurdering,
            behandlingid,
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

        val tilkjentYtelse = iverksettResultatService.hentTilkjentYtelse(behandlingIdRevurdering)!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(0)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].periodeId).isEqualTo(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].forrigePeriodeId).isEqualTo(0)
    }

    @Test
    internal fun `revurdering der beløpet på den første endres, og en ny legges til, forvent at den første perioden erstattes`() {
        val behandlingIdRevurdering = UUID.randomUUID()
        val iverksettRevurdering = opprettIverksett(
            behandlingId = behandlingIdRevurdering,
            forrigeBehandlingId = behandlingid,
            andeler = listOf(
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

        val tilkjentYtelse = iverksettResultatService.hentTilkjentYtelse(behandlingIdRevurdering)!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse.first().periodeId).isEqualTo(1)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].periodeId).isEqualTo(2)
        assertThat(tilkjentYtelse.andelerTilkjentYtelse[1].forrigePeriodeId).isEqualTo(1)
    }

    @Test
    internal fun `iverksett med opphør, forventer ingen andeler`() {
        val opphørBehandlingId = UUID.randomUUID()
        val iverksettMedOpphør =
            opprettIverksett(
                opphørBehandlingId,
                behandlingid,
                emptyList(),
            )

        taskService.deleteAll(taskService.findAll())
        iverksettingService.startIverksetting(iverksettMedOpphør)
        iverksettMotOppdrag()

        val tilkjentYtelse = iverksettResultatService.hentTilkjentYtelse(opphørBehandlingId)!!
        assertThat(tilkjentYtelse.andelerTilkjentYtelse).hasSize(0)
    }

    @Test
    internal fun `iverksett skal persisteres med korrekt stønadstype`() {
        val iverksett =
            opprettIverksett(andeler = listOf(opprettAndelTilkjentYtelse(stønadstype = StønadTypeTiltakspenger.TILTAKSPENGER)))

        iverksettingService.startIverksetting(iverksett)

        val iverksettPersistert = iverksettingService.hentIverksetting(iverksett.behandling.behandlingId)
        assertEquals(StønadTypeTiltakspenger.TILTAKSPENGER, iverksettPersistert?.fagsak?.stønadstype)
    }

    private fun iverksettMotOppdrag() {
        val tasks = taskService.findAll()
        assertThat(tasks).hasSize(1)
        iverksettMotOppdragTask.doTask(tasks.first())
    }
}
