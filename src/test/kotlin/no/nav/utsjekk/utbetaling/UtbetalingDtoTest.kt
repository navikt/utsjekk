package no.nav.utsjekk.utbetaling

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.utsjekk.felles.http.advice.ApiFeil
import no.nav.utsjekk.kontrakter.felles.objectMapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class UtbetalingDtoTest {


    @Test
    fun `kan sende inn engangsutbetaling`() {
        @Language("JSON")
        val json = """
            {
              "sakId": "string",
              "behandlingId": "string",
              "personident": "string",
              "vedtak": {
                "vedtakstidspunkt": "2024-07-02T13:56:46.124112",
                "saksbehandlerId": "string",
                "beslutterId": "string",
                "utbetalinger": [
                  {
                    "beløp": 10234,
                    "type": "ENGANGS",
                    "fom": "2024-07-01",
                    "tom": "2024-07-20",
                    "stønadstype": "string",
                    "brukersNavKontor": "1234"
                  }
                ]
              }
            }
        """.trimIndent()
        assertDoesNotThrow {
            objectMapper.writeValueAsString(objectMapper.readValue<UtbetalingDto>(json))
        }
    }

    @Test
    fun `kan sende inn ubetaling for mnd`() {
        @Language("JSON")
        val json = """
            {
              "sakId": "string",
              "behandlingId": "string",
              "personident": "string",
              "vedtak": {
                "vedtakstidspunkt": "2024-07-02T13:56:46.124112",
                "saksbehandlerId": "string",
                "beslutterId": "string",
                "utbetalinger": [
                  {
                    "beløp": 10234,
                    "type": "MÅNEDLIG",
                    "måned": "2020-03",
                    "stønadstype": "string",
                    "brukersNavKontor": "1234"
                  }
                ]
              }
            }
        """.trimIndent()
        assertDoesNotThrow {
            objectMapper.writeValueAsString(objectMapper.readValue<UtbetalingDto>(json))
        }
    }

    @Test
    fun `kan sende inn utbetaling for dag`() {
        @Language("JSON")
        val json = """
            {
              "sakId": "string",
              "behandlingId": "string",
              "personident": "string",
              "vedtak": {
                "vedtakstidspunkt": "2024-07-02T13:56:46.124112",
                "saksbehandlerId": "string",
                "beslutterId": "string",
                "utbetalinger": [
                  {
                    "beløp": 1234,
                    "type": "DAGLIG",
                    "dato": "2020-03-01",
                    "stønadstype": "string",
                    "brukersNavKontor": "1234"
                  }
                ]
              }
            }
        """.trimIndent()

        assertDoesNotThrow {
            objectMapper.writeValueAsString(objectMapper.readValue<UtbetalingDto>(json))
        }
    }

    @Test
    fun `støtter ikke overlapp`() {
        assertThrows<ApiFeil> {
            UtbetalingDto(
                sakId = "string",
                behandlingId = "string",
                personident = "string",
                vedtak = UtbetalingDto.VedtakDto(
                    vedtakstidspunkt = LocalDateTime.now(),
                    saksbehandlerId = "string",
                    beslutterId = "string",
                    utbetalinger = listOf(
                        UtbetalingDto.VedtakDto.DagsatsDto(
                            beløp = 1234u,
                            dato = LocalDate.of(2020, 3, 1),
                            stønadstype = Stønadstype.Tilleggsstønader.TILSYN_BARN_AAP.navn(),
                            brukersNavKontor = "1234"
                        ),
                        UtbetalingDto.VedtakDto.MånedsatsDto(
                            beløp = 1234u,
                            måned = YearMonth.of(2020, 3),
                            stønadstype = Stønadstype.Tilleggsstønader.TILSYN_BARN_AAP.navn(),
                            brukersNavKontor = "1234"
                        ),
                        UtbetalingDto.VedtakDto.EngangssatsDto(
                            beløp = 1234u,
                            fom = LocalDate.of(2020, 1, 1),
                            tom = LocalDate.of(2020, 1, 21),
                            stønadstype = Stønadstype.Tilleggsstønader.TILSYN_BARN_AAP.navn(),
                            brukersNavKontor = "1234"
                        ),
                    )
                )
            )
        }
    }
}