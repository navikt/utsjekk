package no.nav.dagpenger.iverksett.utbetaling.featuretoggle

import no.nav.dagpenger.kontrakter.felles.Fagsystem

interface FeatureToggleService {
    fun isEnabled(toggleId: String) = isEnabled(toggleId, false)

    fun iverksettingErSkruddAvForFagsystem(fagsystem: Fagsystem): Boolean

    fun isEnabled(
        toggleId: String,
        defaultValue: Boolean,
    ): Boolean
}
