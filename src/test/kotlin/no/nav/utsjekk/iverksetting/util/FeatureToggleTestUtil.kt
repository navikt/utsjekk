package no.nav.utsjekk.iverksetting.util

import io.mockk.every
import io.mockk.mockk
import no.nav.utsjekk.iverksetting.featuretoggle.FeatureToggleConfig
import no.nav.utsjekk.iverksetting.featuretoggle.FeatureToggleService
import no.nav.utsjekk.kontrakter.felles.Fagsystem

fun mockFeatureToggleService(enabled: Boolean = true) =
    mockk<FeatureToggleService>().apply {
        every { isEnabled(any(), any()) } returns enabled
        every { isEnabled(any<String>()) } returns enabled
        every { iverksettingErSkruddAvForFagsystem(Fagsystem.DAGPENGER) } returns false
        every { iverksettingErSkruddAvForFagsystem(Fagsystem.TILTAKSPENGER) } returns false
        every { iverksettingErSkruddAvForFagsystem(Fagsystem.TILLEGGSSTØNADER) } returns false
        every { isEnabled(FeatureToggleConfig.STOPP_IVERKSETTING_DAGPENGER) } answers { false }
        every { isEnabled(FeatureToggleConfig.STOPP_IVERKSETTING_TILTAKSPENGER) } answers { false }
        every { isEnabled(FeatureToggleConfig.STOPP_IVERKSETTING_TILLEGGSSTØNADER) } answers { false }
    }
