package no.nav.dagpenger.iverksett.kontrakter.journalføring.dokarkiv

import no.nav.dagpenger.iverksett.kontrakter.felles.Fagsystem

data class Sak(
    val arkivsaksnummer: String? = null,
    val arkivsaksystem: String? = null,
    val fagsakId: String? = null,
    val fagsaksystem: Fagsystem? = null,
    val sakstype: String? = null
)
