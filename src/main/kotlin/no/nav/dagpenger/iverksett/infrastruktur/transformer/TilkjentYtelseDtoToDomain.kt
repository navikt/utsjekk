package no.nav.dagpenger.iverksett.infrastruktur.transformer

import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelseMedMetaData
import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerDagpenger
import no.nav.dagpenger.kontrakter.iverksett.TilkjentYtelseDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingerMedMetadataDto
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import java.time.LocalDate
import java.time.LocalDateTime

fun TilkjentYtelseDto.toDomain(): TilkjentYtelse {
    return TilkjentYtelse(
        andelerTilkjentYtelse = this.utbetalinger.map { it.toDomain() },
        startdato = this.startdato,
    )
}

fun UtbetalingerMedMetadataDto.toDomain(): TilkjentYtelseMedMetaData {
    return TilkjentYtelseMedMetaData(
        tilkjentYtelse = this.utbetalinger.tilTilkjentYtelse() ?: tomTilkjentYtelse(),
        saksbehandlerId = this.saksbehandlerId,
        stønadstype = this.stønadstype,
        sakId = this.sakId,
        personIdent = this.personIdent,
        behandlingId = this.behandlingId,
        vedtaksdato = this.vedtaksdato,
    )
}

fun Iterable<UtbetalingDto>.tilTilkjentYtelse(): TilkjentYtelse? {
    val andeler = this.map { it.toDomain() }
    val startdato = andeler.minOfOrNull { it.periode.fom } ?: LocalDate.now()

    return when (andeler.size) {
        0 -> null
        else -> TilkjentYtelse(andelerTilkjentYtelse = andeler, startdato = startdato)
    }
}

fun Iterable<UtbetalingDto>.tilVedtaksdetaljer(): VedtaksdetaljerDagpenger {
    return VedtaksdetaljerDagpenger(
        vedtakstype = VedtakType.UTBETALINGSVEDTAK,
        vedtaksresultat = Vedtaksresultat.INNVILGET,
        vedtakstidspunkt = LocalDateTime.now(),
        opphørÅrsak = null,
        saksbehandlerId = "A123456",
        beslutterId = "B123456",
        tilkjentYtelse = this.tilTilkjentYtelse(),
    )
}

fun tomTilkjentYtelse() = TilkjentYtelse(
    andelerTilkjentYtelse = emptyList(),
    startdato = LocalDate.now(),
)
