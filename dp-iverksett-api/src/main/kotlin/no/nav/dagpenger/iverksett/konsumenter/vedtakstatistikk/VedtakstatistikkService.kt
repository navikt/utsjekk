package no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk

import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import org.springframework.stereotype.Service

@Service
class VedtakstatistikkService(
    private val vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer,
) {

    fun sendTilKafka(iverksettData: IverksettDagpenger, forrigeIverksett: IverksettDagpenger?) {
        // Kunne ikke bruke sealed class i kontrakt mot datavarehus og det blir derfor if-else her
        vedtakstatistikkKafkaProducer.sendVedtak(
            VedtakstatistikkMapper.mapTilVedtakDagpengerDVH(
                iverksettData,
                forrigeIverksett?.behandling?.behandlingId,
            ),
        )
    }
}
