package no.nav.dagpenger.iverksett.kontrakter.iverksett

import no.nav.dagpenger.iverksett.kontrakter.felles.Datoperiode
import java.util.UUID

data class UtbetalingDto(
    val beløp: Int,
    val inntekt: Int,
    val inntektsreduksjon: Int,
    val samordningsfradrag: Int,
    val periode: Datoperiode,
    val kildeBehandlingId: UUID? = null,
)
