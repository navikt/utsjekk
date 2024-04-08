package no.nav.utsjekk.konfig

import io.mockk.clearMocks
import io.mockk.every
import no.nav.utsjekk.felles.Profiler
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.utbetaling.featuretoggle.FeatureToggleConfig
import no.nav.utsjekk.utbetaling.util.mockFeatureToggleService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile(Profiler.INTEGRASJONSTEST)
@Configuration
class FeatureToggleMock {
    @Bean
    @Primary
    fun featureToggleService() = featureToggleService

    companion object {
        val featureToggleService = mockFeatureToggleService()

        fun skruAvFagsystem(fagsystem: Fagsystem) {
            every { featureToggleService.iverksettingErSkruddAvForFagsystem(fagsystem) } returns true
        }

        fun resetMock(enabled: Boolean = true) {
            clearMocks(featureToggleService)

            every { featureToggleService.isEnabled(any(), any()) } returns enabled
            every { featureToggleService.isEnabled(any<String>()) } returns enabled
            every { featureToggleService.iverksettingErSkruddAvForFagsystem(Fagsystem.DAGPENGER) } returns false
            every { featureToggleService.iverksettingErSkruddAvForFagsystem(Fagsystem.TILTAKSPENGER) } returns false
            every { featureToggleService.iverksettingErSkruddAvForFagsystem(Fagsystem.TILLEGGSSTØNADER) } returns false
            every { featureToggleService.isEnabled(FeatureToggleConfig.STOPP_IVERKSETTING_DAGPENGER) } answers { false }
            every { featureToggleService.isEnabled(FeatureToggleConfig.STOPP_IVERKSETTING_TILTAKSPENGER) } answers { false }
            every { featureToggleService.isEnabled(FeatureToggleConfig.STOPP_IVERKSETTING_TILLEGGSSTØNADER) } answers { false }
        }
    }
}
