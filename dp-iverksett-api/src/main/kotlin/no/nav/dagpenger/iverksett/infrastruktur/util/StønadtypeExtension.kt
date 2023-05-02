package no.nav.dagpenger.iverksett.infrastruktur.util

import no.nav.dagpenger.iverksett.kontrakter.felles.Fagsystem
import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType

fun StønadType.tilKlassifisering() = when (this) {
    StønadType.DAGPENGER -> "DPORAS"
}

fun StønadType.tilFagsystem() = when(this) {
    StønadType.DAGPENGER -> Fagsystem.DP.tema
}
