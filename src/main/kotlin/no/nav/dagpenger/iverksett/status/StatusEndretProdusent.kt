package no.nav.dagpenger.iverksett.status

import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.behandlingId
import no.nav.dagpenger.iverksett.utbetaling.domene.sakId
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.dagpenger.kontrakter.felles.somString
import no.nav.dagpenger.kontrakter.iverksett.IverksettStatus
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

class StatusEndretProdusent {
    private val producer = KafkaProducer(AivenConfig.default.producerConfig(), StringSerializer(), StringSerializer())

    companion object {
        private const val TOPIC = "teamdagpenger.iverksetting-status-v1"
    }

    fun sendStatusEndretEvent(
        iverksetting: Iverksetting,
        iverksettStatus: IverksettStatus,
    ) {
        val melding =
            StatusEndretMelding(
                sakId = iverksetting.sakId.somString,
                behandlingId = iverksetting.behandlingId.somString,
                iverksettingId = iverksetting.behandling.iverksettingId,
                fagsystem = iverksetting.fagsak.fagsystem,
                status = iverksettStatus,
            )
        producer.send(ProducerRecord(TOPIC, iverksetting.søker.personident, objectMapper.writeValueAsString(melding)))
    }
}
