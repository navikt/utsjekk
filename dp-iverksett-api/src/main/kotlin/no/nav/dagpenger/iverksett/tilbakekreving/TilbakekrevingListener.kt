package no.nav.dagpenger.iverksett.tilbakekreving

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.dagpenger.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.dagpenger.iverksett.iverksetting.IverksettingRepository
import no.nav.dagpenger.iverksett.iverksetting.domene.IverksettData
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.HentFagsystemsbehandlingRequest
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.HentFagsystemsbehandlingRespons
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Ytelsestype
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.log.mdc.MDCConstants
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TilbakekrevingListener(
    private val iverksettingRepository: IverksettingRepository,
    private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
    private val tilbakekrevingProducer: TilbakekrevingProducer,
) : ConsumerSeekAware {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @KafkaListener(
        id = "familie-ef-iverksett",
        topics = ["teamfamilie.privat-tbk-hentfagsystemsbehandling-request-topic"],
        containerFactory = "concurrentTilbakekrevingListenerContainerFactory",
    )
    fun listen(consumerRecord: ConsumerRecord<String, String>) {
        val key: String = consumerRecord.key()
        val data: String = consumerRecord.value()
        try {
            MDC.put(MDCConstants.MDC_CALL_ID, UUID.randomUUID().toString())
            transformerOgSend(data, key)
        } catch (ex: Exception) {
            logger.error("Feil ved håndtering av HentFagsystemsbehandlingRequest med eksternId=$key")
            secureLogger.error(
                "Feil ved håndtering av HentFagsystemsbehandlingRequest med consumerRecord=$consumerRecord",
                ex,
            )
            throw ex
        } finally {
            MDC.remove(MDCConstants.MDC_CALL_ID)
        }
    }

    private fun transformerOgSend(data: String, key: String) {
        try {
            val request: HentFagsystemsbehandlingRequest =
                objectMapper.readValue(data)
            if (!request.erEfYtelse()) {
                return
            }
            logger.info("HentFagsystemsbehandlingRequest er mottatt i kafka med key=$key og data=$data")
            val iverksett = iverksettingRepository.findByEksternId(request.eksternId.toLong()).data
            sjekkFagsakIdKonsistens(iverksett, request)
            familieIntegrasjonerClient.hentBehandlendeEnhetForBehandling(iverksett.søker.personIdent)?.let {
                val fagsystemsbehandling = iverksett.tilFagsystembehandling(it)
                tilbakekrevingProducer.send(fagsystemsbehandling, key)
            } ?: error("Kan ikke finne behandlende enhet for søker på behandling ${iverksett.behandling.behandlingId}")
        } catch (ex: Exception) {
            secureLogger.error(
                "Feil ved sending av melding med key=$key. Forsøker å sende HentFagsystemsbehandlingRespons med feilmelding.",
                ex,
            )
            tilbakekrevingProducer.send(HentFagsystemsbehandlingRespons(feilMelding = ex.message), key)
        }
    }

    private fun HentFagsystemsbehandlingRequest.erEfYtelse(): Boolean {
        return listOf(
            Ytelsestype.OVERGANGSSTØNAD,
            Ytelsestype.SKOLEPENGER,
            Ytelsestype.BARNETILSYN,
        ).contains(this.ytelsestype)
    }

    private fun sjekkFagsakIdKonsistens(iverksett: IverksettData, request: HentFagsystemsbehandlingRequest) {
        if (!iverksett.fagsak.eksternId.equals(request.eksternFagsakId.toLong())) {
            error(
                "Inkonsistens. Ekstern fagsakID mellom iverksatt behandling (ekstern fagsakID=" +
                    "${iverksett.fagsak.eksternId}) og request (ekstern fagsakID=${request.eksternFagsakId}) er ulike, " +
                    "med eksternID=${request.eksternId.toLong()}",
            )
        }
    }
}
