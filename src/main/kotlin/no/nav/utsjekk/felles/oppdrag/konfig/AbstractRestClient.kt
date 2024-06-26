package no.nav.utsjekk.felles.oppdrag.konfig

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * Abstract klasse for å kalle rest-tjenester med metrics og utpakking av ev. body.
 */
abstract class AbstractRestClient(
    val operations: RestOperations,
    metricsPrefix: String,
) {
    private val responstid: Timer = Metrics.timer("$metricsPrefix.tid")
    private val responsSuccess: Counter = Metrics.counter("$metricsPrefix.response", "status", "success")
    private val responsFailure: Counter = Metrics.counter("$metricsPrefix.response", "status", "failure")

    private val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")
    protected val log: Logger = LoggerFactory.getLogger(this::class.java)

    inline fun <reified T : Any> postForEntity(
        uri: URI,
        payload: Any,
        httpHeaders: HttpHeaders? = null,
    ) = executeMedMetrics(uri) { operations.exchange<T>(uri, HttpMethod.POST, HttpEntity(payload, httpHeaders)) }

    private fun <T> validerOgPakkUt(
        respons: ResponseEntity<T>,
        uri: URI,
    ): T {
        if (!respons.statusCode.is2xxSuccessful) {
            secureLogger.info("Kall mot $uri feilet:  ${respons.body}")
            log.info("Kall mot $uri feilet: ${respons.statusCode}")

            throw HttpServerErrorException(respons.statusCode, "", respons.body?.toString()?.toByteArray(), Charsets.UTF_8)
        }

        @Suppress("UNCHECKED_CAST")
        return respons.body as T
    }

    fun <T> executeMedMetrics(
        uri: URI,
        function: () -> ResponseEntity<T>,
    ): T {
        try {
            val startTime = System.nanoTime()
            val responseEntity = function.invoke()
            responstid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
            responsSuccess.increment()
            return validerOgPakkUt(responseEntity, uri)
        } catch (e: Exception) {
            responsFailure.increment()
            secureLogger.warn("Feil ved kall mot uri=$uri", e)
            throw e
        }
    }

    override fun toString(): String = this::class.simpleName + " [operations=" + operations + "]"
}
