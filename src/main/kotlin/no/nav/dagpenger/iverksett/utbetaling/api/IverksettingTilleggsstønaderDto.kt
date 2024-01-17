package no.nav.dagpenger.iverksett.utbetaling.api

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import no.nav.dagpenger.kontrakter.felles.BrukersNavKontor
import no.nav.dagpenger.kontrakter.felles.Personident
import java.time.LocalDate
import java.time.LocalDateTime

data class IverksettTilleggsstønaderDto(
    @Size(min = 1, max = 20)
    @Schema(required = true)
    val saksreferanse: String,
    @Size(min = 1, max = 30)
    @Schema(required = true)
    val behandlingId: String,
    val iverksettingId: String?,
    @Schema(required = true, description = "Fødselsnummer eller D-nummer", example = "15507600333", type = "string")
    val personident: Personident,
    @Schema(required = true)
    val vedtak: VedtaksdetaljerTilleggsstønaderDto,
    @Schema(description = "Må være satt hvis det ikke er første iverksetting på saken")
    val forrigeIverksetting: ForrigeIverksettingTilleggsstønaderDto? = null,
)

data class VedtaksdetaljerTilleggsstønaderDto(
    @Schema(required = true)
    val vedtakstidspunkt: LocalDateTime,
    @Schema(
        required = true,
        description = "NAV-ident til saksbehandler, eller servicebruker til applikasjon dersom vedtaket er fattet fullautomatisk",
        pattern = "^[A-Z]\\d{6}\$",
        example = "Z123456",
    )
    val saksbehandlerId: String,
    @Schema(
        required = true,
        description = "NAV-ident til beslutter, eller servicebruker til applikasjon dersom vedtaket er fattet fullautomatisk",
        pattern = "^[A-Z]\\d{6}\$",
        example = "Z123456",
    )
    val beslutterId: String,
    @Schema(required = false)
    val utbetalinger: List<UtbetalingTilleggsstønaderDto> = emptyList(),
)

data class UtbetalingTilleggsstønaderDto(
    @Schema(description = "Må være et positivt heltall")
    val beløp: Int,
    val satstype: Satstype,
    val fraOgMedDato: LocalDate,
    val tilOgMedDato: LocalDate,
    val klassifiseringskode: String,
    val brukersNavKontor: BrukersNavKontor? = null,
)

@Suppress("unused")
enum class Satstype {
    DAG,
    MND,
    ENG,
}

data class ForrigeIverksettingTilleggsstønaderDto(
    @Size(min = 1, max = 30)
    @Schema(required = true)
    val behandlingId: String,
    val iverksettingId: String? = null,
)
