package no.nav.utsjekk.simulering.client.dto

import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.simulering.client.dto.Mapper.tilSimuleringDetaljer
import no.nav.utsjekk.simulering.domene.Fagområde
import no.nav.utsjekk.simulering.domene.Periode
import no.nav.utsjekk.simulering.domene.Postering
import no.nav.utsjekk.simulering.domene.PosteringType
import no.nav.utsjekk.simulering.domene.SimuleringDetaljer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MapperTest {
    companion object {
        private const val KLASSEKODE = "TSTBASISP4-OP"
        private const val FAGOMRÅDE = "TILLST"
        private const val SAK_ID = "200000237"
        private const val PERSONIDENT = "22479409483"
        private val DATO_28_MAI = LocalDate.of(2024, 5, 28)
        private val DATO_1_MAI = LocalDate.of(2024, 5, 1)
        private val DATO_15_MAI = LocalDate.of(2024, 5, 15)
    }

    @Test
    fun `skal lage simuleringsdetaljer for ett fagområde`() {
        val simuleringRespons =
            SimuleringResponse(
                gjelderId = PERSONIDENT,
                datoBeregnet = DATO_28_MAI,
                totalBelop = 800,
                perioder =
                    listOf(
                        SimulertPeriode(
                            fom = DATO_1_MAI,
                            tom = DATO_1_MAI,
                            utbetalinger =
                                listOf(
                                    Utbetaling(
                                        fagområde = FAGOMRÅDE,
                                        fagSystemId = SAK_ID,
                                        utbetalesTilId = PERSONIDENT,
                                        forfall = DATO_28_MAI,
                                        feilkonto = false,
                                        detaljer =
                                            listOf(
                                                PosteringDto(
                                                    type = "YTEL",
                                                    faktiskFom = DATO_1_MAI,
                                                    faktiskTom = DATO_1_MAI,
                                                    belop = 800,
                                                    sats = 800.0,
                                                    satstype = "DAG",
                                                    klassekode = KLASSEKODE,
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
                gjelderId = PERSONIDENT,
                datoBeregnet = DATO_28_MAI,
                totalBeløp = 800,
                perioder =
                    listOf(
                        Periode(
                            fom = DATO_1_MAI,
                            tom = DATO_1_MAI,
                            posteringer =
                                listOf(
                                    Postering(
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        fom = DATO_1_MAI,
                                        tom = DATO_1_MAI,
                                        beløp = 800,
                                        type = PosteringType.YTELSE,
                                        klassekode = KLASSEKODE,
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
                gjelderId = PERSONIDENT,
                datoBeregnet = DATO_28_MAI,
                totalBelop = 1300,
                perioder =
                    listOf(
                        SimulertPeriode(
                            fom = DATO_1_MAI,
                            tom = DATO_1_MAI,
                            utbetalinger =
                                listOf(
                                    Utbetaling(
                                        fagområde = FAGOMRÅDE,
                                        fagSystemId = SAK_ID,
                                        utbetalesTilId = PERSONIDENT,
                                        forfall = DATO_28_MAI,
                                        feilkonto = false,
                                        detaljer =
                                            listOf(
                                                PosteringDto(
                                                    type = "YTEL",
                                                    faktiskFom = DATO_1_MAI,
                                                    faktiskTom = DATO_1_MAI,
                                                    belop = 800,
                                                    sats = 800.0,
                                                    satstype = "DAG",
                                                    klassekode = KLASSEKODE,
                                                    trekkVedtakId = null,
                                                    refunderesOrgNr = null,
                                                ),
                                            ),
                                    ),
                                    Utbetaling(
                                        fagområde = "TSTARENA",
                                        fagSystemId = "ARENA-ID",
                                        utbetalesTilId = PERSONIDENT,
                                        forfall = DATO_15_MAI,
                                        feilkonto = false,
                                        detaljer =
                                            listOf(
                                                PosteringDto(
                                                    type = "YTEL",
                                                    faktiskFom = DATO_15_MAI,
                                                    faktiskTom = DATO_15_MAI,
                                                    belop = 500,
                                                    sats = 500.0,
                                                    satstype = "DAG",
                                                    klassekode = KLASSEKODE,
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
                gjelderId = PERSONIDENT,
                datoBeregnet = DATO_28_MAI,
                totalBeløp = 1300,
                perioder =
                    listOf(
                        Periode(
                            fom = DATO_1_MAI,
                            tom = DATO_1_MAI,
                            posteringer =
                                listOf(
                                    Postering(
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        fom = DATO_1_MAI,
                                        tom = DATO_1_MAI,
                                        beløp = 800,
                                        type = PosteringType.YTELSE,
                                        klassekode = KLASSEKODE,
                                    ),
                                    Postering(
                                        fagområde = Fagområde.TILLEGGSSTØNADER_ARENA,
                                        sakId = "ARENA-ID",
                                        fom = DATO_15_MAI,
                                        tom = DATO_15_MAI,
                                        beløp = 500,
                                        type = PosteringType.YTELSE,
                                        klassekode = KLASSEKODE,
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
                gjelderId = PERSONIDENT,
                datoBeregnet = DATO_28_MAI,
                totalBelop = 1300,
                perioder =
                    listOf(
                        SimulertPeriode(
                            fom = DATO_1_MAI,
                            tom = DATO_1_MAI,
                            utbetalinger =
                                listOf(
                                    Utbetaling(
                                        fagområde = FAGOMRÅDE,
                                        fagSystemId = SAK_ID,
                                        utbetalesTilId = PERSONIDENT,
                                        forfall = DATO_28_MAI,
                                        feilkonto = false,
                                        detaljer =
                                            listOf(
                                                PosteringDto(
                                                    type = "YTEL",
                                                    faktiskFom = DATO_1_MAI,
                                                    faktiskTom = DATO_1_MAI,
                                                    belop = 800,
                                                    sats = 800.0,
                                                    satstype = "DAG",
                                                    klassekode = KLASSEKODE,
                                                    trekkVedtakId = null,
                                                    refunderesOrgNr = null,
                                                ),
                                            ),
                                    ),
                                    Utbetaling(
                                        fagområde = "DP",
                                        fagSystemId = "DAGPENGER-ID",
                                        utbetalesTilId = PERSONIDENT,
                                        forfall = DATO_15_MAI,
                                        feilkonto = false,
                                        detaljer =
                                            listOf(
                                                PosteringDto(
                                                    type = "YTEL",
                                                    faktiskFom = DATO_15_MAI,
                                                    faktiskTom = DATO_15_MAI,
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
                gjelderId = PERSONIDENT,
                datoBeregnet = DATO_28_MAI,
                totalBeløp = 1300,
                perioder =
                    listOf(
                        Periode(
                            fom = DATO_1_MAI,
                            tom = DATO_1_MAI,
                            posteringer =
                                listOf(
                                    Postering(
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        fom = DATO_1_MAI,
                                        tom = DATO_1_MAI,
                                        beløp = 800,
                                        type = PosteringType.YTELSE,
                                        klassekode = KLASSEKODE,
                                    ),
                                ),
                        ),
                    ),
            )

        assertEquals(forventetSimuleringDetaljer, simuleringRespons.tilSimuleringDetaljer(Fagsystem.TILLEGGSSTØNADER))
    }
}
