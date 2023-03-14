package no.nav.dagpenger.iverksett.tilbakekreving

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.iverksett.infrastruktur.service.KafkaProducerService
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.HentFagsystemsbehandlingRespons
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TilbakekrevingProducerTest {

    private val tilbakekrevingProducer = mockk<TilbakekrevingProducer>()
    private val kafkaProducerService = mockk<KafkaProducerService>()
    private val behandling = mockk<HentFagsystemsbehandlingRespons>()

    private lateinit var producer: TilbakekrevingProducer

    @BeforeEach
    internal fun setUp() {
        producer = TilbakekrevingProducer(kafkaProducerService)
    }

    @Test
    internal fun `kast unntak ved sending av melding, forvent at unntak viderekastes`() {
        every { kafkaProducerService.send(any(), any(), any()) } throws RuntimeException()
        assertThrows(RuntimeException::class.java) { tilbakekrevingProducer.send(behandling, "0") }
    }
}
