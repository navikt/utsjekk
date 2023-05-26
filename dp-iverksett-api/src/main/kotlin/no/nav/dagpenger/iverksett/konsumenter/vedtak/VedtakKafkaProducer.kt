package no.nav.dagpenger.iverksett.konsumenter.vedtak

import no.nav.dagpenger.iverksett.infrastruktur.service.KafkaProducerService
import no.nav.dagpenger.kontrakter.iverksett.objectMapper
import no.nav.dagpenger.kontrakter.iverksett.samordning.DagpengerVedtakhendelse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class VedtakKafkaProducer(private val kafkaProducerService: KafkaProducerService) {

    @Value("\${VEDTAK_TOPIC}")
    lateinit var topic: String

    fun sendVedtak(hendelse: DagpengerVedtakhendelse) {
        kafkaProducerService.send(topic, hendelse.behandlingId.toString(), objectMapper.writeValueAsString(hendelse))
    }
}
