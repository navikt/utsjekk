package no.nav.dagpenger.iverksett.util

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.iverksett.infrastruktur.featuretoggle.FeatureToggleService

fun mockFeatureToggleService(enabled: Boolean = true): FeatureToggleService {
    val mockk = mockk<FeatureToggleService>()
    every { mockk.isEnabled(any()) } returns enabled
    every { mockk.isEnabled("dp.iverksett.stopp-iverksetting") } answers {
        false
    }
    return mockk
}
