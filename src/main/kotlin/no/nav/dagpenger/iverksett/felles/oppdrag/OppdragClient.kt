package no.nav.dagpenger.iverksett.felles.oppdrag

import no.nav.dagpenger.iverksett.felles.oppdrag.konfig.AbstractRestClient
import no.nav.dagpenger.kontrakter.oppdrag.GrensesnittavstemmingRequest
import no.nav.dagpenger.kontrakter.oppdrag.OppdragId
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatusDto
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsoppdrag
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
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

    fun iverksettOppdrag(utbetalingsoppdrag: Utbetalingsoppdrag) = postForEntity<Unit>(postOppdragUri, utbetalingsoppdrag)

    fun hentStatus(oppdragId: OppdragId) = postForEntity<OppdragStatusDto>(getStatusUri, oppdragId)

    fun grensesnittavstemming(grensesnittavstemmingRequest: GrensesnittavstemmingRequest) =
        postForEntity<Unit>(grensesnittavstemmingUri, grensesnittavstemmingRequest)
}
