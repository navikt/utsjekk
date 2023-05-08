package no.nav.dagpenger.iverksett.kontrakter.iverksett

import no.nav.dagpenger.iverksett.kontrakter.felles.Datoperiode
import java.time.LocalDate
import java.util.UUID

data class UtbetalingDto(
    val beløp: Int,
    val fraOgMedDato: LocalDate? = null,
    val tilOgMedDato: LocalDate? = null,
    @Deprecated("Bruk fraOgMedDato og tilOgMedDato")
    val periode: DatoperiodeDto? = null,
    val inntekt: Int? = null,
    val inntektsreduksjon: Int? = null,
    val samordningsfradrag: Int? = null,
    val kildeBehandlingId: UUID? = null,
)
