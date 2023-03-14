package no.nav.dagpenger.iverksett.iverksetting.domene

import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import java.time.LocalDate
import java.util.UUID

data class TilkjentYtelseMedMetaData(
    val tilkjentYtelse: TilkjentYtelse,
    val saksbehandlerId: String,
    val eksternBehandlingId: Long,
    val stønadstype: StønadType,
    val eksternFagsakId: Long,
    val personIdent: String,
    val behandlingId: UUID,
    val vedtaksdato: LocalDate,
)
