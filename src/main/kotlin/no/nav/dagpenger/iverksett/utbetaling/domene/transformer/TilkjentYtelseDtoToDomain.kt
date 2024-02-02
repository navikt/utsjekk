package no.nav.dagpenger.iverksett.utbetaling.domene.transformer

import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto

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
