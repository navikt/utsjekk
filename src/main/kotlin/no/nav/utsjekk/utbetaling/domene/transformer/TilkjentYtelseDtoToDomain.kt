package no.nav.utsjekk.utbetaling.domene.transformer

import no.nav.utsjekk.kontrakter.iverksett.UtbetalingDto
import no.nav.utsjekk.utbetaling.domene.TilkjentYtelse

fun List<UtbetalingDto>.tilTilkjentYtelse(): TilkjentYtelse {
    val andeler = this.map { it.toDomain() }

    return when (andeler.size) {
        0 -> tomTilkjentYtelse()
        else -> TilkjentYtelse(andelerTilkjentYtelse = andeler)
    }
}

fun tomTilkjentYtelse() =
    TilkjentYtelse(
        andelerTilkjentYtelse = emptyList(),
    )
