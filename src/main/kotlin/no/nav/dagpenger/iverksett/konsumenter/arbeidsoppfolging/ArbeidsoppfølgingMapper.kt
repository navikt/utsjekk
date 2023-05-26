package no.nav.dagpenger.iverksett.konsumenter.arbeidsoppfolging

import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerDagpenger
import no.nav.dagpenger.kontrakter.iverksett.arbeidsoppfølging.Aktivitetstype
import no.nav.dagpenger.kontrakter.iverksett.arbeidsoppfølging.Barn
import no.nav.dagpenger.kontrakter.iverksett.arbeidsoppfølging.Periode
import no.nav.dagpenger.kontrakter.iverksett.arbeidsoppfølging.Periodetype
import no.nav.dagpenger.kontrakter.iverksett.arbeidsoppfølging.VedtakDagpengerArbeidsoppfølging
import no.nav.dagpenger.kontrakter.iverksett.arbeidsoppfølging.Vedtaksresultat

object ArbeidsoppfølgingMapper {

    fun mapTilVedtakDagpengerTilArbeidsoppfølging(
        iverksett: IverksettDagpenger,
    ): VedtakDagpengerArbeidsoppfølging {
        return VedtakDagpengerArbeidsoppfølging(
            behanlingId = iverksett.behandling.behandlingId,
            personIdent = iverksett.søker.personIdent,
            barn = iverksett.søker.barn.map { Barn(it.personIdent, it.termindato) },
            stønadstype = iverksett.fagsak.stønadstype,
            vedtaksresultat = Vedtaksresultat.valueOf(iverksett.vedtak.vedtaksresultat.name),
            periode = mapToVedtaksperioder(iverksett.vedtak),
        )
    }

    fun mapToVedtaksperioder(vedtaksdetaljer: VedtaksdetaljerDagpenger): List<Periode> {
        return vedtaksdetaljer.vedtaksperioder.map {
            Periode(
                it.periode.fom,
                it.periode.tom,
                Periodetype.valueOf(it.periodeType.name),
                Aktivitetstype.valueOf(it.aktivitet.name),
            )
        }
    }
}
