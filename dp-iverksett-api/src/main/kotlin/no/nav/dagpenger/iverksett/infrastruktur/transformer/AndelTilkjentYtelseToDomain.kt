package no.nav.dagpenger.iverksett.infrastruktur.transformer

import no.nav.dagpenger.iverksett.api.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.kontrakter.felles.Datoperiode
import no.nav.dagpenger.iverksett.kontrakter.iverksett.UtbetalingDto
import java.time.LocalDate

fun UtbetalingDto.toDomain(): AndelTilkjentYtelse {
    return AndelTilkjentYtelse(
        beløp = this.beløp,
        periode = this.periode.let { Datoperiode(it.fom, it.tom ?: LocalDate.MAX) },
        inntekt = this.inntekt ?: 0,
        samordningsfradrag = this.samordningsfradrag ?: 0,
        inntektsreduksjon = this.inntektsreduksjon ?: 0,
        kildeBehandlingId = this.kildeBehandlingId,
    )
}
