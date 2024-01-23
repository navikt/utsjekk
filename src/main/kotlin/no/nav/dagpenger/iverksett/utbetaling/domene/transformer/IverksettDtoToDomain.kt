package no.nav.dagpenger.iverksett.utbetaling.domene.transformer

import no.nav.dagpenger.iverksett.utbetaling.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.utbetaling.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.Søker
import no.nav.dagpenger.iverksett.utbetaling.domene.Vedtaksdetaljer
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto


fun VedtaksdetaljerDto.toDomain(): Vedtaksdetaljer {
    return Vedtaksdetaljer(
        vedtakstidspunkt = this.vedtakstidspunkt,
        saksbehandlerId = this.saksbehandlerId,
        beslutterId = this.beslutterId,
        brukersNavKontor = this.brukersNavKontor,
        tilkjentYtelse = this.utbetalinger.tilTilkjentYtelse(),
    )
}

fun IverksettDto.toDomain(): Iverksetting {
    return Iverksetting(
        fagsak = this.tilFagsak(),
        søker = this.personident.verdi.tilSøker(),
        behandling = this.tilBehandling(),
        vedtak = this.vedtak.toDomain(),
        forrigeIverksettingBehandlingId = this.forrigeIverksetting?.behandlingId,
    )
}

fun IverksettDto.tilFagsak(): Fagsakdetaljer {
    return Fagsakdetaljer(
        fagsakId = this.sakId,
    )
}

fun String.tilSøker(): Søker = Søker(personident = this)

fun IverksettDto.tilBehandling(): Behandlingsdetaljer = Behandlingsdetaljer(
    behandlingId = this.behandlingId,
    forrigeBehandlingId = this.forrigeIverksetting?.behandlingId,
)
