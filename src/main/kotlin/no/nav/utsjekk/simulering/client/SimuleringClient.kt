package no.nav.utsjekk.simulering.client

import no.nav.utsjekk.felles.oppdrag.konfig.AbstractRestClient
import no.nav.utsjekk.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.utsjekk.simulering.client.dto.Mapper.tilSimuleringRequest
import no.nav.utsjekk.simulering.client.dto.SimuleringResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class SimuleringClient(
    @Value("\${UTSJEKK_SIMULERING_API_URL}")
    private val utsjekkSimuleringUri: URI,
    @Qualifier("azure")
    restOperations: RestOperations,
) : AbstractRestClient(restOperations, "utsjekk.simulering") {
    private val postSimuleringUri: URI =
        UriComponentsBuilder.fromUri(utsjekkSimuleringUri).pathSegment("simulering").build().toUri()

    fun hentSimulering(utbetalingsoppdrag: Utbetalingsoppdrag): SimuleringResponse {
        try {
            secureLogger.info("Prøver å simulere utbetalingsoppdrag: $utbetalingsoppdrag")
            return postForEntity<SimuleringResponse>(postSimuleringUri, utbetalingsoppdrag.tilSimuleringRequest())
        } catch (e: Throwable) {
            logger.warn("Feil fra simulering")
            secureLogger.warn("Feil fra simulering:", e)
            throw e
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}
