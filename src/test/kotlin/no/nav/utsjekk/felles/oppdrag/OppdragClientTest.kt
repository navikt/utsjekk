package no.nav.utsjekk.felles.oppdrag

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import no.nav.utsjekk.iverksetting.util.etTomtUtbetalingsoppdrag
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.boot.web.client.RestTemplateBuilder
import java.net.URI

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OppdragClientTest {
    val wiremockServer = WireMockServer()
    private lateinit var oppdragClient: OppdragClient

    @BeforeAll
    fun setup() {
        wiremockServer.start()
        oppdragClient = OppdragClient(URI.create(wiremockServer.baseUrl()), RestTemplateBuilder().build())
    }

    @AfterAll
    fun reset() {
        wiremockServer.stop()
    }

    @Test
    fun `skal ikke feile hvis utsjekk-oppdrag svarer med 409-feil`() {
        stubFor(post(urlEqualTo("/oppdrag")).willReturn(aResponse().withStatus(409)))

        assertDoesNotThrow { oppdragClient.iverksettOppdrag(etTomtUtbetalingsoppdrag()) }
    }
}
