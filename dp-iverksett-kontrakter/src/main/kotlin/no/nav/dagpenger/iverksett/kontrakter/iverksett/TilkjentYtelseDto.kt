package no.nav.dagpenger.iverksett.kontrakter.iverksett

import java.time.LocalDate

data class TilkjentYtelseDto(
    val utbetalinger: List<UtbetalingDto>,
    val startdato: LocalDate,
)
