package no.nav.dagpenger.iverksett.infrastruktur.transformer

import no.nav.dagpenger.iverksett.api.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.api.domene.Iverksett
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
import no.nav.dagpenger.kontrakter.iverksett.ForrigeIverksettingDto
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
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

fun IverksettDto.toDomain(): IverksettDagpenger {
    return IverksettDagpenger(
        fagsak = this.tilSak(),
        søker = this.personIdent.tilSøker(),
        behandling = this.tilBehandling(),
        vedtak = this.vedtak.toDomain(),
        forrigeIverksetting = this.tilForrigeIverksetting(),
    )
}

fun IverksettDto.tilSak(): Fagsakdetaljer {
    val saksidentifikator =
        this.sakId?.toString() ?: this.saksreferanse
        ?: throw IllegalStateException("sakId eller saksreferanse må være satt")
    return Fagsakdetaljer(saksidentifikator)
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
            fagsak = this.tilSak(),
            søker = this.personIdent.tilSøker(),
            behandling = this.forrigeIverksetting!!.tilBehandling(),
            vedtak = this.forrigeIverksetting!!.tilVedtaksdetaljer(),
            forrigeIverksetting = null,
        )
    }
}
