package no.nav.utsjekk.status

import no.nav.utsjekk.Integrasjonstest
import no.nav.utsjekk.felles.http.ObjectMapperProvider.objectMapper
import no.nav.utsjekk.initializers.KafkaContainerInitializer
import no.nav.utsjekk.iverksetting.domene.behandlingId
import no.nav.utsjekk.iverksetting.domene.sakId
import no.nav.utsjekk.iverksetting.util.enIverksetting
import no.nav.utsjekk.kontrakter.iverksett.IverksettStatus
import no.nav.utsjekk.kontrakter.iverksett.StatusEndretMelding
import org.apache.kafka.clients.producer.KafkaProducer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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

        produsent.sendStatusEndretEvent(iverksetting, IverksettStatus.OK)

        val raw = KafkaContainerInitializer.getAllRecords().first().value()
        val melding = objectMapper.readValue(raw, StatusEndretMelding::class.java)

        assertEquals(iverksetting.sakId, melding.sakId)
        assertEquals(iverksetting.fagsak.fagsystem, melding.fagsystem)
        assertEquals(iverksetting.behandlingId, melding.behandlingId)
        assertEquals(IverksettStatus.OK, melding.status)
        assertNull(melding.iverksettingId)
    }
}
