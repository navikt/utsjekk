package no.nav.dagpenger.iverksett.avstemming

import no.nav.dagpenger.iverksett.Integrasjonstest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class AvstemmingControllerTest : Integrasjonstest() {
    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalClientCredentialsTestToken(accessAsApplication = true))
    }

    @Test
    internal fun `starte grensesnittavstemming gir 202 Accepted`() {
        val respons: ResponseEntity<Any> =
            restTemplate.exchange(
                localhostUrl("/intern/avstemming/start/DAGPENGER"),
                HttpMethod.POST,
                HttpEntity<Void>(headers),
            )

        assertEquals(HttpStatus.ACCEPTED, respons.statusCode)
    }
}
