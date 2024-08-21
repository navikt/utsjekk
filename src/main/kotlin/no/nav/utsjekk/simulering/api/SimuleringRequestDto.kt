package no.nav.utsjekk.simulering.api

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.iverksett.ForrigeIverksettingTilleggsstønaderDto
import no.nav.utsjekk.kontrakter.iverksett.ForrigeIverksettingV2Dto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingTilleggsstønaderDto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingV2Dto
import java.time.LocalDateTime

data class SimuleringRequestTilleggsstønaderDto(
    val sakId: String,
    val behandlingId: String,
    @Schema(required = true, description = "Fødselsnummer eller D-nummer", example = "15507600333", type = "string")
    val personident: Personident,
    val saksbehandler: String,
    val vedtakstidspunkt: LocalDateTime,
    val utbetalinger: List<UtbetalingTilleggsstønaderDto>,
    val forrigeIverksetting: ForrigeIverksettingTilleggsstønaderDto? = null,
)

data class SimuleringRequestV2Dto(
    val sakId: String,
    val behandlingId: String,
    @Schema(required = true, description = "Fødselsnummer eller D-nummer", example = "15507600333", type = "string")
    val personident: Personident,
    val saksbehandlerId: String,
    val utbetalinger: List<UtbetalingV2Dto>,
    val forrigeIverksetting: ForrigeIverksettingV2Dto? = null,
)
