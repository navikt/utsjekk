package no.nav.dagpenger.iverksett.utbetaling.domene.transformer

import java.time.LocalDateTime
import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.domene.Vedtaksdetaljer
import no.nav.dagpenger.kontrakter.iverksett.ForrigeIverksettingDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto

fun List<UtbetalingDto>.tilTilkjentYtelse(): TilkjentYtelse {
    val andeler = this.sammenslått().map { it.toDomain() }

    return when (andeler.size) {
        0 -> tomTilkjentYtelse()
        else -> TilkjentYtelse(andelerTilkjentYtelse = andeler)
    }
}

fun List<UtbetalingDto>.sammenslått(): List<UtbetalingDto> {
    return this.sortedBy { it.fraOgMedDato }.fold(emptyList()) { utbetalinger, utbetaling ->
        if (utbetalinger.isEmpty()) {
            return@fold listOf(utbetaling)
        }

        val last = utbetalinger.last()

        if (utbetaling.kanSlåsSammen(last))
            utbetalinger.dropLast(1) + listOf(last.copy(tilOgMedDato = utbetaling.tilOgMedDato))
        else
            utbetalinger + listOf(utbetaling)
    }
}

private fun UtbetalingDto.kanSlåsSammen(forrige: UtbetalingDto): Boolean {
    return this.beløpPerDag == forrige.beløpPerDag
            && this.stønadsdata == forrige.stønadsdata
            && this.fraOgMedDato == forrige.tilOgMedDato.plusDays(1)
}

fun ForrigeIverksettingDto.tilVedtaksdetaljer(): Vedtaksdetaljer {
    return Vedtaksdetaljer(
        vedtakstidspunkt = LocalDateTime.now(),
        saksbehandlerId = "A123456",
        beslutterId = "B123456",
        tilkjentYtelse = tomTilkjentYtelse(),
    )
}

fun tomTilkjentYtelse() = TilkjentYtelse(
    andelerTilkjentYtelse = emptyList(),
)
