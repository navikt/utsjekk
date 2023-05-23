package no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk

import no.nav.dagpenger.iverksett.api.domene.Barn
import no.nav.dagpenger.iverksett.api.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.Søker
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerDagpenger
import no.nav.dagpenger.iverksett.api.domene.Vilkårsvurdering
import no.nav.dagpenger.iverksett.infrastruktur.util.VilkårsvurderingUtil
import no.nav.dagpenger.iverksett.kontrakter.dvh.Adressebeskyttelse
import no.nav.dagpenger.iverksett.kontrakter.dvh.AktivitetType
import no.nav.dagpenger.iverksett.kontrakter.dvh.Aktivitetskrav
import no.nav.dagpenger.iverksett.kontrakter.dvh.BehandlingType
import no.nav.dagpenger.iverksett.kontrakter.dvh.BehandlingÅrsak
import no.nav.dagpenger.iverksett.kontrakter.dvh.Person
import no.nav.dagpenger.iverksett.kontrakter.dvh.Utbetaling
import no.nav.dagpenger.iverksett.kontrakter.dvh.Utbetalingsdetalj
import no.nav.dagpenger.iverksett.kontrakter.dvh.Vedtak
import no.nav.dagpenger.iverksett.kontrakter.dvh.VedtakDagpengerDVH
import no.nav.dagpenger.iverksett.kontrakter.dvh.VedtaksperiodeDagpengerDto
import no.nav.dagpenger.iverksett.kontrakter.dvh.VedtaksperiodeType
import no.nav.dagpenger.iverksett.kontrakter.dvh.Vilkår
import no.nav.dagpenger.iverksett.kontrakter.dvh.Vilkårsresultat
import no.nav.dagpenger.iverksett.kontrakter.dvh.VilkårsvurderingDto
import no.nav.dagpenger.iverksett.kontrakter.dvh.ÅrsakRevurdering
import no.nav.dagpenger.kontrakter.utbetaling.StønadType
import no.nav.dagpenger.kontrakter.utbetaling.tilKlassifisering
import java.time.ZoneId
import java.util.UUID
import no.nav.dagpenger.iverksett.kontrakter.dvh.Barn as BarnEkstern
import no.nav.dagpenger.iverksett.kontrakter.dvh.StønadType as StønadTypeEkstern

object VedtakstatistikkMapper {
    private fun mapÅrsakRevurdering(behandlingsdetaljer: Behandlingsdetaljer): ÅrsakRevurdering? =
        behandlingsdetaljer.årsakRevurdering?.let {
            ÅrsakRevurdering(
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
                Adressebeskyttelse.valueOf(it.name)
            },
            tidspunktVedtak = iverksett.vedtak.vedtakstidspunkt.atZone(ZoneId.of("Europe/Oslo")),
            vilkårsvurderinger = iverksett.behandling.vilkårsvurderinger.map {
                mapTilVilkårsvurderinger(it)
            },
            person = mapTilPerson(personIdent = iverksett.søker.personIdent),
            barn = iverksett.søker.barn.map { mapTilBarn(it) },
            behandlingType = BehandlingType.valueOf(iverksett.behandling.behandlingType.name),
            behandlingÅrsak = BehandlingÅrsak.valueOf(iverksett.behandling.behandlingÅrsak.name),
            vedtak = Vedtak.valueOf(iverksett.vedtak.vedtaksresultat.name),
            vedtaksperioder = mapToVedtaksperioder(iverksett.vedtak),
            utbetalinger = iverksett.vedtak.tilkjentYtelse?.let {
                mapTilUtbetaling(
                    it,
                    iverksett.fagsak.stønadstype,
                    iverksett.fagsak.fagsakId,
                    iverksett.søker,
                )
            } ?: emptyList(),
            aktivitetskrav = Aktivitetskrav(
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
        stønadsType: StønadType,
        sakId: UUID,
        søker: Søker,
    ): List<Utbetaling> {
        return tilkjentYtelse.andelerTilkjentYtelse.map {
            Utbetaling(
                beløp = it.beløp,
                samordningsfradrag = it.samordningsfradrag,
                inntekt = it.inntekt,
                inntektsreduksjon = it.inntektsreduksjon,
                fraOgMed = it.periode.fom,
                tilOgMed = it.periode.tom,
                Utbetalingsdetalj(
                    gjelderPerson = mapTilPerson(personIdent = søker.personIdent),
                    klassekode = stønadsType.tilKlassifisering(),
                    delytelseId = sakId.toString() + (it.periodeId ?: ""),
                ),
            )
        }
    }

    private fun mapTilPerson(personIdent: String?): Person {
        return Person(personIdent = personIdent)
    }

    private fun mapTilBarn(barn: Barn): BarnEkstern {
        return BarnEkstern(personIdent = barn.personIdent, termindato = barn.termindato)
    }

    private fun mapTilVilkårsvurderinger(vilkårsvurdering: Vilkårsvurdering): VilkårsvurderingDto {
        return VilkårsvurderingDto(
            vilkår = Vilkår.valueOf(vilkårsvurdering.vilkårType.name),
            resultat = Vilkårsresultat.valueOf(vilkårsvurdering.resultat.name),
        )
    }

    private fun mapToVedtaksperioder(vedtaksdetaljer: VedtaksdetaljerDagpenger): List<VedtaksperiodeDagpengerDto> {
        return vedtaksdetaljer.vedtaksperioder.map {
            VedtaksperiodeDagpengerDto(
                it.periode.fom,
                it.periode.tom,
                AktivitetType.valueOf(it.aktivitet.name),
                VedtaksperiodeType.valueOf(it.periodeType.name),
            )
        }
    }
}
