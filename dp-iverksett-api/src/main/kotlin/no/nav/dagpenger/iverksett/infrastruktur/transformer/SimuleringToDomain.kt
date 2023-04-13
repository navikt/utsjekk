package no.nav.dagpenger.iverksett.infrastruktur.transformer

import no.nav.dagpenger.iverksett.api.domene.Simulering
import no.nav.dagpenger.iverksett.kontrakter.iverksett.SimuleringDto

fun SimuleringDto.toDomain(): Simulering {
    val andeler = this.utbetalinger.map { it.toDomain() }
    return Simulering(
        andelerTilkjentYtelse = andeler,
        tilkjentYtelse = this.utbetalinger.tilTilkjentYtelse() ?: tomTilkjentYtelse(),
        saksbehandlerId = this.saksbehandlerId,
        eksternBehandlingId = this.eksternBehandlingId,
        stønadstype = this.stønadstype,
        eksternFagsakId = this.eksternFagsakId,
        personIdent = this.personIdent,
        behandlingId = this.behandlingId,
        vedtaksdato = this.vedtaksdato,
        forrigeBehandlingId = this.forrigeBehandlingId,
    )
}
