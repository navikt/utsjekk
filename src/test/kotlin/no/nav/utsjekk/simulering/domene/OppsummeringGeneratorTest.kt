package no.nav.utsjekk.simulering.domene

import no.nav.utsjekk.simulering.api.OppsummeringForPeriode
import no.nav.utsjekk.simulering.api.SimuleringResponsDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
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
    @Disabled
    fun `skal lage oppsummering for ny utbetaling`() {
        val simuleringDetaljer =
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
                                    SimulertPostering(
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

        val forventetOppsummering =
            SimuleringResponsDto(
                oppsummeringer =
                    listOf(
                        OppsummeringForPeriode(
                            fom = DATO_1_MAI,
                            tom = DATO_1_MAI,
                            tidligereUtbetalt = 0,
                            nyUtbetaling = 800,
                            // TODO hva bør denne logisk være for nye utbetalinger? Etterbetaling for perioder tom. dagens dato, ikke etterbetaling for fremtidig?
                            totalEtterbetaling = 800,
                            totalFeilutbetaling = 0,
                        ),
                    ),
                detaljer = simuleringDetaljer,
            )

        assertEquals(forventetOppsummering, OppsummeringGenerator.lagOppsummering(simuleringDetaljer))
    }

    @Test
    @Disabled
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
                                    SimulertPostering(
                                        type = PosteringType.YTELSE,
                                        fom = DATO_1_MAI,
                                        tom = DATO_1_MAI,
                                        beløp = 800,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        klassekode = KLASSEKODE,
                                    ),
                                    SimulertPostering(
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
    @Disabled
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
                                    SimulertPostering(
                                        type = PosteringType.YTELSE,
                                        fom = DATO_1_MAI,
                                        tom = DATO_1_MAI,
                                        beløp = 100,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        klassekode = KLASSEKODE,
                                    ),
                                    SimulertPostering(
                                        type = PosteringType.YTELSE,
                                        fom = DATO_1_MAI,
                                        tom = DATO_1_MAI,
                                        beløp = 600,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        klassekode = KLASSEKODE,
                                    ),
                                    SimulertPostering(
                                        type = PosteringType.FEILUTBETALING,
                                        fom = DATO_1_MAI,
                                        tom = DATO_1_MAI,
                                        beløp = 100,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        klassekode = KLASSEKODE,
                                    ),
                                    SimulertPostering(
                                        type = PosteringType.MOTPOSTERING,
                                        fom = DATO_1_MAI,
                                        tom = DATO_1_MAI,
                                        beløp = -100,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = SAK_ID,
                                        klassekode = KLASSEKODE,
                                    ),
                                    SimulertPostering(
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
