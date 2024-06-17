package no.nav.utsjekk.simulering.api

import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.iverksett.ForrigeIverksettingDto
import no.nav.utsjekk.kontrakter.iverksett.ForrigeIverksettingTilleggsstønaderDto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingDto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingTilleggsstønaderDto
import java.time.LocalDateTime

data class SimuleringRequestTilleggsstønaderDto(
    val sakId: String,
    val behandlingId: String,
    val personident: Personident,
    val saksbehandler: String,
    val vedtakstidspunkt: LocalDateTime,
    val utbetalinger: List<UtbetalingTilleggsstønaderDto>,
    val forrigeIverksetting: ForrigeIverksettingTilleggsstønaderDto? = null,
)

data class SimuleringRequestDto(
    val sakId: String,
    val behandlingId: String,
    val personident: Personident,
    val saksbehandler: String,
    val vedtakstidspunkt: LocalDateTime,
    val utbetalinger: List<UtbetalingDto>,
    val forrigeIverksetting: ForrigeIverksettingDto? = null,
)
