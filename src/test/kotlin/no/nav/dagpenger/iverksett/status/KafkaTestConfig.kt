package no.nav.dagpenger.iverksett.status

import io.mockk.mockk
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("servertest")
class KafkaTestConfig {
    @Bean
    @Primary
    fun statusEndretProdusent(): StatusEndretProdusent = mockk(relaxed = true)
}
