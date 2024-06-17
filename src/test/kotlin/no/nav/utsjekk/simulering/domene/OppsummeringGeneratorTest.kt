package no.nav.utsjekk.simulering.domene

import no.nav.utsjekk.simulering.api.OppsummeringForPeriode
import no.nav.utsjekk.simulering.api.SimuleringResponsDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OppsummeringGeneratorTest {
    companion object {
        private const val KLASSEKODE = "TSTBASISP4-OP"
        private const val SAK_ID = "200000237"
        private const val PERSONIDENT = "22479409483"
        private val DATO_28_MAI = LocalDate.of(2024, 5, 28)
        private val DATO_1_MAI = LocalDate.of(2024, 5, 1)
    }

    @Test
    fun `skal lage oppsummering for ny utbetaling`() {
        val fremtidigPeriode = LocalDate.now().plusMonths(1)
        val simuleringDetaljer =
            SimuleringDetaljer(
                gjelderId = PERSONIDENT,
                datoBeregnet = DATO_28_MAI,
                totalBeløp = 800,
                perioder =
                    listOf(
                        Periode(
                            fom = fremtidigPeriode,
                            tom = fremtidigPeriode,
                            posteringer =
                                listOf(
                                    Postering(
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        fom = fremtidigPeriode,
                                        tom = fremtidigPeriode,
                                        beløp = 800,
                                        type = PosteringType.YTELSE,
                                        klassekode = KLASSEKODE,
                                    ),
                                ),
                        ),
                    ),
            )

        val forventetOppsummering =
            SimuleringResponsDto(
                oppsummeringer =
                    listOf(
                        OppsummeringForPeriode(
                            fom = fremtidigPeriode,
                            tom = fremtidigPeriode,
                            tidligereUtbetalt = 0,
                            nyUtbetaling = 800,
                            totalEtterbetaling = 0,
                            totalFeilutbetaling = 0,
                        ),
                    ),
                detaljer = simuleringDetaljer,
            )

        assertEquals(forventetOppsummering, OppsummeringGenerator.lagOppsummering(simuleringDetaljer))
    }

    @Test
    fun `skal lage oppsummering for etterbetaling`() {
        val simuleringDetaljer =
            SimuleringDetaljer(
                gjelderId = PERSONIDENT,
                datoBeregnet = DATO_28_MAI,
                totalBeløp = 100,
                perioder =
                    listOf(
                        Periode(
                            fom = DATO_1_MAI,
                            tom = DATO_1_MAI,
                            posteringer =
                                listOf(
                                    Postering(
                                        type = PosteringType.YTELSE,
                                        fom = DATO_1_MAI,
                                        tom = DATO_1_MAI,
                                        beløp = 800,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        klassekode = KLASSEKODE,
                                    ),
                                    Postering(
                                        type = PosteringType.YTELSE,
                                        fom = DATO_1_MAI,
                                        tom = DATO_1_MAI,
                                        beløp = -700,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        klassekode = KLASSEKODE,
                                    ),
                                ),
                        ),
                    ),
            )

        val forventetOppsummering =
            SimuleringResponsDto(
                oppsummeringer =
                    listOf(
                        OppsummeringForPeriode(
                            fom = DATO_1_MAI,
                            tom = DATO_1_MAI,
                            tidligereUtbetalt = 700,
                            nyUtbetaling = 800,
                            totalEtterbetaling = 100,
                            totalFeilutbetaling = 0,
                        ),
                    ),
                detaljer = simuleringDetaljer,
            )

        assertEquals(forventetOppsummering, OppsummeringGenerator.lagOppsummering(simuleringDetaljer))
    }

    @Test
    fun `skal lage oppsummering for feilutbetaling`() {
        val simuleringDetaljer =
            SimuleringDetaljer(
                gjelderId = PERSONIDENT,
                datoBeregnet = DATO_28_MAI,
                totalBeløp = 0,
                perioder =
                    listOf(
                        Periode(
                            fom = DATO_1_MAI,
                            tom = DATO_1_MAI,
                            posteringer =
                                listOf(
                                    Postering(
                                        type = PosteringType.YTELSE,
                                        fom = DATO_1_MAI,
                                        tom = DATO_1_MAI,
                                        beløp = 100,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        klassekode = KLASSEKODE,
                                    ),
                                    Postering(
                                        type = PosteringType.YTELSE,
                                        fom = DATO_1_MAI,
                                        tom = DATO_1_MAI,
                                        beløp = 600,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        klassekode = KLASSEKODE,
                                    ),
                                    Postering(
                                        type = PosteringType.FEILUTBETALING,
                                        fom = DATO_1_MAI,
                                        tom = DATO_1_MAI,
                                        beløp = 100,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        klassekode = KLASSEKODE,
                                    ),
                                    Postering(
                                        type = PosteringType.MOTPOSTERING,
                                        fom = DATO_1_MAI,
                                        tom = DATO_1_MAI,
                                        beløp = -100,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        klassekode = KLASSEKODE,
                                    ),
                                    Postering(
                                        type = PosteringType.YTELSE,
                                        fom = DATO_1_MAI,
                                        tom = DATO_1_MAI,
                                        beløp = -700,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        klassekode = KLASSEKODE,
                                    ),
                                ),
                        ),
                    ),
            )

        val forventetOppsummering =
            SimuleringResponsDto(
                oppsummeringer =
                    listOf(
                        OppsummeringForPeriode(
                            fom = DATO_1_MAI,
                            tom = DATO_1_MAI,
                            tidligereUtbetalt = 700,
                            nyUtbetaling = 600,
                            totalEtterbetaling = 0,
                            totalFeilutbetaling = 100,
                        ),
                    ),
                detaljer = simuleringDetaljer,
            )

        assertEquals(forventetOppsummering, OppsummeringGenerator.lagOppsummering(simuleringDetaljer))
    }
}
