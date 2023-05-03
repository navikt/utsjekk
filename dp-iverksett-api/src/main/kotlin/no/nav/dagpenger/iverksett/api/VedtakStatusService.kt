package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerDagpenger
import no.nav.dagpenger.iverksett.kontrakter.iverksett.IverksettStatus
import org.springframework.stereotype.Service

@Service
class VedtakStatusService (
    private val iverksettingRepository: IverksettingRepository,
    private val iverksettingService: IverksettingService
) {

    fun getVedtakStatus(personId: String) :VedtaksdetaljerDagpenger? {
        val iverksettinger = iverksettingRepository.findByPersonId(personId)

        return iverksettinger.sortedByDescending { it.data.vedtak.vedtakstidspunkt }
            .first { iverksetting ->
                IverksettStatus.OK == iverksettingService.utledStatus(iverksetting.behandlingId)
            }.data.vedtak
    }
}