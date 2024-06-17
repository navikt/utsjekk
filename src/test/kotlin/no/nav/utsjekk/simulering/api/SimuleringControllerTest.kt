package no.nav.utsjekk.simulering.api

import no.nav.utsjekk.Integrasjonstest
import no.nav.utsjekk.kontrakter.felles.Personident
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
import java.time.LocalDateTime

class SimuleringControllerTest : Integrasjonstest() {

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken())
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }

    @Test
    fun `ingen utbetalinger`() {
        val body = SimuleringRequestDto(
            sakId = "en-sakid",
            behandlingId = "en-behandlingId",
            personident = Personident("15507600333"),
            saksbehandler = "A123456",
            vedtakstidspunkt = LocalDateTime.now(),
            utbetalinger = emptyList()
        )

        val respons: ResponseEntity<Any> =
            restTemplate.exchange(
                localhostUrl("/api/simulering"),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )

        assertEquals(HttpStatus.BAD_REQUEST, respons.statusCode)
    }
}