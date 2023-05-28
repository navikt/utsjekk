package no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk

import no.nav.dagpenger.iverksett.api.domene.Barn
import no.nav.dagpenger.iverksett.api.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.Søker
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerDagpenger
import no.nav.dagpenger.iverksett.api.domene.Vilkårsvurdering
import no.nav.dagpenger.iverksett.api.domene.tilKlassifisering
import no.nav.dagpenger.iverksett.infrastruktur.util.VilkårsvurderingUtil
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.iverksett.dvh.AdressebeskyttelseDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.AktivitetTypeDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.AktivitetskravDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.BehandlingTypeDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.BehandlingÅrsakDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.PersonDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.UtbetalingDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.UtbetalingsdetaljDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.VedtakDagpengerDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.VedtakresultatDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.VedtaksperiodeDagpengerDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.VedtaksperiodeTypeDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.VilkårDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.VilkårsresultatDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.VilkårsvurderingDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.ÅrsakRevurderingDVH
import java.time.ZoneId
import java.util.UUID
import no.nav.dagpenger.kontrakter.iverksett.dvh.BarnDVH as BarnEkstern
import no.nav.dagpenger.kontrakter.iverksett.dvh.StønadTypeDVH as StønadTypeEkstern

object VedtakstatistikkMapper {
    private fun mapÅrsakRevurdering(behandlingsdetaljer: Behandlingsdetaljer): ÅrsakRevurderingDVH? =
        behandlingsdetaljer.årsakRevurdering?.let {
            ÅrsakRevurderingDVH(
                opplysningskilde = it.opplysningskilde.name,
                årsak = it.årsak.name,
            )
        }

    fun mapTilVedtakDagpengerDVH(
        iverksett: IverksettDagpenger,
        forrigeIverksettBehandlingId: UUID?,
    ): VedtakDagpengerDVH {
        return VedtakDagpengerDVH(
            fagsakId = iverksett.fagsak.fagsakId,
            behandlingId = iverksett.behandling.behandlingId,
            relatertBehandlingId = forrigeIverksettBehandlingId,
            adressebeskyttelse = iverksett.søker.adressebeskyttelse?.let {
                AdressebeskyttelseDVH.valueOf(it.name)
            },
            tidspunktVedtak = iverksett.vedtak.vedtakstidspunkt.atZone(ZoneId.of("Europe/Oslo")),
            vilkårsvurderinger = iverksett.behandling.vilkårsvurderinger.map {
                mapTilVilkårsvurderinger(it)
            },
            person = mapTilPerson(personIdent = iverksett.søker.personIdent),
            barn = iverksett.søker.barn.map { mapTilBarn(it) },
            behandlingType = BehandlingTypeDVH.valueOf(iverksett.behandling.behandlingType.name),
            behandlingÅrsak = BehandlingÅrsakDVH.valueOf(iverksett.behandling.behandlingÅrsak.name),
            vedtak = VedtakresultatDVH.valueOf(iverksett.vedtak.vedtaksresultat.name),
            vedtaksperioder = mapToVedtaksperioder(iverksett.vedtak),
            utbetalinger = iverksett.vedtak.tilkjentYtelse?.let {
                mapTilUtbetaling(
                    it,
                    iverksett.fagsak.fagsakId,
                    iverksett.søker,
                )
            } ?: emptyList(),
            aktivitetskrav = AktivitetskravDVH(
                aktivitetspliktInntrefferDato = iverksett.behandling.aktivitetspliktInntrefferDato,
                harSagtOppArbeidsforhold = VilkårsvurderingUtil
                    .hentHarSagtOppEllerRedusertFraVurderinger(iverksett.behandling.vilkårsvurderinger),
            ),
            funksjonellId = iverksett.behandling.behandlingId,
            stønadstype = mapStønadType(iverksett.fagsak.stønadstype),
            kravMottatt = iverksett.behandling.kravMottatt,
            årsakRevurdering = mapÅrsakRevurdering(iverksett.behandling),
            avslagÅrsak = iverksett.vedtak.avslagÅrsak?.name,
        )
    }

    private fun mapStønadType(stønadsType: StønadType): StønadTypeEkstern = when (stønadsType) {
        StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER,
        StønadType.DAGPENGER_PERMITTERING_ORDINAER,
        StønadType.DAGPENGER_PERMITTERING_FISKEINDUSTRI,
        StønadType.DAGPENGER_EOS,
        -> StønadTypeEkstern.DAGPENGER
    }

    private fun mapTilUtbetaling(
        tilkjentYtelse: TilkjentYtelse,
        sakId: UUID,
        søker: Søker,
    ): List<UtbetalingDVH> {
        return tilkjentYtelse.andelerTilkjentYtelse.map {
            UtbetalingDVH(
                beløp = it.beløp,
                samordningsfradrag = it.samordningsfradrag,
                inntekt = it.inntekt,
                inntektsreduksjon = it.inntektsreduksjon,
                fraOgMed = it.periode.fom,
                tilOgMed = it.periode.tom,
                UtbetalingsdetaljDVH(
                    gjelderPerson = mapTilPerson(personIdent = søker.personIdent),
                    klassekode = it.tilKlassifisering(),
                    delytelseId = sakId.toString() + (it.periodeId ?: ""),
                ),
            )
        }
    }

    private fun mapTilPerson(personIdent: String?): PersonDVH {
        return PersonDVH(personIdent = personIdent)
    }

    private fun mapTilBarn(barn: Barn): BarnEkstern {
        return BarnEkstern(personIdent = barn.personIdent, termindato = barn.termindato)
    }

    private fun mapTilVilkårsvurderinger(vilkårsvurdering: Vilkårsvurdering): VilkårsvurderingDVH {
        return VilkårsvurderingDVH(
            vilkår = VilkårDVH.valueOf(vilkårsvurdering.vilkårType.name),
            resultat = VilkårsresultatDVH.valueOf(vilkårsvurdering.resultat.name),
        )
    }

    private fun mapToVedtaksperioder(vedtaksdetaljer: VedtaksdetaljerDagpenger): List<VedtaksperiodeDagpengerDVH> {
        return vedtaksdetaljer.vedtaksperioder.map {
            VedtaksperiodeDagpengerDVH(
                it.periode.fom,
                it.periode.tom,
                AktivitetTypeDVH.valueOf(it.aktivitet.name),
                VedtaksperiodeTypeDVH.valueOf(it.periodeType.name),
            )
        }
    }
}
