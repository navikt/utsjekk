package no.nav.utsjekk.utbetaling

import no.nav.utsjekk.Integrasjonstest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.*
import java.time.LocalDate
import java.time.LocalDateTime

class UtbetalingControllerTest : Integrasjonstest() {

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalClientCredentialsTestToken(accessAsApplication = true))
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }

    @Test
    fun `kan opprette ny utbetaling`() {
        val body = UtbetalingDto(
            sakId = "123",
            behandlingId = "abc",
            personident = "",
            vedtak = UtbetalingDto.VedtakDto(
                vedtakstidspunkt = LocalDateTime.now(),
                saksbehandlerId = "",
                beslutterId = "",
                utbetalinger = listOf(
                    UtbetalingDto.VedtakDto.DagsatsDto(
                        beløp = 500u,
                        stønadstype = "SOME_KLASSE_KODE",
                        brukersNavKontor = "",
                        dato = LocalDate.now(),
                    )
                ),
            ),
        )

        val respons: ResponseEntity<Any> = restTemplate.exchange(
            localhostUrl("/api/utbetalinger"),
            HttpMethod.POST,
            HttpEntity(body, headers),
        )

        assertEquals(HttpStatus.CREATED, respons.statusCode)
    }
}












