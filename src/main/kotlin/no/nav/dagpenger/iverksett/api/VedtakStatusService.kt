package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.api.domene.Iverksett
import no.nav.dagpenger.iverksett.api.domene.VedtaksperiodeDagpenger
import no.nav.dagpenger.kontrakter.datadeling.DatadelingRequest
import no.nav.dagpenger.kontrakter.datadeling.DatadelingResponse
import no.nav.dagpenger.kontrakter.datadeling.Periode
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import no.nav.dagpenger.kontrakter.iverksett.VedtaksstatusDto
import org.springframework.stereotype.Service

@Service
class VedtakStatusService(
    private val iverksettingRepository: IverksettingRepository,
) {

    fun getVedtakStatus(personId: String): VedtaksstatusDto? {
        return iverksettingRepository.findByPersonIdAndResult(personId, Vedtaksresultat.INNVILGET.name)
            .maxByOrNull { it.data.vedtak.vedtakstidspunkt }?.data?.vedtak?.let {
                VedtaksstatusDto(
                    vedtakstype = it.vedtakstype,
                    vedtakstidspunkt = it.vedtakstidspunkt,
                    resultat = it.vedtaksresultat,
                    vedtaksperioder = mapVedtaksperioder(it.vedtaksperioder),
                )
            }
    }

    fun hentVedtaksperioderForPersonOgPeriode(request: DatadelingRequest): DatadelingResponse {
        // Finn alle iverksettinger for denne personen
        // Returner kun de vedtaksperiodene som overlapper med fom-tom i request
        // TODO: Skal vi sjekke kun INNVILGET? Hva skjer med historiske vedtak? Blir disse OPPHØRT?
        val liste = iverksettingRepository.findByPersonIdAndResult(request.personIdent, Vedtaksresultat.INNVILGET.name)

        return DatadelingResponse(
            personIdent = request.personIdent,
            perioder = liste.flatMap { mapPerioder(it, request) }
        )
    }

    private fun mapPerioder(iverksett: Iverksett, request: DatadelingRequest): List<Periode> {
        val vedtak = iverksett.data.vedtak
        val yt = vedtak.tilkjentYtelse?.sisteAndelIKjede?.stønadstype ?: StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER

        return vedtak.vedtaksperioder
            .filter {
                (request.tilOgMedDato == null || request.tilOgMedDato!! >= it.periode.fom) && request.fraOgMedDato <= it.periode.tom
            }
            .map {
                Periode(
                    fraOgMedDato = it.periode.fom,
                    tilOgMedDato = it.periode.tom,
                    ytelseType = yt,
                    gjenståendeDager = 0  // TODO: Hvordan kan vi få dette?
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
