package no.nav.dagpenger.iverksett.utbetaling.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.dagpenger.kontrakter.felles.BrukersNavKontor
import no.nav.dagpenger.kontrakter.felles.GeneriskId
import no.nav.dagpenger.kontrakter.felles.Personident
import no.nav.dagpenger.kontrakter.felles.StønadTypeTilleggsstønader
import java.time.LocalDate
import java.time.LocalDateTime

data class IverksettTilleggsstønaderDto(
    @Schema(required = true)
    val sakId: GeneriskId,
    @Schema(required = true)
    val behandlingId: GeneriskId,
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
    val stønadstype: StønadTypeTilleggsstønader,
    val brukersNavKontor: BrukersNavKontor? = null,
)

@Suppress("unused")
enum class Satstype {
    DAG,
    MND,
    ENG,
}

data class ForrigeIverksettingTilleggsstønaderDto(
    @Schema(required = true)
    val behandlingId: GeneriskId,
    val iverksettingId: String? = null,
)
