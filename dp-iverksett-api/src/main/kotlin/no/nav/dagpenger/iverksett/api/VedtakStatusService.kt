package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerDagpenger
import no.nav.dagpenger.iverksett.kontrakter.iverksett.IverksettStatus
import org.springframework.stereotype.Service

@Service
class VedtakStatusService(
    private val iverksettingRepository: IverksettingRepository,
    private val iverksettingService: IverksettingService,
) {

    fun getVedtakStatus(personId: String): VedtaksdetaljerDagpenger? {
        return iverksettingRepository.findByPersonId(personId).sortedByDescending { it.data.vedtak.vedtakstidspunkt }
            .firstOrNull() { iverksetting ->
                IverksettStatus.SENDT_TIL_OPPDRAG == iverksettingService.utledStatus(iverksetting.behandlingId)
            }?.data?.vedtak
    }
}
