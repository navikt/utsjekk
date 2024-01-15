package no.nav.dagpenger.iverksett.felles

import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.dagpenger.iverksett.felles.leaderelection.LeaderClient
import no.nav.dagpenger.iverksett.felles.http.ObjectMapperProvider
import no.nav.dagpenger.iverksett.felles.oppdrag.konfig.RestTemplateAzure
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
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.scheduling.annotation.EnableScheduling

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
class ApplicationConfig {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun kotlinModule(): KotlinModule = KotlinModule.Builder().build()

    @Bean
    fun logFilter(): FilterRegistrationBean<LogFilter> {
        logger.info("Registering LogFilter filter")
        val filterRegistration = FilterRegistrationBean<LogFilter>()
        filterRegistration.filter = LogFilter()
        filterRegistration.order = 1
        return filterRegistration
    }

    @Bean
    fun requestTimeFilter(): FilterRegistrationBean<RequestTimeFilter> {
        logger.info("Registering RequestTimeFilter filter")
        val filterRegistration = FilterRegistrationBean<RequestTimeFilter>()
        filterRegistration.filter = RequestTimeFilter()
        filterRegistration.order = 2
        return filterRegistration
    }

    @Bean
    @Primary
    fun objectMapper() = ObjectMapperProvider.objectMapper

    @Bean
    fun prosesseringInfoProvider(
        @Value("\${PROSESSERING_GRUPPE}") gruppe: String,
    ) = object : ProsesseringInfoProvider {
        override fun hentBrukernavn(): String = try {
            SpringTokenValidationContextHolder().tokenValidationContext.getClaims("azuread").getStringClaim("preferred_username")
        } catch (e: Exception) {
            "dp-iverksett"
        }

        override fun harTilgang(): Boolean = grupper().contains(gruppe)

        private fun grupper(): List<String> {
            return try {
                SpringTokenValidationContextHolder().tokenValidationContext.getClaims("azuread")
                    ?.get("groups") as List<String>? ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        override fun isLeader(): Boolean = LeaderClient.isLeader() ?: true
    }
}
