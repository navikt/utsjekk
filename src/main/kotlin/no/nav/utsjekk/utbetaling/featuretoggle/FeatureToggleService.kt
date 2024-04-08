package no.nav.utsjekk.utbetaling.featuretoggle

import no.nav.utsjekk.kontrakter.felles.Fagsystem

interface FeatureToggleService {
    fun isEnabled(toggleId: String) = isEnabled(toggleId, false)

    fun iverksettingErSkruddAvForFagsystem(fagsystem: Fagsystem): Boolean

    fun isEnabled(
        toggleId: String,
        defaultValue: Boolean,
    ): Boolean
}
