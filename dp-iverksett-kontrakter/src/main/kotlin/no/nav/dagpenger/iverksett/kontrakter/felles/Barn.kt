package no.nav.dagpenger.iverksett.kontrakter.felles

import java.time.LocalDate

data class Barn(
    val personIdent: String? = null,
    val termindato: LocalDate? = null,
)
