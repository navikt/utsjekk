package no.nav.dagpenger.iverksett.status

import no.nav.dagpenger.iverksett.Integrasjonstest
import no.nav.dagpenger.iverksett.felles.http.ObjectMapperProvider.objectMapper
import no.nav.dagpenger.iverksett.utbetaling.domene.behandlingId
import no.nav.dagpenger.iverksett.utbetaling.domene.sakId
import no.nav.dagpenger.iverksett.utbetaling.util.enIverksetting
import no.nav.dagpenger.kontrakter.felles.somString
import no.nav.dagpenger.kontrakter.iverksett.IverksettStatus
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

        val raw = KafkaContainerInitializer.records.first().value()
        val melding = objectMapper.readValue(raw, StatusEndretMelding::class.java)

        assertEquals(iverksetting.sakId.somString, melding.sakId)
        assertEquals(iverksetting.fagsak.fagsystem, melding.fagsystem)
        assertEquals(iverksetting.behandlingId.somString, melding.behandlingId)
        assertEquals(IverksettStatus.OK, melding.status)
        assertNull(melding.iverksettingId)
    }
}