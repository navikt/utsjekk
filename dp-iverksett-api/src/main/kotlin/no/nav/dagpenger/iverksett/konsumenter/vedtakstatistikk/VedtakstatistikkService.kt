package no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk

import no.nav.dagpenger.iverksett.api.domene.IverksettOvergangsstønad
import org.springframework.stereotype.Service

@Service
class VedtakstatistikkService(
    private val vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer,
) {

    fun sendTilKafka(iverksettData: IverksettOvergangsstønad, forrigeIverksett: IverksettOvergangsstønad?) {
        // Kunne ikke bruke sealed class i kontrakt mot datavarehus og det blir derfor if-else her
        vedtakstatistikkKafkaProducer.sendVedtak(
            VedtakstatistikkMapper.mapTilVedtakOvergangsstønadDVH(
                iverksettData,
                forrigeIverksett?.behandling?.eksternId,
            ),
        )
    }
}
