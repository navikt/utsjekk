package no.nav.dagpenger.iverksett.konsumenter.brev

import no.nav.dagpenger.iverksett.api.domene.IverksettOvergangsstønad
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingType
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import no.nav.dagpenger.iverksett.kontrakter.felles.Vedtaksresultat
import no.nav.dagpenger.iverksett.kontrakter.journalføring.dokarkiv.Dokumenttype

fun stønadstypeTilDokumenttype(stønadType: StønadType) =
    when (stønadType) {
        StønadType.OVERGANGSSTØNAD -> Dokumenttype.OVERGANGSSTØNAD_FRITTSTÅENDE_BREV
        StønadType.SKOLEPENGER -> Dokumenttype.SKOLEPENGER_FRITTSTÅENDE_BREV
        StønadType.BARNETILSYN -> Dokumenttype.BARNETILSYN_FRITTSTÅENDE_BREV
    }

fun vedtaksbrevForStønadType(stønadType: StønadType): Dokumenttype =
    when (stønadType) {
        StønadType.OVERGANGSSTØNAD -> Dokumenttype.VEDTAKSBREV_OVERGANGSSTØNAD
        StønadType.BARNETILSYN -> Dokumenttype.VEDTAKSBREV_BARNETILSYN
        StønadType.SKOLEPENGER -> Dokumenttype.VEDTAKSBREV_SKOLEPENGER
    }

fun lagStønadtypeTekst(stønadstype: StønadType): String =
    when (stønadstype) {
        StønadType.OVERGANGSSTØNAD -> "overgangstønad"
        StønadType.BARNETILSYN -> "stønad til barnetilsyn"
        StønadType.SKOLEPENGER -> "stønad til skolepenger"
    }

fun lagVedtakstekst(iverksettData: IverksettOvergangsstønad): String =
    when {
        iverksettData.behandling.behandlingType === BehandlingType.FØRSTEGANGSBEHANDLING ->
            lagVedtakstekstFørstegangsbehandling(iverksettData)
        iverksettData.behandling.behandlingÅrsak === BehandlingÅrsak.SANKSJON_1_MND -> "Vedtak om sanksjon av "
        iverksettData.vedtak.vedtaksresultat === Vedtaksresultat.AVSLÅTT -> "Vedtak om avslått "
        iverksettData.vedtak.vedtaksresultat === Vedtaksresultat.OPPHØRT -> "Vedtak om opphørt "
        iverksettData.vedtak.vedtaksresultat === Vedtaksresultat.INNVILGET &&
            iverksettData.behandling.behandlingÅrsak === BehandlingÅrsak.SØKNAD -> "Vedtak om innvilget "
        else -> "Vedtak om revurdert "
    }

private fun lagVedtakstekstFørstegangsbehandling(iverksettData: IverksettOvergangsstønad) =
    when (iverksettData.vedtak.vedtaksresultat) {
        Vedtaksresultat.INNVILGET -> "Vedtak om innvilget "
        Vedtaksresultat.AVSLÅTT -> "Vedtak om avslått "
        Vedtaksresultat.OPPHØRT -> error("Førstegangsbehandling kan ikke ha resultat Opphørt")
    }
