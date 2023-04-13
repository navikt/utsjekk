package no.nav.dagpenger.iverksett.kontrakter.iverksett

import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import java.time.LocalDate
import java.util.UUID

data class UtbetalingerMedMetadataDto(
    val utbetalinger: List<UtbetalingDto>,
    val saksbehandlerId: String,
    val eksternBehandlingId: Long,
    val stønadstype: StønadType,
    val eksternFagsakId: Long,
    val personIdent: String,
    val behandlingId: UUID,
    val vedtaksdato: LocalDate,
)