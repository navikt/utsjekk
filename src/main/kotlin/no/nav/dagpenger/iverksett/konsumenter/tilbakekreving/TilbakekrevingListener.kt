package no.nav.dagpenger.iverksett.konsumenter.tilbakekreving

import com.fasterxml.jackson.module.kotlin.readValue
import java.lang.IllegalStateException
import no.nav.dagpenger.iverksett.api.IverksettingRepository
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.infrastruktur.FamilieIntegrasjonerClient
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.dagpenger.kontrakter.iverksett.tilbakekreving.HentFagsystemsbehandlingRequest
import no.nav.dagpenger.kontrakter.iverksett.tilbakekreving.HentFagsystemsbehandlingRespons
import no.nav.familie.log.mdc.MDCConstants
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.stereotype.Component
import java.util.UUID
import no.nav.dagpenger.iverksett.api.domene.SakIdentifikator

@Component
class TilbakekrevingListener(
    private val iverksettingRepository: IverksettingRepository,
    private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
    private val tilbakekrevingProducer: TilbakekrevingProducer,
) : ConsumerSeekAware {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

/*    @KafkaListener(
        id = "dp-iverksett",
        topics = ["teamdagpenger.privat-tbk-hentfagsystemsbehandling-request-topic"],
        containerFactory = "concurrentTilbakekrevingListenerContainerFactory",
    )*/
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
            logger.info("HentFagsystemsbehandlingRequest er mottatt i kafka med key=$key og data=$data")
            val iverksett = iverksettingRepository.findById(request.behandlingId).get().data
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

    private fun sjekkFagsakIdKonsistens(iverksett: IverksettDagpenger, request: HentFagsystemsbehandlingRequest) {
        // TODO Trenger håndtere sakidentifikator som både kan være UUID og String.
        // Sånn det er nå kan det skje en subtil bug
        val sakIdForRequest = SakIdentifikator(request.sakId, request.saksreferanse)
        if (iverksett.fagsak.toIdString() != sakIdForRequest.toIdString()) {
            error(
                "Inkonsistens. FagsakID mellom iverksatt behandling (fagsakID=" +
                    "${iverksett.fagsak.toIdString()}) og request (ekstern fagsakID=${sakIdForRequest.toIdString()}) er ulike, " +
                    "med behandlingID=${request.behandlingId}",
            )
        }
    }
}
