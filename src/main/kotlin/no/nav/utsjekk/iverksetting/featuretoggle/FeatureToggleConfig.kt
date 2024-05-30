package no.nav.utsjekk.iverksetting.featuretoggle

import io.getunleash.DefaultUnleash
import io.getunleash.UnleashContext
import io.getunleash.UnleashContextProvider
import io.getunleash.util.UnleashConfig
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import java.net.URI

@ConfigurationProperties("funksjonsbrytere")
class FeatureToggleConfig(
    private val enabled: Boolean,
    private val unleash: Unleash,
) {
    data class Unleash(
        val uri: URI,
        val apiKey: String,
        val environment: String,
        val applicationName: String,
    )

    @Bean
    fun featureToggle(): FeatureToggleService =
        if (enabled) {
            log.info("Funksjonsbryter-funksjonalitet er skrudd PÅ")
            lagUnleashFeatureToggleService()
        } else {
            log.warn("Funksjonsbryter-funksjonalitet er skrudd AV. Gir standardoppførsel for alle funksjonsbrytere, dvs 'false'")
            lagDummyFeatureToggleService()
        }

    private fun lagUnleashFeatureToggleService(): FeatureToggleService {
        val unleash =
            DefaultUnleash(
                UnleashConfig.builder()
                    .appName(unleash.applicationName)
                    .unleashAPI("${unleash.uri}/api/")
                    .apiKey(unleash.apiKey)
                    .unleashContextProvider(lagUnleashContextProvider())
                    .build(),
                ByEnvironmentStrategy(),
            )

        return object : FeatureToggleService {
            override fun isEnabled(
                toggleId: String,
                defaultValue: Boolean,
            ) = unleash.isEnabled(toggleId, defaultValue)

            override fun iverksettingErSkruddAvForFagsystem(fagsystem: Fagsystem) =
                when (fagsystem) {
                    Fagsystem.DAGPENGER -> unleash.isEnabled(STOPP_IVERKSETTING_DAGPENGER)
                    Fagsystem.TILTAKSPENGER -> unleash.isEnabled(STOPP_IVERKSETTING_TILTAKSPENGER)
                    Fagsystem.TILLEGGSSTØNADER -> unleash.isEnabled(STOPP_IVERKSETTING_TILLEGGSSTØNADER)
                }
        }
    }

    private fun lagUnleashContextProvider() =
        UnleashContextProvider {
            UnleashContext.builder()
                .environment(unleash.environment)
                .appName(unleash.applicationName)
                .build()
        }

    private fun lagDummyFeatureToggleService() =
        object : FeatureToggleService {
            override fun isEnabled(
                toggleId: String,
                defaultValue: Boolean,
            ) = if (unleash.environment == "local") {
                System.getenv(toggleId)?.toBoolean() ?: true
            } else {
                defaultValue
            }

            override fun iverksettingErSkruddAvForFagsystem(fagsystem: Fagsystem) =
                when (fagsystem) {
                    Fagsystem.DAGPENGER -> isEnabled(STOPP_IVERKSETTING_DAGPENGER, false)
                    Fagsystem.TILTAKSPENGER -> isEnabled(STOPP_IVERKSETTING_TILTAKSPENGER, false)
                    Fagsystem.TILLEGGSSTØNADER -> isEnabled(STOPP_IVERKSETTING_TILLEGGSSTØNADER, false)
                }
        }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)

        const val STOPP_IVERKSETTING_DAGPENGER = "utsjekk.stopp-iverksetting-dagpenger"
        const val STOPP_IVERKSETTING_TILLEGGSSTØNADER = "utsjekk.stopp-iverksetting-tilleggsstonader"
        const val STOPP_IVERKSETTING_TILTAKSPENGER = "utsjekk.stopp-iverksetting-tiltakspenger"
    }
}
