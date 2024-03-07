package no.nav.dagpenger.iverksett.utbetaling.domene

import no.nav.dagpenger.kontrakter.felles.BrukersNavKontor
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.GeneriskId
import java.time.LocalDateTime

data class Iverksetting(
    val fagsak: Fagsakdetaljer,
    val behandling: Behandlingsdetaljer,
    val søker: Søker,
    val vedtak: Vedtaksdetaljer,
    val forrigeIverksettingBehandlingId: GeneriskId? = null,
) {
    override fun toString() =
        "fagsystem ${fagsak.fagsystem}, sak $sakId, behandling $behandlingId, iverksettingId ${behandling.iverksettingId}"
}

data class Fagsakdetaljer(
    val fagsakId: GeneriskId,
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
    val forrigeBehandlingId: GeneriskId? = null,
    val forrigeIverksettingId: String? = null,
    val behandlingId: GeneriskId,
    val iverksettingId: String? = null,
)

val Iverksetting.sakId get() = this.fagsak.fagsakId
val Iverksetting.personident get() = this.søker.personident

val Iverksetting.behandlingId get() = this.behandling.behandlingId
