package no.nav.dagpenger.iverksett.kontrakter.journalføring

import no.nav.dagpenger.iverksett.kontrakter.felles.Tema

data class JournalposterForBrukerRequest(
    val brukerId: Bruker,
    val antall: Int,
    val tema: List<Tema>? = null,
    val journalposttype: List<Journalposttype>? = null
)
