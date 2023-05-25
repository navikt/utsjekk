package no.nav.dagpenger.iverksett.kontrakter.iverksett

import no.nav.dagpenger.iverksett.kontrakter.iverksett.TilkjentYtelse
import no.nav.dagpenger.kontrakter.utbetaling.StønadType
import java.time.LocalDate
import java.util.UUID

data class TilkjentYtelseMedMetaData(
    val tilkjentYtelse: TilkjentYtelse,
    val saksbehandlerId: String,
    val stønadstype: StønadType,
    val sakId: UUID,
    val personIdent: String,
    val behandlingId: UUID,
    val vedtaksdato: LocalDate,
)
