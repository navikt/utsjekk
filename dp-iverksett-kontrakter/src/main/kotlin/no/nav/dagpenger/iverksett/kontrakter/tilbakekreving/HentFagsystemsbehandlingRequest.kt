package no.nav.dagpenger.iverksett.kontrakter.tilbakekreving

import no.nav.dagpenger.iverksett.kontrakter.felles.Regelverk
import no.nav.dagpenger.iverksett.kontrakter.felles.Språkkode
import java.time.LocalDate

data class HentFagsystemsbehandlingRequest(
    val eksternFagsakId: String,
    val ytelsestype: Ytelsestype,
    val eksternId: String,
)

data class HentFagsystemsbehandlingRespons(
    val feilMelding: String? = null,
    val hentFagsystemsbehandling: HentFagsystemsbehandling? = null,
)

data class HentFagsystemsbehandling(
    val eksternFagsakId: String,
    val ytelsestype: Ytelsestype,
    val regelverk: Regelverk? = null,
    val eksternId: String,
    val personIdent: String,
    val språkkode: Språkkode = Språkkode.NB,
    val enhetId: String,
    val enhetsnavn: String,
    val revurderingsvedtaksdato: LocalDate,
    val verge: Verge? = null,
    val faktainfo: Faktainfo,
    val institusjon: Institusjon? = null,
)
