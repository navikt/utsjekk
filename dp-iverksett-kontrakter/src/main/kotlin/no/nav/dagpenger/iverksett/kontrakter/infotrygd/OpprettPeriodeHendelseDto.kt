package no.nav.dagpenger.iverksett.kontrakter.infotrygd

import java.time.LocalDate

data class Periode(val startdato: LocalDate, val sluttdato: LocalDate, val fulleDagpenger: Boolean)
