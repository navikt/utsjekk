package no.nav.utsjekk.felles

import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.familie.prosessering.config.ProsesseringInfoProvider
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import no.nav.utsjekk.felles.http.ObjectMapperProvider
import no.nav.utsjekk.felles.http.filter.LogFilter
import no.nav.utsjekk.felles.http.filter.RequestTimeFilter
import no.nav.utsjekk.felles.oppdrag.konfig.RestTemplateAzure
import no.nav.utsjekk.felles.oppdrag.konfig.RetryOAuth2HttpClient
import no.nav.utsjekk.iverksetting.domene.KonsumentConfig
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
import org.springframework.web.client.RestClient
import java.time.Duration
import java.time.temporal.ChronoUnit

@SpringBootConfiguration
@ConfigurationPropertiesScan(
    "no.nav.utsjekk",
)
@ComponentScan(
    "no.nav.familie.prosessering",
    "no.nav.utsjekk",
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

    @Bean
    @Primary
    fun oAuth2HttpClient() =
        RetryOAuth2HttpClient(
            restClient =
                RestClient.create(
                    RestTemplateBuilder()
                        .setConnectTimeout(Duration.of(2, ChronoUnit.SECONDS))
                        .setReadTimeout(Duration.of(2, ChronoUnit.SECONDS))
                        .build(),
                ),
        )

    @Bean
    fun prosesseringInfoProvider(
        @Value("\${PROSESSERING_GRUPPE}") gruppe: String,
        @Value("\${PROSESSERING_ROLLE}") rolle: String,
    ) = object : ProsesseringInfoProvider {
        override fun hentBrukernavn(): String =
            try {
                claims()?.getStringClaim("preferred_username") ?: "utsjekk"
            } catch (e: Exception) {
                "utsjekk"
            }

        override fun harTilgang(): Boolean = grupper().contains(gruppe) || roller().contains(rolle)

        private fun grupper(): List<String> {
            return try {
                claims()?.getAsList("groups") ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        private fun roller(): List<String> {
            return try {
                claims()?.getAsList("roles") ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        private fun claims() =
            try {
                SpringTokenValidationContextHolder().getTokenValidationContext().getClaims("azuread")
            } catch (e: Exception) {
                null
            }

        override fun isLeader(): Boolean = LeaderClient.isLeader() ?: true
    }
}
