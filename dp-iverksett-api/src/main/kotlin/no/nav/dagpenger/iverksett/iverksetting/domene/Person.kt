package no.nav.dagpenger.iverksett.iverksetting.domene

import java.time.LocalDate

data class Person(
    val personIdent: String? = null,
)

data class Barn(
    val personIdent: String? = null,
    val termindato: LocalDate? = null,
)
