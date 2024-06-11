package no.nav.utsjekk.simulering.client.dto

import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.simulering.client.dto.Mapper.tilSimuleringDetaljer
import no.nav.utsjekk.simulering.domene.Fagområde
import no.nav.utsjekk.simulering.domene.Periode
import no.nav.utsjekk.simulering.domene.PosteringType
import no.nav.utsjekk.simulering.domene.SimuleringDetaljer
import no.nav.utsjekk.simulering.domene.SimulertPostering
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MapperTest {
    @Test
    fun `skal lage simuleringsdetaljer for ett fagområde`() {
        val simuleringRespons =
            SimuleringResponse(
                gjelderId = "22479409483",
                datoBeregnet = LocalDate.of(2024, 5, 28),
                totalBelop = 800,
                perioder =
                    listOf(
                        SimulertPeriode(
                            fom = LocalDate.of(2024, 5, 1),
                            tom = LocalDate.of(2024, 5, 1),
                            utbetalinger =
                                listOf(
                                    Utbetaling(
                                        fagområde = "TILLST",
                                        fagSystemId = "200000237",
                                        utbetalesTilId = "22479409483",
                                        forfall = LocalDate.of(2024, 5, 28),
                                        feilkonto = false,
                                        detaljer =
                                            listOf(
                                                Postering(
                                                    type = "YTEL",
                                                    faktiskFom = LocalDate.of(2024, 5, 1),
                                                    faktiskTom = LocalDate.of(2024, 5, 1),
                                                    belop = 800,
                                                    sats = 800.0,
                                                    satstype = "DAG",
                                                    klassekode = "TSTBASISP4-OP",
                                                    trekkVedtakId = null,
                                                    refunderesOrgNr = null,
                                                ),
                                            ),
                                    ),
                                ),
                        ),
                    ),
            )

        val forventetSimuleringDetaljer =
            SimuleringDetaljer(
                gjelderId = "22479409483",
                datoBeregnet = LocalDate.of(2024, 5, 28),
                totalBeløp = 800,
                perioder =
                    listOf(
                        Periode(
                            fom = LocalDate.of(2024, 5, 1),
                            tom = LocalDate.of(2024, 5, 1),
                            posteringer =
                                listOf(
                                    SimulertPostering(
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = "200000237",
                                        fom = LocalDate.of(2024, 5, 1),
                                        tom = LocalDate.of(2024, 5, 1),
                                        beløp = 800,
                                        type = PosteringType.YTELSE,
                                    ),
                                ),
                        ),
                    ),
            )

        assertEquals(forventetSimuleringDetaljer, simuleringRespons.tilSimuleringDetaljer(Fagsystem.TILLEGGSSTØNADER))
    }

    @Test
    fun `skal lage simuleringsdetaljer for tilleggsstønader inkl arena`() {
        val simuleringRespons =
            SimuleringResponse(
                gjelderId = "22479409483",
                datoBeregnet = LocalDate.of(2024, 5, 28),
                totalBelop = 1300,
                perioder =
                    listOf(
                        SimulertPeriode(
                            fom = LocalDate.of(2024, 5, 1),
                            tom = LocalDate.of(2024, 5, 1),
                            utbetalinger =
                                listOf(
                                    Utbetaling(
                                        fagområde = "TILLST",
                                        fagSystemId = "200000237",
                                        utbetalesTilId = "22479409483",
                                        forfall = LocalDate.of(2024, 5, 28),
                                        feilkonto = false,
                                        detaljer =
                                            listOf(
                                                Postering(
                                                    type = "YTEL",
                                                    faktiskFom = LocalDate.of(2024, 5, 1),
                                                    faktiskTom = LocalDate.of(2024, 5, 1),
                                                    belop = 800,
                                                    sats = 800.0,
                                                    satstype = "DAG",
                                                    klassekode = "TSTBASISP4-OP",
                                                    trekkVedtakId = null,
                                                    refunderesOrgNr = null,
                                                ),
                                            ),
                                    ),
                                    Utbetaling(
                                        fagområde = "TSTARENA",
                                        fagSystemId = "ARENA-ID",
                                        utbetalesTilId = "22479409483",
                                        forfall = LocalDate.of(2024, 5, 15),
                                        feilkonto = false,
                                        detaljer =
                                            listOf(
                                                Postering(
                                                    type = "YTEL",
                                                    faktiskFom = LocalDate.of(2024, 5, 15),
                                                    faktiskTom = LocalDate.of(2024, 5, 15),
                                                    belop = 500,
                                                    sats = 500.0,
                                                    satstype = "DAG",
                                                    klassekode = "TSTBASISP4-OP",
                                                    trekkVedtakId = null,
                                                    refunderesOrgNr = null,
                                                ),
                                            ),
                                    ),
                                ),
                        ),
                    ),
            )

        val forventetSimuleringDetaljer =
            SimuleringDetaljer(
                gjelderId = "22479409483",
                datoBeregnet = LocalDate.of(2024, 5, 28),
                totalBeløp = 1300,
                perioder =
                    listOf(
                        Periode(
                            fom = LocalDate.of(2024, 5, 1),
                            tom = LocalDate.of(2024, 5, 1),
                            posteringer =
                                listOf(
                                    SimulertPostering(
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = "200000237",
                                        fom = LocalDate.of(2024, 5, 1),
                                        tom = LocalDate.of(2024, 5, 1),
                                        beløp = 800,
                                        type = PosteringType.YTELSE,
                                    ),
                                    SimulertPostering(
                                        fagområde = Fagområde.TILLEGGSSTØNADER_ARENA,
                                        sakId = "ARENA-ID",
                                        fom = LocalDate.of(2024, 5, 15),
                                        tom = LocalDate.of(2024, 5, 15),
                                        beløp = 500,
                                        type = PosteringType.YTELSE,
                                    ),
                                ),
                        ),
                    ),
            )

        assertEquals(forventetSimuleringDetaljer, simuleringRespons.tilSimuleringDetaljer(Fagsystem.TILLEGGSSTØNADER))
    }

    @Test
    fun `skal lage simuleringsdetaljer for tilleggsstønader med flere ytelser`() {
        val simuleringRespons =
            SimuleringResponse(
                gjelderId = "22479409483",
                datoBeregnet = LocalDate.of(2024, 5, 28),
                totalBelop = 1300,
                perioder =
                    listOf(
                        SimulertPeriode(
                            fom = LocalDate.of(2024, 5, 1),
                            tom = LocalDate.of(2024, 5, 1),
                            utbetalinger =
                                listOf(
                                    Utbetaling(
                                        fagområde = "TILLST",
                                        fagSystemId = "200000237",
                                        utbetalesTilId = "22479409483",
                                        forfall = LocalDate.of(2024, 5, 28),
                                        feilkonto = false,
                                        detaljer =
                                            listOf(
                                                Postering(
                                                    type = "YTEL",
                                                    faktiskFom = LocalDate.of(2024, 5, 1),
                                                    faktiskTom = LocalDate.of(2024, 5, 1),
                                                    belop = 800,
                                                    sats = 800.0,
                                                    satstype = "DAG",
                                                    klassekode = "TSTBASISP4-OP",
                                                    trekkVedtakId = null,
                                                    refunderesOrgNr = null,
                                                ),
                                            ),
                                    ),
                                    Utbetaling(
                                        fagområde = "DP",
                                        fagSystemId = "DAGPENGER-ID",
                                        utbetalesTilId = "22479409483",
                                        forfall = LocalDate.of(2024, 5, 15),
                                        feilkonto = false,
                                        detaljer =
                                            listOf(
                                                Postering(
                                                    type = "YTEL",
                                                    faktiskFom = LocalDate.of(2024, 5, 15),
                                                    faktiskTom = LocalDate.of(2024, 5, 15),
                                                    belop = 500,
                                                    sats = 500.0,
                                                    satstype = "DAG",
                                                    klassekode = "DPORAS",
                                                    trekkVedtakId = null,
                                                    refunderesOrgNr = null,
                                                ),
                                            ),
                                    ),
                                ),
                        ),
                    ),
            )

        val forventetSimuleringDetaljer =
            SimuleringDetaljer(
                gjelderId = "22479409483",
                datoBeregnet = LocalDate.of(2024, 5, 28),
                totalBeløp = 1300,
                perioder =
                    listOf(
                        Periode(
                            fom = LocalDate.of(2024, 5, 1),
                            tom = LocalDate.of(2024, 5, 1),
                            posteringer =
                                listOf(
                                    SimulertPostering(
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = "200000237",
                                        fom = LocalDate.of(2024, 5, 1),
                                        tom = LocalDate.of(2024, 5, 1),
                                        beløp = 800,
                                        type = PosteringType.YTELSE,
                                    ),
                                ),
                        ),
                    ),
            )

        assertEquals(forventetSimuleringDetaljer, simuleringRespons.tilSimuleringDetaljer(Fagsystem.TILLEGGSSTØNADER))
    }
}
