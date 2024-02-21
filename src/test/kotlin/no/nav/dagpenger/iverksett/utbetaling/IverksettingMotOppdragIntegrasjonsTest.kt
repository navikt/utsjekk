package no.nav.dagpenger.iverksett.utbetaling

import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.utbetaling.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.behandlingId
import no.nav.dagpenger.iverksett.utbetaling.domene.sakId
import no.nav.dagpenger.iverksett.utbetaling.task.IverksettMotOppdragTask
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingsresultatService
import no.nav.dagpenger.iverksett.utbetaling.util.behandlingsdetaljer
import no.nav.dagpenger.iverksett.utbetaling.util.enAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.util.enIverksetting
import no.nav.dagpenger.iverksett.utbetaling.util.lagAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.util.vedtaksdetaljer
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.GeneriskId
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomString
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.felles.somString
import no.nav.dagpenger.kontrakter.felles.somUUID
import no.nav.dagpenger.kontrakter.iverksett.IverksettStatus
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.Assertions.assertEquals
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

    @Test
    fun `start iverksetting med 1 andel`() {
        val iverksetting = startIverksetting()

        hentPersistertTilkjentYtelse(iverksetting).also {
            assertEquals(1, it.andelerTilkjentYtelse.size)
            assertEquals(0, it.andelerTilkjentYtelse.first().periodeId)
        }
    }

    @Test
    fun `revurdering med en ny periode, forvent at den nye perioden har peker på den forrige`() {
        val iverksetting = startIverksetting()
        val iverksettingMedRevurdering =
            enIverksetting(
                sakId = iverksetting.sakId,
                forrigeBehandlingId = iverksetting.behandlingId,
                andeler =
                    listOf(
                        lagAndelTilkjentYtelse(
                            beløp = 1000,
                            fraOgMed = LocalDate.of(2021, 1, 1),
                            tilOgMed = LocalDate.of(2021, 1, 31),
                        ),
                        lagAndelTilkjentYtelse(
                            beløp = 1000,
                            fraOgMed = LocalDate.now(),
                            tilOgMed = LocalDate.now().plusMonths(1),
                        ),
                    ),
            )

        startIverksetting(iverksettingMedRevurdering)

        hentPersistertTilkjentYtelse(iverksettingMedRevurdering).also {
            assertEquals(2, it.andelerTilkjentYtelse.size)
            assertEquals(0, it.andelerTilkjentYtelse.first().periodeId)
            assertEquals(1, it.andelerTilkjentYtelse[1].periodeId)
            assertEquals(0, it.andelerTilkjentYtelse[1].forrigePeriodeId)
        }
    }

    @Test
    fun `revurdering der beløpet på den første endres, og en ny legges til, forvent at den første perioden erstattes`() {
        val iverksetting = startIverksetting()
        val iverksettingMedRevurdering =
            enIverksetting(
                sakId = iverksetting.sakId,
                forrigeBehandlingId = iverksetting.behandlingId,
                andeler =
                    listOf(
                        lagAndelTilkjentYtelse(
                            beløp = 299,
                            fraOgMed = LocalDate.of(2021, 1, 1),
                            tilOgMed = LocalDate.of(2021, 1, 31),
                        ),
                        lagAndelTilkjentYtelse(
                            beløp = 1000,
                            fraOgMed = LocalDate.now(),
                            tilOgMed = LocalDate.now().plusMonths(1),
                        ),
                    ),
            )

        startIverksetting(iverksettingMedRevurdering)

        hentPersistertTilkjentYtelse(iverksettingMedRevurdering).also {
            assertEquals(2, it.andelerTilkjentYtelse.size)
            assertEquals(1, it.andelerTilkjentYtelse.first().periodeId)
            assertEquals(2, it.andelerTilkjentYtelse[1].periodeId)
            assertEquals(1, it.andelerTilkjentYtelse[1].forrigePeriodeId)
        }
    }

    @Test
    fun `iverksetting med opphør, forventer ingen andeler`() {
        val iverksetting = startIverksetting()
        val iverksettingMedOpphør =
            enIverksetting(
                sakId = iverksetting.sakId,
                forrigeBehandlingId = iverksetting.behandlingId,
                andeler = emptyList(),
            )

        startIverksetting(iverksettingMedOpphør)

        hentPersistertTilkjentYtelse(iverksettingMedOpphør).also {
            assertEquals(0, it.andelerTilkjentYtelse.size)
        }
    }

    @Test
    fun `iverksett skal persisteres med korrekt fagsystem`() {
        val iverksetting =
            startIverksetting(andeler = listOf(enAndelTilkjentYtelse(stønadstype = StønadTypeTiltakspenger.JOBBKLUBB)))

        hentPersistertIverksetting(iverksetting).also {
            assertEquals(Fagsystem.TILTAKSPENGER, it?.fagsak?.fagsystem)
        }
    }

    @Test
    fun `iverksetting uten utbetaling skal få status UTBETALES_IKKE`() {
        val iverksetting = startIverksetting(enIverksetting(vedtaksdetaljer = vedtaksdetaljer(andeler = emptyList())))
        val status = utledStatus(iverksetting)

        assertEquals(IverksettStatus.OK_UTEN_UTBETALING, status)
    }

    @Test
    fun `iverksetting for tilleggsstønader uten utbetaling skal gå ok`() {
        val iverksetting =
            startIverksetting(
                enIverksetting(
                    fagsystem = Fagsystem.TILLEGGSSTØNADER,
                    sakId = GeneriskIdSomString("54321"),
                    behandlingsdetaljer =
                        behandlingsdetaljer(
                            behandlingId = UUID.fromString("4ea75432-f1f7-4a36-9bc5-498023000af4"),
                            iverksettingId = "9f6eaa12-6b32-42a2-bde9-4b520833443d",
                        ),
                    vedtaksdetaljer = vedtaksdetaljer(andeler = emptyList()),
                ),
            )
        val status = utledStatus(iverksetting)

        assertEquals(IverksettStatus.OK_UTEN_UTBETALING, status)
    }

    private fun startIverksetting(
        sakId: GeneriskId = GeneriskIdSomUUID(UUID.randomUUID()),
        behandlingId: GeneriskId = GeneriskIdSomUUID(UUID.randomUUID()),
        andeler: List<AndelTilkjentYtelse> =
            listOf(
                lagAndelTilkjentYtelse(
                    beløp = 1000,
                    fraOgMed = LocalDate.of(2021, 1, 1),
                    tilOgMed = LocalDate.of(2021, 1, 31),
                ),
            ),
    ) = startIverksetting(
        enIverksetting(
            sakId = sakId,
            behandlingId = behandlingId,
            andeler = andeler,
        ),
    )

    private fun startIverksetting(iverksetting: Iverksetting): Iverksetting {
        iverksettingService.startIverksetting(iverksetting)

        taskService.findAll().let { tasks ->
            assertEquals(1, tasks.size)
            iverksettMotOppdragTask.doTask(tasks.first())
        }

        taskService.deleteAll(taskService.findAll())

        return iverksetting
    }

    private fun hentPersistertIverksetting(iverksetting: Iverksetting) =
        iverksettingService.hentIverksetting(
            fagsystem = iverksetting.fagsak.fagsystem,
            sakId = iverksetting.sakId,
            behandlingId = iverksetting.behandling.behandlingId,
        )

    private fun hentPersistertTilkjentYtelse(iverksetting: Iverksetting) =
        requireNotNull(
            iverksettingsresultatService.hentTilkjentYtelse(
                fagsystem = iverksetting.fagsak.fagsystem,
                sakId = iverksetting.sakId,
                behandlingId = iverksetting.behandlingId.somUUID,
                iverksettingId = iverksetting.behandling.iverksettingId,
            ),
        ) {
            "Fant ikke tilkjent ytelse. Sjekk at testen din faktisk behandler en iverksetting"
        }

    private fun utledStatus(iverksetting: Iverksetting) =
        requireNotNull(
            iverksettingService.utledStatus(
                fagsystem = iverksetting.fagsak.fagsystem,
                sakId = iverksetting.sakId.somString,
                behandlingId = iverksetting.behandlingId.somUUID,
                iverksettingId = iverksetting.behandling.iverksettingId,
            ),
        ) {
            "Fant ikke status for iverksetting. Sjekk at testen din faktisk behandler en iverksetting"
        }
}
