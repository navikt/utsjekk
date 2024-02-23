package no.nav.dagpenger.iverksett.felles

import org.springframework.context.annotation.Profile

object Profiler {
    const val SERVERTEST = "servertest"
    const val LOKAL = "local"
}

@Profile("!${Profiler.LOKAL} && !${Profiler.SERVERTEST}")
annotation class KjørerPåNais
