package no.nav.dagpenger.iverksett.kontrakter.iverksett

import java.time.LocalDate
import java.time.YearMonth

data class TilkjentYtelseDto(
    val andelerTilkjentYtelse: List<AndelTilkjentYtelseDto>,
    val startdato: LocalDate,
)
