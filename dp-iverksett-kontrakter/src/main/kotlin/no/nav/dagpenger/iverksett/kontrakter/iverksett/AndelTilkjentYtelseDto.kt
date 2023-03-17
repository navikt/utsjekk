package no.nav.dagpenger.iverksett.kontrakter.iverksett

import no.nav.dagpenger.iverksett.kontrakter.felles.Månedsperiode
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

data class AndelTilkjentYtelseDto(
    val beløp: Int,
    val inntekt: Int,
    val inntektsreduksjon: Int,
    val samordningsfradrag: Int,
    val periode: Månedsperiode,
    val kildeBehandlingId: UUID? = null,
)
