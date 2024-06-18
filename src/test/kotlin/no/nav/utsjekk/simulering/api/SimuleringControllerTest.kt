package no.nav.utsjekk.simulering.api

import no.nav.utsjekk.Integrasjonstest
import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.felles.Satstype
import no.nav.utsjekk.kontrakter.felles.StønadTypeTilleggsstønader
import no.nav.utsjekk.kontrakter.iverksett.ForrigeIverksettingTilleggsstønaderDto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingTilleggsstønaderDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.time.LocalDate
import java.time.LocalDateTime

class SimuleringControllerTest : Integrasjonstest() {
    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken())
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }

    @Test
    fun `endring på ikke-eksisterende utbetaling`() {
        val body =
            SimuleringRequestTilleggsstønaderDto(
                sakId = "en-sakid",
                behandlingId = "en-behandlingId",
                personident = Personident("15507600333"),
                saksbehandler = "A123456",
                vedtakstidspunkt = LocalDateTime.now(),
                utbetalinger =
                    listOf(
                        UtbetalingTilleggsstønaderDto(
                            beløp = 100,
                            satstype = Satstype.DAGLIG,
                            fraOgMedDato = LocalDate.of(2024, 4, 2),
                            tilOgMedDato = LocalDate.of(2024, 4, 10),
                            stønadstype = StønadTypeTilleggsstønader.TILSYN_BARN_AAP,
                        ),
                    ),
                forrigeIverksetting =
                    ForrigeIverksettingTilleggsstønaderDto(
                        behandlingId = "noe-tull",
                        iverksettingId = "noe-tull",
                    ),
            )

        val respons: ResponseEntity<Any> =
            restTemplate.exchange(
                localhostUrl("/api/simulering/tilleggsstonader"),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )

        assertEquals(HttpStatus.BAD_REQUEST, respons.statusCode)
    }
}
