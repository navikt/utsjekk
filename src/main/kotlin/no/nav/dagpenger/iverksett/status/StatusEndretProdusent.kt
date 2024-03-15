package no.nav.dagpenger.iverksett.status

import no.nav.dagpenger.iverksett.felles.http.ObjectMapperProvider.objectMapper
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.behandlingId
import no.nav.dagpenger.iverksett.utbetaling.domene.sakId
import no.nav.dagpenger.kontrakter.iverksett.IverksettStatus
import no.nav.dagpenger.kontrakter.iverksett.StatusEndretMelding
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class StatusEndretProdusent(
    private val producer: KafkaProducer<String, String>,
    @Value("\${kafka.config.topic}") private val topic: String,
) {
    fun sendStatusEndretEvent(
        iverksetting: Iverksetting,
        iverksettStatus: IverksettStatus,
    ) {
        val melding =
            StatusEndretMelding(
                sakId = iverksetting.sakId,
                behandlingId = iverksetting.behandlingId,
                iverksettingId = iverksetting.behandling.iverksettingId,
                fagsystem = iverksetting.fagsak.fagsystem,
                status = iverksettStatus,
            )

        producer.send(ProducerRecord(topic, iverksetting.søker.personident, objectMapper.writeValueAsString(melding)))
    }
}
