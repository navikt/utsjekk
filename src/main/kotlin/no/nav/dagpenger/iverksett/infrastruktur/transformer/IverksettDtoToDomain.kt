package no.nav.dagpenger.iverksett.infrastruktur.transformer

import no.nav.dagpenger.iverksett.api.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.Søker
import no.nav.dagpenger.iverksett.api.domene.TilbakekrevingMedVarsel
import no.nav.dagpenger.iverksett.api.domene.Tilbakekrevingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerDagpenger
import no.nav.dagpenger.iverksett.api.domene.VedtaksperiodeDagpenger
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottaker
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottakere
import no.nav.dagpenger.kontrakter.felles.BrevmottakerDto
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.iverksett.BehandlingType
import no.nav.dagpenger.kontrakter.iverksett.BehandlingÅrsak
import no.nav.dagpenger.kontrakter.iverksett.IverksettDagpengerdDto
import no.nav.dagpenger.kontrakter.iverksett.TilbakekrevingDto
import no.nav.dagpenger.kontrakter.iverksett.TilbakekrevingMedVarselDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import java.time.LocalDate
import java.util.UUID

fun VedtaksperiodeDto.toDomain(): VedtaksperiodeDagpenger {
    return VedtaksperiodeDagpenger(
        periode = Datoperiode(this.fraOgMedDato, this.tilOgMedDato ?: LocalDate.MAX),
        periodeType = this.periodeType,
    )
}

fun VedtaksdetaljerDto.toDomain(): VedtaksdetaljerDagpenger {
    return VedtaksdetaljerDagpenger(
        vedtakstype = this.vedtakstype,
        vedtaksresultat = this.resultat,
        vedtakstidspunkt = this.vedtakstidspunkt,
        opphørÅrsak = this.opphorAarsak,
        saksbehandlerId = this.saksbehandlerId,
        beslutterId = this.beslutterId,
        tilkjentYtelse = this.utbetalinger.tilTilkjentYtelse(),
        vedtaksperioder = this.vedtaksperioder.map { it.toDomain() },
        tilbakekreving = this.tilbakekreving?.toDomain(),
        brevmottakere = this.brevmottakere.toDomain(),
        avslagÅrsak = this.avslagAarsak,
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

fun List<BrevmottakerDto>.toDomain(): Brevmottakere =
    Brevmottakere(mottakere = this.map { it.toDomain() })

fun BrevmottakerDto.toDomain(): Brevmottaker = Brevmottaker(
    ident = this.ident,
    navn = this.navn,
    identType = this.identType,
    mottakerRolle = this.mottakerRolle,
)

fun IverksettDagpengerdDto.toDomain(): IverksettDagpenger {
    return IverksettDagpenger(
        fagsak = this.sakId.tilSak()
            ?: throw IllegalStateException("sakId, sak eller fagsak må ha verdi"),
        søker = this.personIdent.tilSøker()
            ?: throw IllegalStateException("personIdent eller søker må ha verdi"),
        behandling = this.behandlingId?.tilBehandling()
            ?: throw IllegalStateException("behandlingId eller behandling må ha verdi"),
        vedtak = this.vedtak.toDomain(),
        forrigeVedtak = this.utbetalingerPaaForrigeVedtak.tilVedtaksdetaljer(),
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
