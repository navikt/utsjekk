package no.nav.dagpenger.iverksett.vedtakstatistikk

import no.nav.dagpenger.iverksett.infrastruktur.service.KafkaProducerService
import no.nav.dagpenger.iverksett.kontrakter.dvh.StønadType
import no.nav.dagpenger.iverksett.kontrakter.dvh.VedtakBarnetilsynDVH
import no.nav.dagpenger.iverksett.kontrakter.dvh.VedtakOvergangsstønadDVH
import no.nav.dagpenger.iverksett.kontrakter.dvh.VedtakSkolepenger
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class VedtakstatistikkKafkaProducer(private val kafkaProducerService: KafkaProducerService) {

    @Value("\${ENSLIG_FORSORGER_VEDTAK_TOPIC}")
    lateinit var topic: String

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sendVedtak(vedtakstatistikk: VedtakOvergangsstønadDVH) {
        sendVedtak(vedtakstatistikk.behandlingId, vedtakstatistikk.stønadstype, vedtakstatistikk.toJson())
    }

    fun sendVedtak(vedtakstatistikk: VedtakBarnetilsynDVH) {
        sendVedtak(vedtakstatistikk.behandlingId, vedtakstatistikk.stønadstype, vedtakstatistikk.toJson())
    }

    fun sendVedtak(vedtakstatistikk: VedtakSkolepenger) {
        sendVedtak(vedtakstatistikk.behandlingId, vedtakstatistikk.stønadstype, vedtakstatistikk.toJson())
    }

    fun sendVedtak(behandlingId: Long, stønadstype: StønadType, vedtakStatistikk: String) {
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

fun StønadType.tilFelles(): no.nav.dagpenger.iverksett.kontrakter.felles.StønadType = when (this) {
    StønadType.BARNETILSYN -> no.nav.dagpenger.iverksett.kontrakter.felles.StønadType.BARNETILSYN
    StønadType.SKOLEPENGER -> no.nav.dagpenger.iverksett.kontrakter.felles.StønadType.SKOLEPENGER
    StønadType.OVERGANGSSTØNAD -> no.nav.dagpenger.iverksett.kontrakter.felles.StønadType.OVERGANGSSTØNAD
}
