package no.nav.utsjekk.simulering.domene

import no.nav.utsjekk.simulering.api.OppsummeringForPeriode
import no.nav.utsjekk.simulering.api.SimuleringResponsDto
import no.nav.utsjekk.simulering.client.dto.Postering
import no.nav.utsjekk.simulering.client.dto.SimuleringResponse
import no.nav.utsjekk.simulering.client.dto.SimulertPeriode
import no.nav.utsjekk.simulering.client.dto.Utbetaling
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OppsummeringGeneratorTest {
    @Test
    @Disabled
    fun `skal lage oppsummering for ny utbetaling`() {
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
                rådata = simuleringRespons,
            )

        assertEquals(forventetOppsummering, OppsummeringGenerator.lagOppsummering(simuleringRespons))
    }

    @Test
    @Disabled
    fun `skal lage oppsummering for etterbetaling`() {
        val simuleringRespons =
            SimuleringResponse(
                gjelderId = "22479409483",
                datoBeregnet = LocalDate.of(2024, 5, 28),
                totalBelop = 100,
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
                                                Postering(
                                                    type = "YTEL",
                                                    faktiskFom = LocalDate.of(2024, 5, 1),
                                                    faktiskTom = LocalDate.of(2024, 5, 1),
                                                    belop = -700,
                                                    sats = 700.0,
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
                rådata = simuleringRespons,
            )

        assertEquals(forventetOppsummering, OppsummeringGenerator.lagOppsummering(simuleringRespons))
    }

    @Test
    @Disabled
    fun `skal lage oppsummering for feilutbetaling`() {
        val simuleringRespons =
            SimuleringResponse(
                gjelderId = "22479409483",
                datoBeregnet = LocalDate.of(2024, 5, 28),
                totalBelop = 0,
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
                                        feilkonto = true,
                                        detaljer =
                                            listOf(
                                                Postering(
                                                    type = "YTEL",
                                                    faktiskFom = LocalDate.of(2024, 5, 1),
                                                    faktiskTom = LocalDate.of(2024, 5, 1),
                                                    belop = 100,
                                                    sats = 0.0,
                                                    satstype = "",
                                                    klassekode = "TSTBASISP4-OP",
                                                    trekkVedtakId = null,
                                                    refunderesOrgNr = null,
                                                ),
                                                Postering(
                                                    type = "YTEL",
                                                    faktiskFom = LocalDate.of(2024, 5, 1),
                                                    faktiskTom = LocalDate.of(2024, 5, 1),
                                                    belop = 600,
                                                    sats = 600.0,
                                                    satstype = "DAG",
                                                    klassekode = "TSTBASISP4-OP",
                                                    trekkVedtakId = null,
                                                    refunderesOrgNr = null,
                                                ),
                                                Postering(
                                                    type = "FEIL",
                                                    faktiskFom = LocalDate.of(2024, 5, 1),
                                                    faktiskTom = LocalDate.of(2024, 5, 1),
                                                    belop = 100,
                                                    sats = 0.0,
                                                    satstype = "",
                                                    klassekode = "KL_KODE_FEIL_ARBYT",
                                                    trekkVedtakId = null,
                                                    refunderesOrgNr = null,
                                                ),
                                                Postering(
                                                    type = "MOTP",
                                                    faktiskFom = LocalDate.of(2024, 5, 1),
                                                    faktiskTom = LocalDate.of(2024, 5, 1),
                                                    belop = -100,
                                                    sats = 0.0,
                                                    satstype = "",
                                                    klassekode = "TBMOTOBS",
                                                    trekkVedtakId = null,
                                                    refunderesOrgNr = null,
                                                ),
                                                Postering(
                                                    type = "YTEL",
                                                    faktiskFom = LocalDate.of(2024, 5, 1),
                                                    faktiskTom = LocalDate.of(2024, 5, 1),
                                                    belop = -700,
                                                    sats = 700.0,
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
                rådata = simuleringRespons,
            )

        assertEquals(forventetOppsummering, OppsummeringGenerator.lagOppsummering(simuleringRespons))
    }
}
