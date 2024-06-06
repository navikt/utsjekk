package no.nav.utsjekk.simulering.api

import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.iverksett.ForrigeIverksettingTilleggsstønaderDto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingTilleggsstønaderDto
import java.time.LocalDateTime

data class SimuleringRequestDto(
    val sakId: String,
    val behandlingId: String,
    val personident: Personident,
    val vedtakstidspunkt: LocalDateTime,
    val utbetalinger: List<UtbetalingTilleggsstønaderDto>,
    val forrigeIverksetting: ForrigeIverksettingTilleggsstønaderDto? = null,
)
