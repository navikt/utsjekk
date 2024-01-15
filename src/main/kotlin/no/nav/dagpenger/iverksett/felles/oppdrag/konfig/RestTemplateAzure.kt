package no.nav.dagpenger.iverksett.felles.oppdrag.konfig

import no.nav.dagpenger.iverksett.felles.http.ObjectMapperProvider.objectMapper
import no.nav.dagpenger.iverksett.felles.oppdrag.konfig.interceptor.BearerTokenClientInterceptor
import no.nav.dagpenger.iverksett.felles.oppdrag.konfig.interceptor.ConsumerIdClientInterceptor
import no.nav.dagpenger.iverksett.felles.oppdrag.konfig.interceptor.MdcValuesPropagatingClientInterceptor
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.time.temporal.ChronoUnit

@Suppress("SpringFacetCodeInspection")
@Configuration
@Import(
    ConsumerIdClientInterceptor::class,
    BearerTokenClientInterceptor::class,
)
class RestTemplateAzure {

    @Bean
    @Primary
    fun restTemplateBuilder(): RestTemplateBuilder {
        val jackson2HttpMessageConverter = MappingJackson2HttpMessageConverter(objectMapper)
        return RestTemplateBuilder()
                .setConnectTimeout(Duration.of(2, ChronoUnit.SECONDS))
                .setReadTimeout(Duration.of(30, ChronoUnit.SECONDS))
                .messageConverters(listOf(jackson2HttpMessageConverter) + RestTemplate().messageConverters)
    }

    @Bean("azure")
    fun restTemplateJwtBearer(
            restTemplateBuilder: RestTemplateBuilder,
            consumerIdClientInterceptor: ConsumerIdClientInterceptor,
            bearerTokenClientInterceptor: BearerTokenClientInterceptor
    ): RestOperations {
        return restTemplateBuilder.additionalInterceptors(
            consumerIdClientInterceptor,
            bearerTokenClientInterceptor,
            MdcValuesPropagatingClientInterceptor()
        ).build()
    }
}
