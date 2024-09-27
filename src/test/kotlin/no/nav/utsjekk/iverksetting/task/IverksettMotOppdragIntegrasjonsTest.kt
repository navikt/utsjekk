package no.nav.utsjekk.iverksetting.task

import no.nav.familie.prosessering.internal.TaskService
import no.nav.utsjekk.Integrasjonstest
import no.nav.utsjekk.initializers.KafkaContainerInitializer
import no.nav.utsjekk.iverksetting.domene.AndelTilkjentYtelse
import no.nav.utsjekk.iverksetting.domene.Iverksetting
import no.nav.utsjekk.iverksetting.domene.StønadsdataTilleggsstønader
import no.nav.utsjekk.iverksetting.domene.StønadsdataTiltakspenger
import no.nav.utsjekk.iverksetting.domene.behandlingId
import no.nav.utsjekk.iverksetting.domene.sakId
import no.nav.utsjekk.iverksetting.domene.transformer.RandomOSURId
import no.nav.utsjekk.iverksetting.tilstand.IverksettingService
import no.nav.utsjekk.iverksetting.tilstand.IverksettingsresultatService
import no.nav.utsjekk.iverksetting.util.behandlingsdetaljer
import no.nav.utsjekk.iverksetting.util.enAndelTilkjentYtelse
import no.nav.utsjekk.iverksetting.util.enIverksetting
import no.nav.utsjekk.iverksetting.util.vedtaksdetaljer
import no.nav.utsjekk.kontrakter.felles.BrukersNavKontor
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.StønadTypeTilleggsstønader
import no.nav.utsjekk.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.utsjekk.kontrakter.felles.objectMapper
import no.nav.utsjekk.kontrakter.iverksett.IverksettStatus
import no.nav.utsjekk.kontrakter.iverksett.StatusEndretMelding
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class IverksettMotOppdragIntegrasjonsTest : Integrasjonstest() {
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
                        enAndelTilkjentYtelse(
                            beløp = 1000,
                            fra = LocalDate.of(2021, 1, 1),
                            til = LocalDate.of(2021, 1, 31),
                        ),
                        enAndelTilkjentYtelse(
                            beløp = 1000,
                            fra = LocalDate.now(),
                            til = LocalDate.now().plusMonths(1),
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
                        enAndelTilkjentYtelse(
                            beløp = 299,
                            fra = LocalDate.of(2021, 1, 1),
                            til = LocalDate.of(2021, 1, 31),
                        ),
                        enAndelTilkjentYtelse(
                            beløp = 1000,
                            fra = LocalDate.now(),
                            til = LocalDate.now().plusMonths(1),
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
            startIverksetting(
                andeler =
                    listOf(
                        enAndelTilkjentYtelse(
                            stønadsdata =
                                StønadsdataTiltakspenger(
                                    stønadstype = StønadTypeTiltakspenger.JOBBKLUBB,
                                    brukersNavKontor = BrukersNavKontor("4400"),
                                    meldekortId = "M1",
                                ),
                        ),
                    ),
            )

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
                    sakId = "54321",
                    behandlingsdetaljer =
                        behandlingsdetaljer(
                            behandlingId = RandomOSURId.generate(),
                            iverksettingId = "9f6eaa12-6b32-42a2-bde9-4b520833443d",
                        ),
                    vedtaksdetaljer = vedtaksdetaljer(andeler = emptyList()),
                ),
            )
        val status = utledStatus(iverksetting)

        assertEquals(IverksettStatus.OK_UTEN_UTBETALING, status)
    }

    @Test
    fun `utbetalingsperioder for tilleggsstønader med ulike NAV-kontor skal havne på samme kjede`() {
        val iverksetting =
            startIverksetting(
                enIverksetting(
                    fagsystem = Fagsystem.TILLEGGSSTØNADER,
                    sakId = "54321",
                    behandlingsdetaljer =
                        behandlingsdetaljer(
                            behandlingId = RandomOSURId.generate(),
                            iverksettingId = "9f6eaa12-6b32-42a2-bde9-4b520833443d",
                        ),
                    vedtaksdetaljer =
                        vedtaksdetaljer(
                            andeler =
                                listOf(
                                    enAndelTilkjentYtelse(
                                        beløp = 100,
                                        fra = LocalDate.of(2021, 1, 1),
                                        til = LocalDate.of(2021, 1, 31),
                                        stønadsdata =
                                            StønadsdataTilleggsstønader(
                                                stønadstype = StønadTypeTilleggsstønader.TILSYN_BARN_AAP,
                                                brukersNavKontor = null,
                                            ),
                                    ),
                                    enAndelTilkjentYtelse(
                                        beløp = 400,
                                        fra = LocalDate.of(2021, 2, 1),
                                        til = LocalDate.of(2021, 2, 28),
                                        stønadsdata =
                                            StønadsdataTilleggsstønader(
                                                stønadstype = StønadTypeTilleggsstønader.TILSYN_BARN_AAP,
                                                brukersNavKontor = BrukersNavKontor(enhet = "4000"),
                                            ),
                                    ),
                                    enAndelTilkjentYtelse(
                                        beløp = 500,
                                        fra = LocalDate.of(2021, 3, 1),
                                        til = LocalDate.of(2021, 3, 31),
                                        stønadsdata =
                                            StønadsdataTilleggsstønader(
                                                stønadstype = StønadTypeTilleggsstønader.TILSYN_BARN_AAP,
                                                brukersNavKontor = BrukersNavKontor(enhet = "3200"),
                                            ),
                                    ),
                                ),
                        ),
                ),
            )

        hentPersistertTilkjentYtelse(iverksetting).also {
            assertEquals(1, it.sisteAndelPerKjede.size)
        }
    }

    @Test
    fun `produserer statusmelding på kafka når utbetaling iverksettes, forventer SENDT_TIL_OPPDRAG`() {
        val sakId = "54321"
        val behandlingId = RandomOSURId.generate()
        val iverksettingId = "9f6eaa12-6b32-42a2-bde9-4b520833443d"
        val iverksetting =
            enIverksetting(
                sakId = sakId,
                behandlingsdetaljer =
                    behandlingsdetaljer(
                        behandlingId = behandlingId,
                        iverksettingId = iverksettingId,
                    ),
                vedtaksdetaljer = vedtaksdetaljer(),
            )

        startIverksetting(iverksetting)

        val raw = KafkaContainerInitializer.getAllRecords().first().value()
        val melding = objectMapper.readValue(raw, StatusEndretMelding::class.java)

        assertEquals(sakId, melding.sakId)
        assertEquals(behandlingId, melding.behandlingId)
        assertEquals(iverksettingId, melding.iverksettingId)
        assertEquals(IverksettStatus.SENDT_TIL_OPPDRAG, melding.status)
    }

    @Test
    fun `produserer statusmelding på kafka når iverksetting ikke har utbetaling, forventer OK_UTEN_UTBETALING`() {
        val sakId = "54321"
        val behandlingId = RandomOSURId.generate()
        val iverksettingId = "9f6eaa12-6b32-42a2-bde9-4b520833443d"
        val iverksetting =
            enIverksetting(
                sakId = sakId,
                behandlingsdetaljer =
                    behandlingsdetaljer(
                        behandlingId = behandlingId,
                        iverksettingId = iverksettingId,
                    ),
                vedtaksdetaljer = vedtaksdetaljer(andeler = emptyList()),
            )

        startIverksetting(iverksetting)

        val raw = KafkaContainerInitializer.getAllRecords().first().value()
        val melding = objectMapper.readValue(raw, StatusEndretMelding::class.java)

        assertEquals(sakId, melding.sakId)
        assertEquals(behandlingId, melding.behandlingId)
        assertEquals(iverksettingId, melding.iverksettingId)
        assertEquals(IverksettStatus.OK_UTEN_UTBETALING, melding.status)
    }

    private fun startIverksetting(
        sakId: String = RandomOSURId.generate(),
        behandlingId: String = RandomOSURId.generate(),
        andeler: List<AndelTilkjentYtelse> =
            listOf(
                enAndelTilkjentYtelse(
                    beløp = 1000,
                    fra = LocalDate.of(2021, 1, 1),
                    til = LocalDate.of(2021, 1, 31),
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
                behandlingId = iverksetting.behandlingId,
                iverksettingId = iverksetting.behandling.iverksettingId,
            ),
        ) {
            "Fant ikke tilkjent ytelse. Sjekk at testen din faktisk behandler en iverksetting"
        }

    private fun utledStatus(iverksetting: Iverksetting) =
        requireNotNull(
            iverksettingService.utledStatus(
                fagsystem = iverksetting.fagsak.fagsystem,
                sakId = iverksetting.sakId,
                behandlingId = iverksetting.behandlingId,
                iverksettingId = iverksetting.behandling.iverksettingId,
            ),
        ) {
            "Fant ikke status for iverksetting. Sjekk at testen din faktisk behandler en iverksetting"
        }
}
