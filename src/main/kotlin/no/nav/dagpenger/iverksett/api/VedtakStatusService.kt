package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.api.domene.Iverksett
import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerDagpenger
import no.nav.dagpenger.iverksett.api.domene.VedtaksperiodeDagpenger
import no.nav.dagpenger.iverksett.api.domene.personIdent
import no.nav.dagpenger.kontrakter.felles.BrevmottakerDto
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.TilbakekrevingDto
import no.nav.dagpenger.kontrakter.iverksett.TilbakekrevingMedVarselDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import no.nav.dagpenger.kontrakter.iverksett.VedtaksstatusDto
import org.springframework.stereotype.Service
import java.time.LocalDate

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

    fun hentIverksettingerForPersonOgPeriode(request: VedtakRequest): List<IverksettDto> {
        // Finn alle iverksettinger som har vedtaksperioder som overlapper med fom-tom
        return iverksettingRepository.findByPersonIdAndResult(request.fnr, Vedtaksresultat.INNVILGET.name)
            .filter { iverksett ->
                iverksett.data.vedtak.vedtaksperioder.any {
                    (request.tilOgMedDato == null || request.tilOgMedDato >= it.periode.fom) && request.fraOgMedDato <= it.periode.tom
                }
            }
            .map { mapIverksett(it) }
    }

    // TODO: Flytte til Kontrakter?
    data class VedtakRequest(
        val fnr: String,
        val fraOgMedDato: LocalDate,
        val tilOgMedDato: LocalDate?
    )

    private fun mapVedtaksperioder(inn: List<VedtaksperiodeDagpenger>): List<VedtaksperiodeDto> {
        return inn.map { vedtaksperiode ->
            VedtaksperiodeDto(
                fraOgMedDato = vedtaksperiode.periode.fom,
                tilOgMedDato = vedtaksperiode.periode.tom,
                periodeType = vedtaksperiode.periodeType,
            )
        }
    }

    private fun mapIverksett(iverksett: Iverksett): IverksettDto {
        return IverksettDto(
            sakId = iverksett.data.fagsak.fagsakId,
            behandlingId = iverksett.behandlingId,
            personIdent = iverksett.data.personIdent,
            vedtak = mapVedtaksdetaljer(iverksett.data.vedtak),
            utbetalingerPaaForrigeVedtak = emptyList(),
        )
    }

    private fun mapVedtaksdetaljer(vedtak: VedtaksdetaljerDagpenger): VedtaksdetaljerDto {
        return VedtaksdetaljerDto(
            vedtakstype = vedtak.vedtakstype,
            vedtakstidspunkt = vedtak.vedtakstidspunkt,
            resultat = vedtak.vedtaksresultat,
            saksbehandlerId = vedtak.saksbehandlerId,
            beslutterId = vedtak.beslutterId,
            opphorAarsak = vedtak.opphørÅrsak,
            avslagAarsak = vedtak.avslagÅrsak,
            utbetalinger = mapUtbetalinger(vedtak),
            vedtaksperioder = mapVedtaksperioder(vedtak),
            tilbakekreving = mapTilbakekreving(vedtak),
            brevmottakere = mapBrevmottakere(vedtak)
        )
    }

    private fun mapUtbetalinger(vedtak: VedtaksdetaljerDagpenger): List<UtbetalingDto> {
        return vedtak.tilkjentYtelse?.andelerTilkjentYtelse?.map {
            UtbetalingDto(
                belopPerDag = it.beløp,
                fraOgMedDato = it.periode.fom,
                tilOgMedDato = it.periode.tom,
                stonadstype = it.stønadstype,
                ferietillegg = it.ferietillegg,
            )
        } ?: emptyList()
    }

    private fun mapVedtaksperioder(vedtak: VedtaksdetaljerDagpenger): List<VedtaksperiodeDto> {
        return vedtak.vedtaksperioder.map {
            VedtaksperiodeDto(
                fraOgMedDato = it.periode.fom,
                tilOgMedDato = it.periode.tom,
                periodeType = it.periodeType,
            )
        }
    }

    private fun mapTilbakekreving(vedtak: VedtaksdetaljerDagpenger): TilbakekrevingDto? {
        return vedtak.tilbakekreving?.let { tilbakekrevingsdetaljer ->
            TilbakekrevingDto(
                tilbakekrevingsvalg = tilbakekrevingsdetaljer.tilbakekrevingsvalg,
                tilbakekrevingMedVarsel = tilbakekrevingsdetaljer.tilbakekrevingMedVarsel?.let {
                    TilbakekrevingMedVarselDto(
                        varseltekst = it.varseltekst,
                        sumFeilutbetaling = it.sumFeilutbetaling,
                        fellesperioder = it.perioder ?: emptyList()
                    )
                }
            )
        }
    }

    private fun mapBrevmottakere(vedtak: VedtaksdetaljerDagpenger): List<BrevmottakerDto> {
        return vedtak.brevmottakere?.mottakere?.map {
            BrevmottakerDto(
                ident = it.ident,
                navn = it.navn,
                mottakerRolle = it.mottakerRolle,
                identType = it.identType,
            )
        } ?: emptyList()
    }
}
