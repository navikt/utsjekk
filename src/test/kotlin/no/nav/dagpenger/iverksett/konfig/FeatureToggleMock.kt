package no.nav.dagpenger.iverksett.config

import no.nav.dagpenger.iverksett.utbetaling.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.util.mockFeatureToggleService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("servertest")
@Configuration
class FeatureToggleMock {

    @Bean
    @Primary
    fun featureToggleService(): FeatureToggleService {
        return mockFeatureToggleService()
    }
}
