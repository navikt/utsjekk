package no.nav.dagpenger.iverksett.konsumenter.tilbakekreving

import no.nav.dagpenger.iverksett.api.domene.SakIdentifikator
import no.nav.dagpenger.iverksett.infrastruktur.service.KafkaProducerService
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.dagpenger.kontrakter.iverksett.tilbakekreving.HentFagsystemsbehandlingRespons
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TilbakekrevingProducer(private val kafkaProducerService: KafkaProducerService) {

    @Value("\${FAGSYSTEMBEHANDLING_RESPONS_TOPIC}")
    lateinit var topic: String

    private val logger = LoggerFactory.getLogger(javaClass)

    fun send(behandling: HentFagsystemsbehandlingRespons, key: String) {
        val sakIdForBehandling = SakIdentifikator(behandling.hentFagsystemsbehandling?.sakId, behandling.hentFagsystemsbehandling?.saksreferanse)
        try {
            kafkaProducerService.send(topic, key, behandling.toJson())
            logger.info(
                "Fagsystembehandling er sent til Kafka. key=$key " +
                    "eksternFagsakId=${sakIdForBehandling.toIdString()}",
            )
        } catch (ex: Exception) {
            val errorMessage = "Kunne ikke sende behandling til Kafka. Se securelogs for mere informasjon. "
            logger.error(errorMessage)
            throw RuntimeException(errorMessage)
        }
    }

    private fun Any.toJson(): String = objectMapper.writeValueAsString(this)
}
