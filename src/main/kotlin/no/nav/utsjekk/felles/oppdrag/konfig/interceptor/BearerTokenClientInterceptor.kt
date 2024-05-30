package no.nav.utsjekk.felles.oppdrag.konfig.interceptor

import com.nimbusds.oauth2.sdk.GrantType
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.utsjekk.iverksetting.api.TokenContext
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import java.net.URI

@Component
class BearerTokenClientInterceptor(
    private val oAuth2AccessTokenService: OAuth2AccessTokenService,
    private val clientConfigurationProperties: ClientConfigurationProperties,
) :
    ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        request.headers.setBearerAuth(
            genererAccessToken(
                request,
                clientConfigurationProperties,
                oAuth2AccessTokenService,
            ),
        )
        return execution.execute(request, body)
    }
}

private fun genererAccessToken(
    request: HttpRequest,
    clientConfigurationProperties: ClientConfigurationProperties,
    oAuth2AccessTokenService: OAuth2AccessTokenService,
): String {
    val clientProperties =
        clientPropertiesFor(
            request.uri,
            clientConfigurationProperties,
        )
    return oAuth2AccessTokenService.getAccessToken(clientProperties).accessToken
        ?: throw IllegalStateException("Klarte ikke hente access token for ${request.uri}")
}

private fun clientPropertiesFor(
    uri: URI,
    clientConfigurationProperties: ClientConfigurationProperties,
): ClientProperties {
    val clientProperties = filterClientProperties(clientConfigurationProperties, uri)
    return if (clientProperties.size == 1) {
        clientProperties.first()
    } else {
        clientPropertiesForGrantType(clientProperties, clientCredentialOrJwtBearer(), uri)
    }
}

private fun filterClientProperties(
    clientConfigurationProperties: ClientConfigurationProperties,
    uri: URI,
) = clientConfigurationProperties
    .registration
    .values
    .filter { uri.toString().startsWith(it.resourceUrl.toString()) }

private fun clientPropertiesForGrantType(
    values: List<ClientProperties>,
    grantType: GrantType,
    uri: URI,
): ClientProperties {
    return values.firstOrNull { grantType == it.grantType }
        ?: error("Fant ikke Oauth2 klient-config for uri=$uri og grant type=$grantType")
}

private fun clientCredentialOrJwtBearer() = if (TokenContext.erSystemkontekst()) GrantType.CLIENT_CREDENTIALS else GrantType.JWT_BEARER
