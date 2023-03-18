package no.nav.dagpenger.iverksett.infrastruktur.util

import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType

fun StønadType.tilKlassifisering() = when (this) {
    StønadType.DAGPENGER -> "DP"
}

fun String.tilStønadstype() = when (this) {
    "DP" -> StønadType.DAGPENGER
    else -> throw IllegalArgumentException("$this er ikke gyldig stønadstype")
}
