package no.nav.dagpenger.iverksett.konsumenter.brev

import no.nav.dagpenger.iverksett.kontrakter.iverksett.IverksettDagpenger
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingType
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.Vedtaksresultat
import no.nav.dagpenger.iverksett.kontrakter.journalføring.dokarkiv.Dokumenttype
import no.nav.dagpenger.kontrakter.utbetaling.StønadType

fun stønadstypeTilDokumenttype(stønadType: StønadType) =
    when (stønadType) {
        StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER,
        StønadType.DAGPENGER_PERMITTERING_ORDINAER,
        StønadType.DAGPENGER_PERMITTERING_FISKEINDUSTRI,
        StønadType.DAGPENGER_EOS,
        -> Dokumenttype.DAGPENGER_FRITTSTÅENDE_BREV
    }

fun vedtaksbrevForStønadType(stønadType: StønadType): Dokumenttype =
    when (stønadType) {
        StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER,
        StønadType.DAGPENGER_PERMITTERING_ORDINAER,
        StønadType.DAGPENGER_PERMITTERING_FISKEINDUSTRI,
        StønadType.DAGPENGER_EOS,
        -> Dokumenttype.VEDTAKSBREV_DAGPENGER
    }

fun lagStønadtypeTekst(stønadstype: StønadType): String =
    when (stønadstype) {
        StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER,
        StønadType.DAGPENGER_PERMITTERING_ORDINAER,
        StønadType.DAGPENGER_PERMITTERING_FISKEINDUSTRI,
        StønadType.DAGPENGER_EOS,
        -> "dagpenger"
    }

fun lagVedtakstekst(iverksettData: IverksettDagpenger): String =
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

private fun lagVedtakstekstFørstegangsbehandling(iverksettData: IverksettDagpenger) =
    when (iverksettData.vedtak.vedtaksresultat) {
        Vedtaksresultat.INNVILGET -> "Vedtak om innvilget "
        Vedtaksresultat.AVSLÅTT -> "Vedtak om avslått "
        Vedtaksresultat.OPPHØRT -> error("Førstegangsbehandling kan ikke ha resultat Opphørt")
    }
