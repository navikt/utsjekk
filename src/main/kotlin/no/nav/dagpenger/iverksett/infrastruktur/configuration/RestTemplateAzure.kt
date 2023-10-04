package no.nav.dagpenger.iverksett.infrastruktur.configuration

import no.nav.dagpenger.iverksett.infrastruktur.interceptor.BearerTokenClientCredentialsClientInterceptor
import no.nav.dagpenger.iverksett.infrastruktur.interceptor.BearerTokenClientInterceptor
import no.nav.dagpenger.iverksett.infrastruktur.interceptor.BearerTokenOnBehalfOfClientInterceptor
import no.nav.dagpenger.iverksett.infrastruktur.interceptor.ConsumerIdClientInterceptor
import no.nav.dagpenger.iverksett.infrastruktur.interceptor.MdcValuesPropagatingClientInterceptor
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.client.RestOperations

@Suppress("SpringFacetCodeInspection")
@Configuration
@Import(
    RestTemplateBuilderBean::class,
    ConsumerIdClientInterceptor::class,
    BearerTokenClientInterceptor::class,
    BearerTokenClientCredentialsClientInterceptor::class,
    BearerTokenOnBehalfOfClientInterceptor::class
)
class RestTemplateAzure {

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

    @Bean("azureClientCredential")
    fun restTemplateClientCredentialBearer(
        restTemplateBuilder: RestTemplateBuilder,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
        bearerTokenClientInterceptor: BearerTokenClientCredentialsClientInterceptor
    ): RestOperations {
        return restTemplateBuilder.additionalInterceptors(
            consumerIdClientInterceptor,
            bearerTokenClientInterceptor,
            MdcValuesPropagatingClientInterceptor()
        ).build()
    }

    @Bean("azureOnBehalfOf")
    fun restTemplateOnBehalfOfBearer(
        restTemplateBuilder: RestTemplateBuilder,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
        bearerTokenClientInterceptor: BearerTokenOnBehalfOfClientInterceptor
    ): RestOperations {
        return restTemplateBuilder.additionalInterceptors(
            consumerIdClientInterceptor,
            bearerTokenClientInterceptor,
            MdcValuesPropagatingClientInterceptor()
        ).build()
    }
}
