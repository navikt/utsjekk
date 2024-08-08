package no.nav.utsjekk.iverksetting.domene.transformer

import no.nav.utsjekk.iverksetting.domene.TilkjentYtelse
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingDto

fun List<UtbetalingDto>.tilTilkjentYtelse(brukersNavKontor: String?): TilkjentYtelse {
    val andeler = this.map { it.toDomain(brukersNavKontor) }

    return when (andeler.size) {
        0 -> tomTilkjentYtelse()
        else -> TilkjentYtelse(andelerTilkjentYtelse = andeler)
    }
}

fun tomTilkjentYtelse() =
    TilkjentYtelse(
        andelerTilkjentYtelse = emptyList(),
    )
