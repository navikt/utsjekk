package no.nav.utsjekk.simulering.client

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.utsjekk.iverksetting.util.etTomtUtbetalingsoppdrag
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import java.net.URI

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimuleringClientTest {
    val wiremockServer = WireMockServer()
    private lateinit var simuleringClient: SimuleringClient

    @BeforeAll
    fun setup() {
        wiremockServer.start()
        simuleringClient = SimuleringClient(URI.create(wiremockServer.baseUrl()), RestTemplateBuilder().build())
    }

    @AfterAll
    fun reset() {
        wiremockServer.stop()
    }

    @Test
    fun `Kaster feil når Oppdrag er nede og svarer med 503`() {
        WireMock.stubFor(
            WireMock.post(WireMock.urlEqualTo("/simulering")).willReturn(WireMock.aResponse().withStatus(503)),
        )

        assertThrows<HttpServerErrorException> { simuleringClient.hentSimulering(etTomtUtbetalingsoppdrag()) }
    }

    @Test
    fun `Kaster feil når oppdraget finnes fra før og svarer med 409`() {
        WireMock.stubFor(
            WireMock.post(WireMock.urlEqualTo("/simulering")).willReturn(WireMock.aResponse().withStatus(409)),
        )

        assertThrows<HttpClientErrorException> { simuleringClient.hentSimulering(etTomtUtbetalingsoppdrag()) }
    }
}
