package no.nav.dagpenger.iverksett.konsumenter.arbeidsoppfolging

import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import org.springframework.stereotype.Service

@Service
class ArbeidsoppfølgingService(
    private val arbeidsoppfølgingKafkaProducer: ArbeidsoppfølgingKafkaProducer,
) {

    fun sendTilKafka(iverksettData: IverksettDagpenger) {
        if (iverksettData is IverksettDagpenger) {
            arbeidsoppfølgingKafkaProducer.sendVedtak(
                ArbeidsoppfølgingMapper.mapTilVedtakDagpengerTilArbeidsoppfølging(iverksettData),
            )
        }
    }
}
