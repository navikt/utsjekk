package no.nav.dagpenger.iverksett.infrastruktur.transformer

import no.nav.dagpenger.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.familie.kontrakter.ef.iverksett.AndelTilkjentYtelseDto

fun AndelTilkjentYtelseDto.toDomain(): AndelTilkjentYtelse {
    return AndelTilkjentYtelse(
        beløp = this.beløp,
        periode = this.periode,
        inntekt = this.inntekt,
        samordningsfradrag = this.samordningsfradrag,
        inntektsreduksjon = this.inntektsreduksjon,
        kildeBehandlingId = this.kildeBehandlingId,
    )
}
