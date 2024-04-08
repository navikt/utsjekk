package no.nav.utsjekk.konfig

import io.mockk.every
import io.mockk.mockk
import no.nav.utsjekk.felles.Profiler
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile(Profiler.MOCK_OAUTH)
class OAuth2AccessTokenTestConfig {
    @Bean
    @Primary
    fun oAuth2AccessTokenServiceMock() =
        mockk<OAuth2AccessTokenService>().also {
            every { it.getAccessToken(any()) }
                .returns(OAuth2AccessTokenResponse("Mock-token-response", 60, 60))
        }
}
