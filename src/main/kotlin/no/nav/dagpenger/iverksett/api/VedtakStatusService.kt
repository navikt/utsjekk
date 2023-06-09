package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.api.domene.Tilbakekrevingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.VedtaksperiodeDagpenger
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottakere
import no.nav.dagpenger.kontrakter.felles.BrevmottakerDto
import no.nav.dagpenger.kontrakter.iverksett.TilbakekrevingDto
import no.nav.dagpenger.kontrakter.iverksett.TilbakekrevingMedVarselDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import org.springframework.stereotype.Service

@Service
class VedtakStatusService(
    private val iverksettingRepository: IverksettingRepository,
) {

    fun getVedtakStatus(personId: String): VedtaksdetaljerDto? {
        return iverksettingRepository.findByPersonId(personId)
            .sortedByDescending { it.data.vedtak.vedtakstidspunkt }
            .firstOrNull {
                Vedtaksresultat.INNVILGET == it.data.vedtak.vedtaksresultat
            }?.data?.vedtak?.let {
            VedtaksdetaljerDto(
                vedtakstype = it.vedtakstype,
                vedtakstidspunkt = it.vedtakstidspunkt,
                resultat = it.vedtaksresultat,
                saksbehandlerId = it.saksbehandlerId,
                beslutterId = it.beslutterId,
                opphorAarsak = it.opphørÅrsak,
                avslagAarsak = it.avslagÅrsak,
                utbetalinger = emptyList(), // Always empty?
                vedtaksperioder = mapVedtaksperioder(it.vedtaksperioder),
                tilbakekreving = mapTilbakekreving(it.tilbakekreving),
                brevmottakere = mapBrevmottakere(it.brevmottakere),
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

    private fun mapTilbakekreving(inn: Tilbakekrevingsdetaljer?): TilbakekrevingDto? {
        return inn?.let { tilbakekrevingsdetaljer ->
            TilbakekrevingDto(
                tilbakekrevingsvalg = tilbakekrevingsdetaljer.tilbakekrevingsvalg,
                tilbakekrevingMedVarsel = tilbakekrevingsdetaljer.tilbakekrevingMedVarsel?.let {
                    TilbakekrevingMedVarselDto(
                        varseltekst = it.varseltekst,
                        sumFeilutbetaling = it.sumFeilutbetaling,
                        fellesperioder = it.perioder ?: emptyList(),
                    )
                },
            )
        }
    }

    private fun mapBrevmottakere(inn: Brevmottakere?): List<BrevmottakerDto> {
        return if (inn?.mottakere != null) {
            inn.mottakere.map { value ->
                BrevmottakerDto(
                    ident = value.ident,
                    navn = value.navn,
                    mottakerRolle = value.mottakerRolle,
                    identType = value.identType,
                )
            }
        } else {
            emptyList()
        }
    }
}
