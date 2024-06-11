package no.nav.utsjekk.simulering.domene

import no.nav.utsjekk.simulering.api.OppsummeringForPeriode
import no.nav.utsjekk.simulering.api.SimuleringResponsDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OppsummeringGeneratorTest {
    @Test
    @Disabled
    fun `skal lage oppsummering for ny utbetaling`() {
        val simuleringDetaljer =
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

        val forventetOppsummering =
            SimuleringResponsDto(
                oppsummeringer =
                    listOf(
                        OppsummeringForPeriode(
                            fom = LocalDate.of(2024, 5, 1),
                            tom = LocalDate.of(2024, 5, 1),
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
                gjelderId = "22479409483",
                datoBeregnet = LocalDate.of(2024, 5, 28),
                totalBeløp = 100,
                perioder =
                    listOf(
                        Periode(
                            fom = LocalDate.of(2024, 5, 1),
                            tom = LocalDate.of(2024, 5, 1),
                            posteringer =
                                listOf(
                                    SimulertPostering(
                                        type = PosteringType.YTELSE,
                                        fom = LocalDate.of(2024, 5, 1),
                                        tom = LocalDate.of(2024, 5, 1),
                                        beløp = 800,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = "200000237",
                                    ),
                                    SimulertPostering(
                                        type = PosteringType.YTELSE,
                                        fom = LocalDate.of(2024, 5, 1),
                                        tom = LocalDate.of(2024, 5, 1),
                                        beløp = -700,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = "200000237",
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
                            fom = LocalDate.of(2024, 5, 1),
                            tom = LocalDate.of(2024, 5, 1),
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
                gjelderId = "22479409483",
                datoBeregnet = LocalDate.of(2024, 5, 28),
                totalBeløp = 0,
                perioder =
                    listOf(
                        Periode(
                            fom = LocalDate.of(2024, 5, 1),
                            tom = LocalDate.of(2024, 5, 1),
                            posteringer =
                                listOf(
                                    SimulertPostering(
                                        type = PosteringType.YTELSE,
                                        fom = LocalDate.of(2024, 5, 1),
                                        tom = LocalDate.of(2024, 5, 1),
                                        beløp = 100,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = "200000237",
                                    ),
                                    SimulertPostering(
                                        type = PosteringType.YTELSE,
                                        fom = LocalDate.of(2024, 5, 1),
                                        tom = LocalDate.of(2024, 5, 1),
                                        beløp = 600,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = "200000237",
                                    ),
                                    SimulertPostering(
                                        type = PosteringType.FEILUTBETALING,
                                        fom = LocalDate.of(2024, 5, 1),
                                        tom = LocalDate.of(2024, 5, 1),
                                        beløp = 100,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = "200000237",
                                    ),
                                    SimulertPostering(
                                        type = PosteringType.MOTPOSTERING,
                                        fom = LocalDate.of(2024, 5, 1),
                                        tom = LocalDate.of(2024, 5, 1),
                                        beløp = -100,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = "200000237",
                                    ),
                                    SimulertPostering(
                                        type = PosteringType.YTELSE,
                                        fom = LocalDate.of(2024, 5, 1),
                                        tom = LocalDate.of(2024, 5, 1),
                                        beløp = -700,
                                        fagområde = Fagområde.TILLEGGSSTØNADER,
                                        sakId = "200000237",
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
                            fom = LocalDate.of(2024, 5, 1),
                            tom = LocalDate.of(2024, 5, 1),
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
