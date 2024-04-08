package no.nav.utsjekk.felles.oppdrag

import no.nav.utsjekk.felles.oppdrag.konfig.AbstractRestClient
import no.nav.utsjekk.kontrakter.oppdrag.GrensesnittavstemmingRequest
import no.nav.utsjekk.kontrakter.oppdrag.OppdragIdDto
import no.nav.utsjekk.kontrakter.oppdrag.OppdragStatusDto
import no.nav.utsjekk.kontrakter.oppdrag.Utbetalingsoppdrag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class OppdragClient(
    @Value("\${DP_OPPDRAG_API_URL}")
    private val dagpengerOppdragUri: URI,
    @Qualifier("azure")
    restOperations: RestOperations,
) : AbstractRestClient(restOperations, "dp.oppdrag") {
    private val postOppdragUri: URI =
        UriComponentsBuilder.fromUri(dagpengerOppdragUri).pathSegment("oppdrag").build().toUri()
    private val getStatusUri: URI =
        UriComponentsBuilder.fromUri(dagpengerOppdragUri).pathSegment("status").build().toUri()
    private val grensesnittavstemmingUri: URI =
        UriComponentsBuilder.fromUri(dagpengerOppdragUri).pathSegment("grensesnittavstemming").build().toUri()

    fun iverksettOppdrag(utbetalingsoppdrag: Utbetalingsoppdrag) {
        try {
            postForEntity<Unit>(postOppdragUri, utbetalingsoppdrag)
        } catch (e: HttpClientErrorException) {
            if (e.statusCode == HttpStatus.CONFLICT) {
                logger.warn(
                    "dp-oppdrag har allerede iverksatt utbetaling for fagsystem ${utbetalingsoppdrag.fagsystem}, sak: " +
                        "${utbetalingsoppdrag.saksnummer}, behandling: ${utbetalingsoppdrag.utbetalingsperiode.firstOrNull()?.behandlingId}" +
                        ", iverksetting: ${utbetalingsoppdrag.iverksettingId}",
                )
            } else {
                throw e
            }
        }
    }

    fun hentStatus(oppdragId: OppdragIdDto) = postForEntity<OppdragStatusDto>(getStatusUri, oppdragId)

    fun grensesnittavstemming(grensesnittavstemmingRequest: GrensesnittavstemmingRequest) =
        postForEntity<Unit>(grensesnittavstemmingUri, grensesnittavstemmingRequest)

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
