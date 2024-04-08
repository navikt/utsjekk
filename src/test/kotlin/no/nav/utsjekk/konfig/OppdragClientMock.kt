package no.nav.utsjekk.konfig

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.utsjekk.felles.Profiler
import no.nav.utsjekk.felles.oppdrag.OppdragClient
import no.nav.utsjekk.kontrakter.oppdrag.OppdragStatus
import no.nav.utsjekk.kontrakter.oppdrag.OppdragStatusDto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile(Profiler.MOCK_OPPDRAG)
class OppdragClientMock {
    @Bean
    @Primary
    fun oppdragClient() =
        mockk<OppdragClient>().also {
            every { it.grensesnittavstemming(any()) } just Runs
            every { it.iverksettOppdrag(any()) } just Runs
            every { it.hentStatus(any()) } returns OppdragStatusDto(OppdragStatus.KVITTERT_OK, null)
        }
}
