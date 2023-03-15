package no.nav.dagpenger.iverksett.kontrakter.journalføring.dokarkiv

import no.nav.dagpenger.iverksett.kontrakter.felles.Språkkode

data class Førsteside(
    val språkkode: Språkkode = Språkkode.NB,
    val navSkjemaId: String,
    val overskriftstittel: String
)
