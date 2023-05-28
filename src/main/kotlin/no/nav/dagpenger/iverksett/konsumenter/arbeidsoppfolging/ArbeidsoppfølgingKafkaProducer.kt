package no.nav.dagpenger.iverksett.konsumenter.arbeidsoppfolging

import no.nav.dagpenger.iverksett.infrastruktur.service.KafkaProducerService
import no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk.toJson
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.iverksett.arbeidsoppfølging.VedtakDagpengerArbeidsoppfølging
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ArbeidsoppfølgingKafkaProducer(private val kafkaProducerService: KafkaProducerService) {

    @Value("\${ARBEIDSOPPFOLGING_VEDTAK_TOPIC}")
    lateinit var topic: String

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sendVedtak(vedtakDagpengerArbeidsoppfølging: VedtakDagpengerArbeidsoppfølging) {
        sendVedtak(
            vedtakDagpengerArbeidsoppfølging.behanlingId,
            vedtakDagpengerArbeidsoppfølging.stønadstype,
            vedtakDagpengerArbeidsoppfølging.toJson(),
        )
    }

    fun sendVedtak(behandlingId: UUID, stønadstype: StønadType, vedtakArbeidsoppfølging: String) {
        logger.info("Sending to Kafka topic: {}", topic)
        secureLogger.debug("Sending to Kafka topic: {}\nArbeidsoppfølging: {}", topic, vedtakArbeidsoppfølging)

        runCatching {
            kafkaProducerService.sendMedStønadstypeIHeader(topic, StønadType.valueOf(stønadstype.name), behandlingId.toString(), vedtakArbeidsoppfølging)
            logger.info("Arbeidsoppfølging for behandling=$behandlingId sent til Kafka")
        }.onFailure {
            val errorMessage = "Kunne ikke sende vedtak til arbeidsoppfølging topic. Se securelogs for mer informasjon."
            logger.error(errorMessage)
            secureLogger.error("Kunne ikke sende vedtak til arbeidsoppfølging topic", it)
            throw RuntimeException(errorMessage)
        }
    }
}
