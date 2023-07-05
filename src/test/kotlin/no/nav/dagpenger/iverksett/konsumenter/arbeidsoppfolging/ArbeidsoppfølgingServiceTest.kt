package no.nav.dagpenger.iverksett.konsumenter.arbeidsoppfolging

import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.iverksett.infrastruktur.util.opprettIverksettDagpenger
import org.junit.Test

class ArbeidsoppfølgingServiceTest {

    private val arbeidsoppfølgingKafkaProducer = mockk<ArbeidsoppfølgingKafkaProducer>()

    val arbeidsoppfølgingService = ArbeidsoppfølgingService(arbeidsoppfølgingKafkaProducer)

    @Test
    fun `sendTilKafka hvis dagpenger`() {
        justRun { arbeidsoppfølgingKafkaProducer.sendVedtak(any()) }
        val iverksett = opprettIverksettDagpenger()
        arbeidsoppfølgingService.sendTilKafka(iverksett)

        verify(exactly = 1) { arbeidsoppfølgingKafkaProducer.sendVedtak(any()) }
    }
}
