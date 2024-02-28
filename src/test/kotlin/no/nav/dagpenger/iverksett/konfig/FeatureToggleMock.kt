package no.nav.dagpenger.iverksett.konfig

import no.nav.dagpenger.iverksett.felles.Profiler
import no.nav.dagpenger.iverksett.felles.util.mockFeatureToggleService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile(Profiler.INTEGRASJONSTEST)
@Configuration
class FeatureToggleMock {
    @Bean
    @Primary
    fun featureToggleService() = mockFeatureToggleService()
}
