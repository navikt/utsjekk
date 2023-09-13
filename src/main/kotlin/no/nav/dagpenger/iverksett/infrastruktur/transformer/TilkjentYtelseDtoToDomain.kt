package no.nav.dagpenger.iverksett.infrastruktur.transformer

import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerDagpenger
import no.nav.dagpenger.kontrakter.iverksett.ForrigeIverksettingDto
import no.nav.dagpenger.kontrakter.iverksett.TilkjentYtelseDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import java.time.LocalDateTime

fun TilkjentYtelseDto.toDomain(): TilkjentYtelse {
    return TilkjentYtelse(
        andelerTilkjentYtelse = this.utbetalinger.map { it.toDomain() },
    )
}

fun List<UtbetalingDto>.tilTilkjentYtelse(): TilkjentYtelse? {
    val andeler = this.sammenslått().map { it.toDomain() }

    return when (andeler.size) {
        0 -> null
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
    return this.belopPerDag == forrige.belopPerDag
            && this.stonadstype == forrige.stonadstype
            && this.ferietillegg == forrige.ferietillegg
            && this.fraOgMedDato == forrige.tilOgMedDato.plusDays(1)
}

fun ForrigeIverksettingDto.tilVedtaksdetaljer(): VedtaksdetaljerDagpenger {
    return VedtaksdetaljerDagpenger(
        vedtakstype = VedtakType.UTBETALINGSVEDTAK,
        vedtaksresultat = Vedtaksresultat.INNVILGET,
        vedtakstidspunkt = LocalDateTime.now(),
        opphørÅrsak = null,
        saksbehandlerId = "A123456",
        beslutterId = "B123456",
        tilkjentYtelse = this.utbetalinger.tilTilkjentYtelse(),
    )
}

fun tomTilkjentYtelse() = TilkjentYtelse(
    andelerTilkjentYtelse = emptyList(),
)
