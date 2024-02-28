package no.nav.dagpenger.iverksett.felles

import org.springframework.context.annotation.Profile

object Profiler {
    const val INTEGRASJONSTEST = "integrasjonstest"
    const val LOKAL = "local"
    const val MOCK_OPPDRAG = "mock-oppdrag"
    const val MOCK_OAUTH = "mock-oauth"
}

@Profile("!${Profiler.LOKAL} && !${Profiler.INTEGRASJONSTEST}")
annotation class KjørerPåNais
