package no.nav.dagpenger.iverksett.infrastruktur.util

import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType

fun StønadType.tilKlassifisering() = when (this) {
    StønadType.DAGPENGER -> "DPORAS"
}
