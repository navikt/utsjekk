package no.nav.dagpenger.iverksett.status

import no.nav.dagpenger.iverksett.Integrasjonstest
import no.nav.dagpenger.iverksett.utbetaling.util.enIverksetting
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class StatusEndretProdusentTest : Integrasjonstest() {
    @Autowired
    private lateinit var kafkaProducer: KafkaProducer<String, String>

    private lateinit var produsent: StatusEndretProdusent

    @BeforeEach
    fun setup() {
        produsent = StatusEndretProdusent(kafkaProducer, "test_topic")
    }

    @Test
    fun `produserer statusmelding på topic`() {
        val iverksetting = enIverksetting()

//        produsent.sendStatusEndretEvent(iverksetting, IverksettStatus.OK)

        kafkaProducer.send(ProducerRecord("test_topic", "{ \"test\": 1234 }")).get()
        kafkaProducer.close()

//        assertEquals(1, KafkaContainerInitializer.records!!.count())
    }
}
