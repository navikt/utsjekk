package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerDagpenger
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import org.springframework.stereotype.Service

@Service
class VedtakStatusService(
    private val iverksettingRepository: IverksettingRepository,
    private val iverksettingService: IverksettingService,
) {

    fun getVedtakStatus(personId: String): VedtaksdetaljerDagpenger? {
        return iverksettingRepository.findByPersonId(personId).sortedByDescending { it.data.vedtak.vedtakstidspunkt }
            .firstOrNull {
                Vedtaksresultat.INNVILGET == it.data.vedtak.vedtaksresultat
            }?.data?.vedtak
    }
}
