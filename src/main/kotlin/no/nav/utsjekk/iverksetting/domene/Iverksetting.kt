package no.nav.utsjekk.iverksetting.domene

import no.nav.utsjekk.kontrakter.felles.BrukersNavKontor
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import java.time.LocalDateTime

data class Iverksetting(
    val fagsak: Fagsakdetaljer,
    val behandling: Behandlingsdetaljer,
    val søker: Søker,
    val vedtak: Vedtaksdetaljer,
) {
    override fun toString() =
        "fagsystem ${fagsak.fagsystem}, sak $sakId, behandling $behandlingId, iverksettingId ${behandling.iverksettingId}"
}

data class Fagsakdetaljer(
    val fagsakId: String,
    val fagsystem: Fagsystem,
)

data class Søker(
    val personident: String,
)

data class Vedtaksdetaljer(
    val vedtakstidspunkt: LocalDateTime,
    val saksbehandlerId: String,
    val beslutterId: String,
    val brukersNavKontor: BrukersNavKontor? = null,
    val tilkjentYtelse: TilkjentYtelse,
)

data class Behandlingsdetaljer(
    val forrigeBehandlingId: String? = null,
    val forrigeIverksettingId: String? = null,
    val behandlingId: String,
    val iverksettingId: String? = null,
)

val Iverksetting.sakId get() = this.fagsak.fagsakId
val Iverksetting.personident get() = this.søker.personident

val Iverksetting.behandlingId get() = this.behandling.behandlingId
