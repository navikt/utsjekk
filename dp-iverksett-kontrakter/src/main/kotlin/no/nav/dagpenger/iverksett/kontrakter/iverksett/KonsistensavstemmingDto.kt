package no.nav.dagpenger.iverksett.kontrakter.iverksett

import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import java.time.LocalDateTime
import java.util.UUID

data class KonsistensavstemmingDto(
    val stønadType: StønadType,
    val tilkjenteYtelser: List<KonsistensavstemmingTilkjentYtelseDto>,
    val avstemmingstidspunkt: LocalDateTime? = null,
)

data class KonsistensavstemmingTilkjentYtelseDto(
    val behandlingId: UUID,
    val eksternBehandlingId: Long,
    val eksternFagsakId: Long,
    val personIdent: String,
    val andelerTilkjentYtelse: List<UtbetalingDto>,
)
