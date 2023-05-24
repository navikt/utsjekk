package no.nav.dagpenger.iverksett.infrastruktur.transformer

import no.nav.dagpenger.iverksett.api.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.Delvilkårsvurdering
import no.nav.dagpenger.iverksett.api.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.Søker
import no.nav.dagpenger.iverksett.api.domene.TilbakekrevingMedVarsel
import no.nav.dagpenger.iverksett.api.domene.Tilbakekrevingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerDagpenger
import no.nav.dagpenger.iverksett.api.domene.VedtaksperiodeDagpenger
import no.nav.dagpenger.iverksett.api.domene.Vilkårsvurdering
import no.nav.dagpenger.iverksett.api.domene.Vurdering
import no.nav.dagpenger.iverksett.api.domene.ÅrsakRevurdering
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottaker
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottakere
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingType
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.Datoperiode
import no.nav.dagpenger.iverksett.kontrakter.iverksett.AktivitetType
import no.nav.dagpenger.iverksett.kontrakter.iverksett.BehandlingsdetaljerDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.DelvilkårsvurderingDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.FagsakdetaljerDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.IverksettDagpengerdDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.SakDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.SøkerDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.TilbakekrevingDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.TilbakekrevingMedVarselDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VedtaksdetaljerDagpengerDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VedtaksperiodeDagpengerDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VilkårsvurderingDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VurderingDto
import java.time.LocalDate
import java.util.UUID
import no.nav.dagpenger.iverksett.kontrakter.iverksett.Brevmottaker as BrevmottakerKontrakter

fun VurderingDto.toDomain(): Vurdering {
    return Vurdering(this.regelId, this.svar, this.begrunnelse)
}

fun DelvilkårsvurderingDto.toDomain(): Delvilkårsvurdering {
    return Delvilkårsvurdering(this.resultat, this.vurderinger.map { it.toDomain() })
}

fun VilkårsvurderingDto.toDomain(): Vilkårsvurdering {
    return Vilkårsvurdering(this.vilkårType, this.resultat, this.delvilkårsvurderinger.map { it.toDomain() })
}

fun FagsakdetaljerDto.toDomain(): Fagsakdetaljer {
    return Fagsakdetaljer(
        fagsakId = this.fagsakId,
        stønadstype = this.stønadstype,
    )
}

fun SakDto.toDomain(): Fagsakdetaljer {
    return Fagsakdetaljer(
        fagsakId = this.sakId,
        stønadstype = this.stønadstype,
    )
}

fun SøkerDto.toDomain(): Søker {
    return Søker(
        personIdent = this.personIdent,
        barn = this.barn.map { it.toDomain() },
        tilhørendeEnhet = this.tilhørendeEnhet,
        adressebeskyttelse = this.adressebeskyttelse,
    )
}

fun BehandlingsdetaljerDto.toDomain(): Behandlingsdetaljer {
    return Behandlingsdetaljer(
        behandlingId = this.behandlingId,
        forrigeBehandlingId = this.forrigeBehandlingId,
        behandlingType = this.behandlingType,
        behandlingÅrsak = this.behandlingÅrsak,
        vilkårsvurderinger = this.vilkårsvurderinger.map { it.toDomain() },
        aktivitetspliktInntrefferDato = this.aktivitetspliktInntrefferDato,
        kravMottatt = this.kravMottatt,
        årsakRevurdering = this.årsakRevurdering?.let { ÅrsakRevurdering(it.opplysningskilde, it.årsak) },
    )
}

fun VedtaksperiodeDagpengerDto.toDomain(): VedtaksperiodeDagpenger {
    return VedtaksperiodeDagpenger(
        aktivitet = this.aktivitet ?: AktivitetType.IKKE_AKTIVITETSPLIKT,
        periode = this.fraOgMedDato?.let { Datoperiode(it, this.tilOgMedDato ?: LocalDate.MAX) }
            ?: this.periode?.let { Datoperiode(it.fom, it.tom ?: LocalDate.MAX) }
            ?: throw IllegalStateException("Verken fraOgMedDato eller periode har verdi. En av dem, helst fraOgMedDato, må være satt"),
        periodeType = this.periodeType,
    )
}

fun VedtaksdetaljerDagpengerDto.toDomain(): VedtaksdetaljerDagpenger {
    return VedtaksdetaljerDagpenger(
        vedtakstype = this.vedtakstype,
        vedtaksresultat = this.resultat,
        vedtakstidspunkt = this.vedtakstidspunkt,
        opphørÅrsak = this.opphørÅrsak,
        saksbehandlerId = this.saksbehandlerId,
        beslutterId = this.beslutterId,
        tilkjentYtelse = this.utbetalinger.tilTilkjentYtelse(),
        vedtaksperioder = this.vedtaksperioder.map { it.toDomain() },
        tilbakekreving = this.tilbakekreving?.toDomain(),
        brevmottakere = this.brevmottakere.toDomain(),
        avslagÅrsak = this.avslagÅrsak,
    )
}

fun TilbakekrevingDto.toDomain(): Tilbakekrevingsdetaljer {
    return Tilbakekrevingsdetaljer(
        tilbakekrevingsvalg = this.tilbakekrevingsvalg,
        this.tilbakekrevingMedVarsel?.toDomain(),
    )
}

fun TilbakekrevingMedVarselDto.toDomain(): TilbakekrevingMedVarsel {
    return TilbakekrevingMedVarsel(
        varseltekst = this.varseltekst,
        sumFeilutbetaling = this.sumFeilutbetaling,
        perioder = this.fellesperioder.toList(),
    )
}

fun List<BrevmottakerKontrakter>.toDomain(): Brevmottakere =
    Brevmottakere(mottakere = this.map { it.toDomain() })

fun BrevmottakerKontrakter.toDomain(): Brevmottaker = Brevmottaker(
    ident = this.ident,
    navn = this.navn,
    identType = this.identType,
    mottakerRolle = this.mottakerRolle,
)

fun IverksettDagpengerdDto.toDomain(): IverksettDagpenger {
    return IverksettDagpenger(
        fagsak = this.fagsak?.toDomain()
            ?: this.sak?.toDomain()
            ?: this.sakId.tilSak()
            ?: throw IllegalStateException("sakId, sak eller fagsak må ha verdi"),
        søker = this.søker?.toDomain()
            ?: this.personIdent.tilSøker()
            ?: throw IllegalStateException("personIdent eller søker må ha verdi"),
        behandling = this.behandling?.toDomain()
            ?: this.behandlingId?.tilBehandling()
            ?: throw IllegalStateException("behandlingId eller behandling må ha verdi"),
        vedtak = this.vedtak.toDomain(),
        forrigeVedtak = this.forrigeVedtak?.toDomain(),
    )
}

fun UUID?.tilSak(): Fagsakdetaljer? = this?.let { Fagsakdetaljer(it) }
fun String?.tilSøker(): Søker? = this?.let { Søker(personIdent = it, tilhørendeEnhet = "") }

fun UUID?.tilBehandling(): Behandlingsdetaljer? = this?.let {
    Behandlingsdetaljer(
        behandlingId = it,
        behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
        behandlingÅrsak = BehandlingÅrsak.SØKNAD,
    )
}
