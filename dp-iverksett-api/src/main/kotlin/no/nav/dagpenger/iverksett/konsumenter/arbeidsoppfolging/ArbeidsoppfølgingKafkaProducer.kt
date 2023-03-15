package no.nav.dagpenger.iverksett.konsumenter.arbeidsoppfolging

import no.nav.dagpenger.iverksett.infrastruktur.service.KafkaProducerService
import no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk.toJson
import no.nav.dagpenger.iverksett.kontrakter.arbeidsoppfølging.Stønadstype
import no.nav.dagpenger.iverksett.kontrakter.arbeidsoppfølging.VedtakOvergangsstønadArbeidsoppfølging
import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ArbeidsoppfølgingKafkaProducer(private val kafkaProducerService: KafkaProducerService) {

    @Value("\${ARBEIDSOPPFOLGING_VEDTAK_TOPIC}")
    lateinit var topic: String

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sendVedtak(vedtakOvergangsstønadArbeidsoppfølging: VedtakOvergangsstønadArbeidsoppfølging) {
        sendVedtak(vedtakOvergangsstønadArbeidsoppfølging.vedtakId, vedtakOvergangsstønadArbeidsoppfølging.stønadstype, vedtakOvergangsstønadArbeidsoppfølging.toJson())
    }

    fun sendVedtak(behandlingId: Long, stønadstype: Stønadstype, vedtakArbeidsoppfølging: String) {
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
