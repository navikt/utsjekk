package no.nav.dagpenger.iverksett.felles

import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.dagpenger.iverksett.felles.http.ObjectMapperProvider
import no.nav.dagpenger.iverksett.felles.oppdrag.konfig.RestTemplateAzure
import no.nav.dagpenger.iverksett.felles.oppdrag.konfig.RetryOAuth2HttpClient
import no.nav.dagpenger.iverksett.utbetaling.domene.KonsumentConfig
import no.nav.familie.log.filter.LogFilter
import no.nav.familie.log.filter.RequestTimeFilter
import no.nav.familie.prosessering.config.ProsesseringInfoProvider
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.Duration
import java.time.temporal.ChronoUnit

@SpringBootConfiguration
@ConfigurationPropertiesScan(
    "no.nav.dagpenger.iverksett",
)
@ComponentScan(
    "no.nav.familie.prosessering",
    "no.nav.dagpenger.iverksett",
    "no.nav.security.token.support",
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            value = [MappingJackson2XmlHttpMessageConverter::class],
        ),
    ],
)
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
@Import(
    RestTemplateAzure::class,
)
@EnableOAuth2Client(cacheEnabled = true)
@EnableScheduling
@EnableConfigurationProperties(KonsumentConfig::class)
class ApplicationConfig {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun kotlinModule(): KotlinModule = KotlinModule.Builder().build()

    @Bean
    fun logFilter() =
        FilterRegistrationBean<LogFilter>().apply {
            logger.info("Registering LogFilter filter")
            filter = LogFilter()
            order = 1
        }

    @Bean
    fun requestTimeFilter() =
        FilterRegistrationBean<RequestTimeFilter>().apply {
            logger.info("Registering RequestTimeFilter filter")
            filter = RequestTimeFilter()
            order = 2
        }

    @Bean
    @Primary
    fun objectMapper() = ObjectMapperProvider.objectMapper

    /**
     * Overskrever OAuth2HttpClient som settes opp i token-support som ikke kan få med objectMapper fra felles
     * pga .setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
     * og [OAuth2AccessTokenResponse] som burde settes med setters, då feltnavn heter noe annet enn feltet i json
     */
    @Bean
    @Primary
    fun oAuth2HttpClient() =
        RetryOAuth2HttpClient(
            RestTemplateBuilder()
                .setConnectTimeout(Duration.of(2, ChronoUnit.SECONDS))
                .setReadTimeout(Duration.of(2, ChronoUnit.SECONDS)),
        )

    @Bean
    fun prosesseringInfoProvider(
        @Value("\${PROSESSERING_GRUPPE}") gruppe: String,
    ) = object : ProsesseringInfoProvider {
        override fun hentBrukernavn(): String =
            try {
                SpringTokenValidationContextHolder().tokenValidationContext.getClaims("azuread").getStringClaim("preferred_username")
            } catch (e: Exception) {
                "dp-iverksett"
            }

        override fun harTilgang(): Boolean = grupper().contains(gruppe)

        private fun grupper(): List<String> {
            return try {
                SpringTokenValidationContextHolder().tokenValidationContext.getClaims("azuread")
                    ?.getAsList("groups") ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        override fun isLeader(): Boolean = LeaderClient.isLeader() ?: true
    }
}
