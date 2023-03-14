package no.nav.dagpenger.iverksett.util

import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType

fun StønadType.tilKlassifisering() = when (this) {
    StønadType.OVERGANGSSTØNAD -> "EFOG"
    StønadType.BARNETILSYN -> "EFBT"
    StønadType.SKOLEPENGER -> "EFSP"
}

fun String.tilStønadstype() = when (this) {
    "EFOG" -> StønadType.OVERGANGSSTØNAD
    "EFBT" -> StønadType.BARNETILSYN
    "EFSP" -> StønadType.SKOLEPENGER
    else -> throw IllegalArgumentException("$this er ikke gyldig stønadstype")
}
