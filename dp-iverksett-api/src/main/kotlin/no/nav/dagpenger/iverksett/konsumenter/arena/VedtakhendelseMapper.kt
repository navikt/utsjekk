package no.nav.dagpenger.iverksett.konsumenter.arena

import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.kontrakter.felles.Behandlingstema
import no.nav.dagpenger.iverksett.kontrakter.felles.Vedtaksresultat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

fun mapIverksettTilVedtakHendelser(iverksettData: IverksettDagpenger, aktørId: String): VedtakHendelser {
    return VedtakHendelser(
        aktoerID = aktørId,
        avslutningsstatus = mapAvslutningsstatus(iverksettData.vedtak.vedtaksresultat),
        behandlingstema = Behandlingstema.valueOf(
            iverksettData.fagsak.stønadstype.name.lowercase(Locale.getDefault())
                .replaceFirstChar { it.uppercase() },
        ).value,
        hendelsesprodusentREF = "DP",
        applikasjonSakREF = iverksettData.fagsak.fagsakId.toString(),
        hendelsesTidspunkt = LocalDateTime.now().format(dateTimeFormatter),
    )
}

private fun mapAvslutningsstatus(vedtaksresultat: Vedtaksresultat): String {
    return when (vedtaksresultat) {
        Vedtaksresultat.INNVILGET -> "innvilget"
        Vedtaksresultat.OPPHØRT -> "opphoert"
        else -> error("Håndterer ikke restultat $vedtaksresultat mot arena")
    }
}
