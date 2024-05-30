package no.nav.utsjekk.iverksetting.featuretoggle

import io.getunleash.UnleashContext
import io.getunleash.strategy.Strategy

class ByEnvironmentStrategy : Strategy {
    companion object {
        private const val MILJØKEY = "miljø"
    }

    override fun getName() = "byEnvironment"

    override fun isEnabled(map: Map<String, String>) = isEnabled(map, UnleashContext.builder().build())

    override fun isEnabled(
        map: Map<String, String>,
        unleashContext: UnleashContext,
    ): Boolean =
        unleashContext.environment
            .map { env -> map[MILJØKEY]?.split(',')?.contains(env) ?: false }
            .orElse(false)
}
