package no.nav.dagpenger.iverksett.konsumenter.tilbakekreving

import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.Tilbakekrevingsdetaljer
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.Enhet
import no.nav.dagpenger.iverksett.kontrakter.felles.Språkkode
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Behandlingstype
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Faktainfo
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.HentFagsystemsbehandling
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.HentFagsystemsbehandlingRespons
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Periode
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Tilbakekrevingsvalg
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Varsel
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Ytelsestype
import no.nav.dagpenger.kontrakter.utbetaling.Fagsystem

const val ENHETSNAVN_BREV = "NAV Arbeid og ytelser"

fun Tilbakekrevingsdetaljer?.validerTilbakekreving(): Boolean {
    try {
        this?.also { lagVarsel(it) }
    } catch (e: IllegalStateException) {
        return false
    }
    return true
}

fun IverksettDagpenger.tilOpprettTilbakekrevingRequest(enhet: Enhet) =
    OpprettTilbakekrevingRequest(
        fagsystem = Fagsystem.Dagpenger,
        ytelsestype = Ytelsestype.valueOf(this.fagsak.stønadstype.name),
        eksternFagsakId = this.fagsak.fagsakId.toString(),
        personIdent = this.søker.personIdent,
        eksternId = this.behandling.behandlingId.toString(),
        behandlingstype = Behandlingstype.TILBAKEKREVING, // samme som BAKS gjør
        manueltOpprettet = false, // manuelt opprettet ennå ikke støttet i familie-tilbake?
        språkkode = Språkkode.NB, // Bør følge med iverksett.søker
        enhetId = enhet.enhetId, // iverksett.søker.tilhørendeEnhet?
        enhetsnavn = ENHETSNAVN_BREV, // Det som kommer etter "Med vennlig hilsen" i tilbakekrevingsbrev.
        saksbehandlerIdent = this.vedtak.saksbehandlerId,
        varsel = this.vedtak.tilbakekreving?.let { lagVarsel(it) },
        revurderingsvedtaksdato = this.vedtak.vedtakstidspunkt.toLocalDate(),
        verge = null, // Verge er per nå ikke støttet
        faktainfo = lagFaktainfo(this),
    )

fun IverksettDagpenger.tilFagsystembehandling(enhet: Enhet) =
    HentFagsystemsbehandlingRespons(
        hentFagsystemsbehandling =
        HentFagsystemsbehandling(
            fagsakId = this.fagsak.fagsakId,
            behandlingId = this.behandling.behandlingId,
            ytelsestype = Ytelsestype.valueOf(this.fagsak.stønadstype.name),
            personIdent = this.søker.personIdent,
            språkkode = Språkkode.NB,
            enhetId = enhet.enhetId,
            enhetsnavn = enhet.enhetNavn,
            revurderingsvedtaksdato = this.vedtak.vedtakstidspunkt.toLocalDate(),
            faktainfo = lagFaktainfo(this),
        ),
    )

private fun lagVarsel(tilbakekrevingsdetaljer: Tilbakekrevingsdetaljer): Varsel? {
    return when (tilbakekrevingsdetaljer.tilbakekrevingsvalg) {
        Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL ->
            Varsel(
                tilbakekrevingsdetaljer.tilbakekrevingMedVarsel?.varseltekst
                    ?: error("varseltekst er påkrevd for å map'e TilbakekrevingMedVarsel til Varsel"),
                tilbakekrevingsdetaljer.tilbakekrevingMedVarsel.sumFeilutbetaling
                    ?: error("sumFeilutbetaling er påkrevd for å map'e TilbakekrevingMedVarsel til Varsel"),
                tilbakekrevingsdetaljer.tilbakekrevingMedVarsel.perioder?.map { Periode(it.fom, it.tom) }
                    ?: error("perioder er påkrevd for å map'e TilbakekrevingMedVarsel til Varsel"),
            )
        else -> null
    }
}

private fun lagFaktainfo(iverksett: IverksettDagpenger): Faktainfo {
    return Faktainfo(
        revurderingsårsak = iverksett.behandling.behandlingÅrsak.visningsTekst(),
        revurderingsresultat = iverksett.vedtak.vedtaksresultat.visningsnavn,
        tilbakekrevingsvalg = iverksett.vedtak.tilbakekreving?.tilbakekrevingsvalg,
        konsekvensForYtelser = emptySet(), // Settes også empty av ba-sak
    )
}

private fun BehandlingÅrsak.visningsTekst(): String {
    return when (this) {
        BehandlingÅrsak.SØKNAD -> "Søknad"
        BehandlingÅrsak.KLAGE -> "Klage"
        BehandlingÅrsak.NYE_OPPLYSNINGER -> "Nye opplysninger"
        BehandlingÅrsak.KORRIGERING_UTEN_BREV -> "Korrigering uten brev"
        BehandlingÅrsak.PAPIRSØKNAD -> "Papirsøknad"
        BehandlingÅrsak.G_OMREGNING -> "G-omregning"
        BehandlingÅrsak.SATSENDRING -> "Satsendring"

        BehandlingÅrsak.MIGRERING,
        BehandlingÅrsak.SANKSJON_1_MND,
        -> error("Skal ikke gi tilbakekreving for årsak=$this")
    }
}
