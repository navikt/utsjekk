package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.api.domene.VedtaksperiodeDagpenger
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import org.springframework.stereotype.Service

@Service
class VedtakStatusService(
    private val iverksettingRepository: IverksettingRepository,
) {

    fun getVedtakStatus(personId: String): VedtaksdetaljerDto? {
        return iverksettingRepository.findByPersonIdAndResult(personId, Vedtaksresultat.INNVILGET.name)
            .maxByOrNull { it.data.vedtak.vedtakstidspunkt }?.data?.vedtak?.let {
                VedtaksdetaljerDto(
                    vedtakstype = it.vedtakstype,
                    vedtakstidspunkt = it.vedtakstidspunkt,
                    resultat = it.vedtaksresultat,
                    saksbehandlerId = "",
                    beslutterId = "",
                    vedtaksperioder = mapVedtaksperioder(it.vedtaksperioder),
                )
            }
    }

    private fun mapVedtaksperioder(inn: List<VedtaksperiodeDagpenger>): List<VedtaksperiodeDto> {
        return inn.map { vedtaksperiode ->
            VedtaksperiodeDto(
                fraOgMedDato = vedtaksperiode.periode.fom,
                tilOgMedDato = vedtaksperiode.periode.tom,
                periodeType = vedtaksperiode.periodeType,
            )
        }
    }
}
