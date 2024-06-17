package no.nav.utsjekk.konfig

import io.mockk.every
import io.mockk.mockk
import no.nav.utsjekk.felles.Profiler
import no.nav.utsjekk.simulering.client.SimuleringClient
import no.nav.utsjekk.simulering.enSimuleringResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile(Profiler.MOCK_SIMULERING)
class SimuleringClientMock {
    @Bean
    @Primary
    fun simuleringClient() =
        mockk<SimuleringClient>().also {
            every { it.hentSimulering(any()) } returns enSimuleringResponse()
        }
}
