package no.nav.utsjekk.simulering.domene

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.objectMapper
import no.nav.utsjekk.simulering.api.OppsummeringForPeriode
import no.nav.utsjekk.simulering.api.SimuleringResponsDto
import no.nav.utsjekk.simulering.client.dto.Mapper.tilSimuleringDetaljer
import no.nav.utsjekk.simulering.client.dto.SimuleringResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OppsummeringGeneratorTest {
    companion object {
        private const val KLASSEKODE = "TSTBASISP4-OP"
        private const val SAK_ID = "200000237"
        private const val PERSONIDENT = "22479409483"
        private val DATO_1_MARS = LocalDate.of(2024, 3, 1)
        private val DATO_1_APRIL = LocalDate.of(2024, 4, 1)
        private val DATO_1_MAI = LocalDate.of(2024, 5, 1)
        private val DATO_28_MAI = LocalDate.of(2024, 5, 28)
    }

    @Test
    fun `bugfiks 11-09-2024 - oppsummering av ny utbetaling`() {
        val json = """
            {"gjelderId":"x","datoBeregnet":"2024-09-11","totalBelop":15728,"perioder":[{"fom":"2024-09-02","tom":"2024-09-02","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000363","utbetalesTilId":"x","forfall":"2024-09-11","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2024-09-02","faktiskTom":"2024-09-02","belop":1861,"sats":1861.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null},{"type":"FEIL","faktiskFom":"2024-09-02","faktiskTom":"2024-09-02","belop":1550,"sats":0.0,"satstype":null,"klassekode":"KL_KODE_JUST_ARBYT","trekkVedtakId":null,"refunderesOrgNr":null},{"type":"YTEL","faktiskFom":"2024-09-02","faktiskTom":"2024-09-02","belop":-3411,"sats":3411.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2024-10-01","tom":"2024-10-01","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000363","utbetalesTilId":"x","forfall":"2024-09-11","feilkonto":false,"detaljer":[{"type":"FEIL","faktiskFom":"2024-10-01","faktiskTom":"2024-10-01","belop":-1550,"sats":0.0,"satstype":null,"klassekode":"KL_KODE_JUST_ARBYT","trekkVedtakId":null,"refunderesOrgNr":null},{"type":"YTEL","faktiskFom":"2024-10-01","faktiskTom":"2024-10-01","belop":2038,"sats":2038.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2024-11-01","tom":"2024-11-01","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000363","utbetalesTilId":"x","forfall":"2024-09-11","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2024-11-01","faktiskTom":"2024-11-01","belop":1861,"sats":1861.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2024-12-02","tom":"2024-12-02","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000363","utbetalesTilId":"x","forfall":"2024-09-11","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2024-12-02","faktiskTom":"2024-12-02","belop":1949,"sats":1949.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2025-01-01","tom":"2025-01-01","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000363","utbetalesTilId":"x","forfall":"2024-09-11","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2025-01-01","faktiskTom":"2025-01-01","belop":2038,"sats":2038.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2025-02-03","tom":"2025-02-03","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000363","utbetalesTilId":"x","forfall":"2024-09-11","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2025-02-03","faktiskTom":"2025-02-03","belop":1772,"sats":1772.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2025-03-03","tom":"2025-03-03","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000363","utbetalesTilId":"x","forfall":"2024-09-11","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2025-03-03","faktiskTom":"2025-03-03","belop":1861,"sats":1861.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2025-04-01","tom":"2025-04-01","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000363","utbetalesTilId":"x","forfall":"2024-09-11","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2025-04-01","faktiskTom":"2025-04-01","belop":1949,"sats":1949.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2025-05-01","tom":"2025-05-01","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000363","utbetalesTilId":"x","forfall":"2024-09-11","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2025-05-01","faktiskTom":"2025-05-01","belop":1949,"sats":1949.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2025-06-02","tom":"2025-06-02","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000363","utbetalesTilId":"x","forfall":"2024-09-11","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2025-06-02","faktiskTom":"2025-06-02","belop":1861,"sats":1861.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]}]}
        """.trimIndent()
        val detaljer =
            objectMapper.readValue<SimuleringResponse>(json).tilSimuleringDetaljer(Fagsystem.TILLEGGSSTØNADER)
        val oppsummering = OppsummeringGenerator.lagOppsummering(detaljer)

        val september = oppsummering.oppsummeringer.minBy { it.fom }
        assertEquals(1861, september.nyUtbetaling)
        assertEquals(3411, september.tidligereUtbetalt)
        assertEquals(0, september.totalEtterbetaling)
        assertEquals(1550, september.totalFeilutbetaling)
    }

    @Test
    fun `bugfiks 10-09-2024 - oppsummering av ny utbetaling`() {
        val json = """
            {"gjelderId":"x","datoBeregnet":"2024-09-10","totalBelop":10278,"perioder":[{"fom":"2024-09-02","tom":"2024-09-02","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000357","utbetalesTilId":"x","forfall":"2024-09-10","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2024-09-02","faktiskTom":"2024-09-02","belop":1240,"sats":1240.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null},{"type":"FEIL","faktiskFom":"2024-09-02","faktiskTom":"2024-09-02","belop":1241,"sats":0.0,"satstype":null,"klassekode":"KL_KODE_JUST_ARBYT","trekkVedtakId":null,"refunderesOrgNr":null},{"type":"YTEL","faktiskFom":"2024-09-02","faktiskTom":"2024-09-02","belop":-2481,"sats":2481.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2024-10-01","tom":"2024-10-01","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000357","utbetalesTilId":"x","forfall":"2024-09-10","feilkonto":false,"detaljer":[{"type":"FEIL","faktiskFom":"2024-10-01","faktiskTom":"2024-10-01","belop":-1241,"sats":0.0,"satstype":null,"klassekode":"KL_KODE_JUST_ARBYT","trekkVedtakId":null,"refunderesOrgNr":null},{"type":"YTEL","faktiskFom":"2024-10-01","faktiskTom":"2024-10-01","belop":1359,"sats":1359.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2024-11-01","tom":"2024-11-01","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000357","utbetalesTilId":"x","forfall":"2024-09-10","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2024-11-01","faktiskTom":"2024-11-01","belop":1240,"sats":1240.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2024-12-02","tom":"2024-12-02","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000357","utbetalesTilId":"x","forfall":"2024-09-10","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2024-12-02","faktiskTom":"2024-12-02","belop":1300,"sats":1300.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2025-01-01","tom":"2025-01-01","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000357","utbetalesTilId":"x","forfall":"2024-09-10","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2025-01-01","faktiskTom":"2025-01-01","belop":1359,"sats":1359.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2025-02-03","tom":"2025-02-03","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000357","utbetalesTilId":"x","forfall":"2024-09-10","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2025-02-03","faktiskTom":"2025-02-03","belop":1181,"sats":1181.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2025-03-03","tom":"2025-03-03","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000357","utbetalesTilId":"x","forfall":"2024-09-10","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2025-03-03","faktiskTom":"2025-03-03","belop":1240,"sats":1240.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2025-04-01","tom":"2025-04-01","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000357","utbetalesTilId":"x","forfall":"2024-09-10","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2025-04-01","faktiskTom":"2025-04-01","belop":1300,"sats":1300.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2025-05-01","tom":"2025-05-01","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000357","utbetalesTilId":"x","forfall":"2024-09-10","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2025-05-01","faktiskTom":"2025-05-01","belop":1300,"sats":1300.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]},{"fom":"2025-06-02","tom":"2025-06-02","utbetalinger":[{"fagområde":"TILLST","fagSystemId":"200000357","utbetalesTilId":"x","forfall":"2024-09-10","feilkonto":false,"detaljer":[{"type":"YTEL","faktiskFom":"2025-06-02","faktiskTom":"2025-06-02","belop":1240,"sats":1240.0,"satstype":"DAG","klassekode":"TSTBASISP4-OP","trekkVedtakId":null,"refunderesOrgNr":null}]}]}]}
        """.trimIndent()
        val detaljer =
            objectMapper.readValue<SimuleringResponse>(json).tilSimuleringDetaljer(Fagsystem.TILLEGGSSTØNADER)
        val oppsummering = OppsummeringGenerator.lagOppsummering(detaljer)

        val september = oppsummering.oppsummeringer.minBy { it.fom }
        assertEquals(1240, september.nyUtbetaling)
        assertEquals(2481, september.tidligereUtbetalt)
        assertEquals(0, september.totalEtterbetaling)
        assertEquals(1241, september.totalFeilutbetaling)
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
                        posteringerForNyUtbetaling(fom = fremtidigPeriode, tom = fremtidigPeriode),
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
                        posteringer = posteringerForEtterbetaling(fom = DATO_1_MAI, tom = DATO_1_MAI),
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
                        posteringer = posteringerForFeilutbetaling(fom = DATO_1_MAI, tom = DATO_1_MAI),
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

    @Test
    fun `skal lage oppsummering for flere perioder`() {
        val fremtidigPeriode = LocalDate.now().plusMonths(2)
        val simuleringDetaljer =
            SimuleringDetaljer(
                gjelderId = PERSONIDENT,
                datoBeregnet = DATO_28_MAI,
                totalBeløp = 900,
                perioder =
                listOf(
                    Periode(
                        fom = DATO_1_MARS,
                        tom = DATO_1_MARS,
                        posteringer = posteringerForFeilutbetaling(fom = DATO_1_MARS, tom = DATO_1_MARS),
                    ),
                    Periode(
                        fom = DATO_1_APRIL,
                        tom = DATO_1_APRIL,
                        posteringer = posteringerForEtterbetaling(fom = DATO_1_APRIL, tom = DATO_1_APRIL),
                    ),
                    Periode(
                        fom = fremtidigPeriode,
                        tom = fremtidigPeriode,
                        posteringer = posteringerForNyUtbetaling(fom = fremtidigPeriode, tom = fremtidigPeriode),
                    ),
                ),
            )

        val forventetOppsummering =
            SimuleringResponsDto(
                oppsummeringer =
                listOf(
                    OppsummeringForPeriode(
                        fom = DATO_1_MARS,
                        tom = DATO_1_MARS,
                        tidligereUtbetalt = 700,
                        nyUtbetaling = 600,
                        totalEtterbetaling = 0,
                        totalFeilutbetaling = 100,
                    ),
                    OppsummeringForPeriode(
                        fom = DATO_1_APRIL,
                        tom = DATO_1_APRIL,
                        tidligereUtbetalt = 700,
                        nyUtbetaling = 800,
                        totalEtterbetaling = 100,
                        totalFeilutbetaling = 0,
                    ),
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

    private fun posteringerForNyUtbetaling(
        fom: LocalDate,
        tom: LocalDate,
    ) = listOf(
        Postering(
            fagområde = Fagområde.TILLEGGSSTØNADER,
            sakId = SAK_ID,
            fom = fom,
            tom = tom,
            beløp = 800,
            type = PosteringType.YTELSE,
            klassekode = KLASSEKODE,
        ),
    )

    private fun posteringerForEtterbetaling(
        fom: LocalDate,
        tom: LocalDate,
    ) = listOf(
        Postering(
            type = PosteringType.YTELSE,
            fom = fom,
            tom = tom,
            beløp = 800,
            fagområde = Fagområde.TILLEGGSSTØNADER,
            sakId = SAK_ID,
            klassekode = KLASSEKODE,
        ),
        Postering(
            type = PosteringType.YTELSE,
            fom = fom,
            tom = tom,
            beløp = -700,
            fagområde = Fagområde.TILLEGGSSTØNADER,
            sakId = SAK_ID,
            klassekode = KLASSEKODE,
        ),
    )

    private fun posteringerForFeilutbetaling(
        fom: LocalDate,
        tom: LocalDate,
    ) = listOf(
        Postering(
            type = PosteringType.YTELSE,
            fom = fom,
            tom = tom,
            beløp = 100,
            fagområde = Fagområde.TILLEGGSSTØNADER,
            sakId = SAK_ID,
            klassekode = KLASSEKODE,
        ),
        Postering(
            type = PosteringType.YTELSE,
            fom = fom,
            tom = tom,
            beløp = 600,
            fagområde = Fagområde.TILLEGGSSTØNADER,
            sakId = SAK_ID,
            klassekode = KLASSEKODE,
        ),
        Postering(
            type = PosteringType.FEILUTBETALING,
            fom = fom,
            tom = tom,
            beløp = 100,
            fagområde = Fagområde.TILLEGGSSTØNADER,
            sakId = SAK_ID,
            klassekode = KLASSEKODE,
        ),
        Postering(
            type = PosteringType.MOTPOSTERING,
            fom = fom,
            tom = tom,
            beløp = -100,
            fagområde = Fagområde.TILLEGGSSTØNADER,
            sakId = SAK_ID,
            klassekode = KLASSEKODE,
        ),
        Postering(
            type = PosteringType.YTELSE,
            fom = fom,
            tom = tom,
            beløp = -700,
            fagområde = Fagområde.TILLEGGSSTØNADER,
            sakId = SAK_ID,
            klassekode = KLASSEKODE,
        ),
    )
}
