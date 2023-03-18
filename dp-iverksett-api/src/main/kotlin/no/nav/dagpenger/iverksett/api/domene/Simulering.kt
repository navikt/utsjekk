package no.nav.dagpenger.iverksett.api.domene

import java.util.UUID

data class Simulering(val nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData, val forrigeBehandlingId: UUID?)

fun IverksettDagpenger.tilSimulering() = Simulering(
    nyTilkjentYtelseMedMetaData = TilkjentYtelseMedMetaData(
        tilkjentYtelse = this.vedtak.tilkjentYtelse!!,
        saksbehandlerId = this.vedtak.saksbehandlerId,
        eksternBehandlingId = this.behandling.eksternId,
        stønadstype = this.fagsak.stønadstype,
        eksternFagsakId = this.fagsak.eksternId,
        personIdent = this.søker.personIdent,
        behandlingId = this.behandling.behandlingId,
        vedtaksdato = this.vedtak.vedtakstidspunkt.toLocalDate(),
    ),
    forrigeBehandlingId = this.behandling.forrigeBehandlingId,
)
