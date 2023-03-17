package no.nav.dagpenger.iverksett.konsumenter.arbeidsoppfolging

import no.nav.dagpenger.iverksett.api.domene.IverksettOvergangsstønad
import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerOvergangsstønad
import no.nav.dagpenger.iverksett.kontrakter.arbeidsoppfølging.Aktivitetstype
import no.nav.dagpenger.iverksett.kontrakter.arbeidsoppfølging.Barn
import no.nav.dagpenger.iverksett.kontrakter.arbeidsoppfølging.Periode
import no.nav.dagpenger.iverksett.kontrakter.arbeidsoppfølging.Periodetype
import no.nav.dagpenger.iverksett.kontrakter.arbeidsoppfølging.Stønadstype
import no.nav.dagpenger.iverksett.kontrakter.arbeidsoppfølging.VedtakOvergangsstønadArbeidsoppfølging
import no.nav.dagpenger.iverksett.kontrakter.arbeidsoppfølging.Vedtaksresultat

object ArbeidsoppfølgingMapper {

    fun mapTilVedtakOvergangsstønadTilArbeidsoppfølging(
        iverksett: IverksettOvergangsstønad,
    ): VedtakOvergangsstønadArbeidsoppfølging {
        return VedtakOvergangsstønadArbeidsoppfølging(
            vedtakId = iverksett.behandling.eksternId,
            personIdent = iverksett.søker.personIdent,
            barn = iverksett.søker.barn.map { Barn(it.personIdent, it.termindato) },
            stønadstype = Stønadstype.valueOf(iverksett.fagsak.stønadstype.name),
            vedtaksresultat = Vedtaksresultat.valueOf(iverksett.vedtak.vedtaksresultat.name),
            periode = mapToVedtaksperioder(iverksett.vedtak),
        )
    }

    fun mapToVedtaksperioder(vedtaksdetaljer: VedtaksdetaljerOvergangsstønad): List<Periode> {
        return vedtaksdetaljer.vedtaksperioder.map {
            Periode(
                it.periode.fomDato(),
                it.periode.tomDato(),
                Periodetype.valueOf(it.periodeType.name),
                Aktivitetstype.valueOf(it.aktivitet.name),
            )
        }
    }
}
