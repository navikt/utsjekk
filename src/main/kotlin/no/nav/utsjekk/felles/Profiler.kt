package no.nav.utsjekk.felles

import org.springframework.context.annotation.Profile

object Profiler {
    const val INTEGRASJONSTEST = "integrasjonstest"
    const val LOKAL = "local"
    const val MOCK_OPPDRAG = "mock-oppdrag"
    const val MOCK_OAUTH = "mock-oauth"
    const val MOCK_SIMULERING = "mock-simulering"
}

@Profile("!${Profiler.LOKAL} && !${Profiler.INTEGRASJONSTEST}")
annotation class KjørerPåNais
