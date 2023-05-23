package no.nav.dagpenger.iverksett.api.domene

import no.nav.dagpenger.kontrakter.utbetaling.StønadType
import java.time.LocalDate
import java.util.UUID

data class Simulering(
    val andelerTilkjentYtelse: List<AndelTilkjentYtelse>,
    val tilkjentYtelse: TilkjentYtelse,
    val saksbehandlerId: String,
    val stønadstype: StønadType,
    val sakId: UUID,
    val personIdent: String,
    val behandlingId: UUID,
    val vedtaksdato: LocalDate,
    val forrigeBehandlingId: UUID?,
)

fun IverksettDagpenger.tilSimulering() = Simulering(
    andelerTilkjentYtelse = this.vedtak.tilkjentYtelse!!.andelerTilkjentYtelse,
    tilkjentYtelse = this.vedtak.tilkjentYtelse!!,
    saksbehandlerId = this.vedtak.saksbehandlerId,
    stønadstype = this.fagsak.stønadstype,
    sakId = this.fagsak.fagsakId,
    personIdent = this.søker.personIdent,
    behandlingId = this.behandling.behandlingId,
    vedtaksdato = this.vedtak.vedtakstidspunkt.toLocalDate(),
    forrigeBehandlingId = this.behandling.forrigeBehandlingId,
)

fun Simulering.tilTilkjentYtelseMedMetadata(): TilkjentYtelseMedMetaData =
    TilkjentYtelseMedMetaData(
        tilkjentYtelse = this.andelerTilkjentYtelse.tilTilkjentYtelse(),
        saksbehandlerId = this.saksbehandlerId,
        stønadstype = this.stønadstype,
        sakId = this.sakId,
        behandlingId = this.behandlingId,
        personIdent = this.personIdent,
        vedtaksdato = this.vedtaksdato,

    )

fun Iterable<AndelTilkjentYtelse>.tilTilkjentYtelse() = TilkjentYtelse(
    andelerTilkjentYtelse = this.toList(),
    startdato = this.minOfOrNull { it.periode.fom } ?: LocalDate.now(),
)
