package no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk

import no.nav.dagpenger.iverksett.infrastruktur.service.KafkaProducerService
import no.nav.dagpenger.iverksett.kontrakter.dvh.StønadType
import no.nav.dagpenger.iverksett.kontrakter.dvh.VedtakDagpengerDVH
import no.nav.dagpenger.iverksett.kontrakter.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class VedtakstatistikkKafkaProducer(private val kafkaProducerService: KafkaProducerService) {

    @Value("\${DAGPENGER_VEDTAK_TOPIC}")
    lateinit var topic: String

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sendVedtak(vedtakstatistikk: VedtakDagpengerDVH) {
        sendVedtak(vedtakstatistikk.behandlingId, vedtakstatistikk.stønadstype, vedtakstatistikk.toJson())
    }
    fun sendVedtak(behandlingId: UUID, stønadstype: StønadType, vedtakStatistikk: String) {
        logger.info("Sending to Kafka topic: {}", topic)
        secureLogger.debug("Sending to Kafka topic: {}\nVedtakStatistikk: {}", topic, vedtakStatistikk)

        runCatching {
            kafkaProducerService.sendMedStønadstypeIHeader(topic, stønadstype.tilFelles(), behandlingId.toString(), vedtakStatistikk)
            logger.info("Vedtakstatistikk for behandling=$behandlingId sent til Kafka")
        }.onFailure {
            val errorMessage = "Could not send vedtak to Kafka. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send vedtak to Kafka", it)
            throw RuntimeException(errorMessage)
        }
    }
}

fun Any.toJson(): String = objectMapper.writeValueAsString(this)

fun StønadType.tilFelles(): no.nav.dagpenger.kontrakter.utbetaling.StønadType = when (this) {
    StønadType.DAGPENGER -> no.nav.dagpenger.kontrakter.utbetaling.StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER
}
