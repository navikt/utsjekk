package no.nav.dagpenger.iverksett.infrastruktur.transformer

import java.time.LocalDate
import no.nav.dagpenger.iverksett.api.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.Søker
import no.nav.dagpenger.iverksett.api.domene.TilbakekrevingMedVarsel
import no.nav.dagpenger.iverksett.api.domene.Tilbakekrevingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerDagpenger
import no.nav.dagpenger.iverksett.api.domene.VedtaksperiodeDagpenger
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.felles.SakIdentifikator
import no.nav.dagpenger.kontrakter.iverksett.BehandlingType
import no.nav.dagpenger.kontrakter.iverksett.BehandlingÅrsak
import no.nav.dagpenger.kontrakter.iverksett.ForrigeIverksettingDto
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.TilbakekrevingDto
import no.nav.dagpenger.kontrakter.iverksett.TilbakekrevingMedVarselDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto

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

fun IverksettDto.toDomain(): IverksettDagpenger {
    return IverksettDagpenger(
        fagsak = this.tilFagsak(),
        søker = this.personIdent.tilSøker(),
        behandling = this.tilBehandling(),
        vedtak = this.vedtak.toDomain(),
        forrigeIverksetting = this.tilForrigeIverksetting(),
    )
}

fun IverksettDto.tilFagsak(): Fagsakdetaljer {
    return Fagsakdetaljer(
        fagsakId = this.sakId,
        saksreferanse = this.saksreferanse
    )
}

fun IverksettDto.tilSakIdentifikator(): SakIdentifikator {
    return SakIdentifikator(this.sakId, this.saksreferanse)
}


fun String.tilSøker(): Søker = Søker(personIdent = this, tilhørendeEnhet = "")

fun IverksettDto.tilBehandling(): Behandlingsdetaljer = Behandlingsdetaljer(
    behandlingId = this.behandlingId,
    forrigeBehandlingId = this.forrigeIverksetting?.behandlingId,
    behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
    behandlingÅrsak = BehandlingÅrsak.SØKNAD,
)

fun ForrigeIverksettingDto.tilBehandling(): Behandlingsdetaljer = Behandlingsdetaljer(
    behandlingId = this.behandlingId,
    behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
    behandlingÅrsak = BehandlingÅrsak.SØKNAD,
)

fun IverksettDto.tilForrigeIverksetting(): IverksettDagpenger? {
    return when (this.forrigeIverksetting) {
        null -> null
        else -> IverksettDagpenger(
            fagsak = this.tilFagsak(),
            søker = this.personIdent.tilSøker(),
            behandling = this.forrigeIverksetting!!.tilBehandling(),
            vedtak = this.forrigeIverksetting!!.tilVedtaksdetaljer(),
            forrigeIverksetting = null,
        )
    }
}
