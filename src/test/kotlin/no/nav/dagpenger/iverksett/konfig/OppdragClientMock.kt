package no.nav.dagpenger.iverksett.konfig

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.dagpenger.iverksett.felles.oppdrag.OppdragClient
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatusDto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mock-oppdrag")
class OppdragClientMock {
    @Bean
    @Primary
    fun oppdragClient(): OppdragClient {
        val oppdragClientMock = mockk<OppdragClient>()

        every { oppdragClientMock.grensesnittavstemming(any()) } just Runs
        every { oppdragClientMock.iverksettOppdrag(any()) } just Runs
        every { oppdragClientMock.hentStatus(any()) } returns OppdragStatusDto(OppdragStatus.KVITTERT_OK, null)

        return oppdragClientMock
    }
}
