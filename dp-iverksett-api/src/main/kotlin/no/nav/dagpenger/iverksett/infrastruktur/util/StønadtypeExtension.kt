package no.nav.dagpenger.iverksett.infrastruktur.util

import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.utbetaling.Fagsystem

fun StønadType.tilKlassifisering() = when (this) {
    StønadType.DAGPENGER -> "DPORAS"
}

fun StønadType.tilFagsystem() = when (this) {
    StønadType.DAGPENGER -> Fagsystem.Dagpenger
}
