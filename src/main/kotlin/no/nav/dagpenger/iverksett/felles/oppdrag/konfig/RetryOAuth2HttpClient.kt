package no.nav.dagpenger.iverksett.felles.oppdrag.konfig

import no.nav.security.token.support.client.core.http.OAuth2HttpRequest
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.spring.oauth2.DefaultOAuth2HttpClient
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.NestedExceptionUtils
import org.springframework.web.client.HttpServerErrorException
import java.net.SocketException
import java.net.SocketTimeoutException

class RetryOAuth2HttpClient(
    restTemplateBuilder: RestTemplateBuilder,
    private val maxRetries: Int = 2,
) : DefaultOAuth2HttpClient(restTemplateBuilder) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    private val retryExceptions =
        setOf(
            SocketException::class,
            SocketTimeoutException::class,
            HttpServerErrorException.GatewayTimeout::class,
            HttpServerErrorException.BadGateway::class,
        )

    override fun post(req: OAuth2HttpRequest): OAuth2AccessTokenResponse? {
        var retries = 0

        while (true) {
            try {
                return super.post(req)
            } catch (e: Exception) {
                handleException(e, retries++, req)
            }
        }
    }

    private fun handleException(
        e: Exception,
        retries: Int,
        oAuth2HttpRequest: OAuth2HttpRequest,
    ) {
        if (shouldRetry(e) && retries < maxRetries) {
            logger.warn(
                "Kall mot url=${oAuth2HttpRequest.tokenEndpointUrl} feilet, cause=${
                    NestedExceptionUtils.getMostSpecificCause(e)::class
                }",
            )
            secureLogger.warn("Kall mot url=${oAuth2HttpRequest.tokenEndpointUrl} feilet med feil=${e.message}")
        } else {
            throw e
        }
    }

    private fun shouldRetry(e: Exception): Boolean {
        return retryExceptions.contains(e.cause?.let { it::class })
    }
}
