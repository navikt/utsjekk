package no.nav.utsjekk.simulering.api

import no.nav.utsjekk.iverksetting.domene.transformer.tilTilkjentYtelse
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.domene.Behandlingsinformasjon
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.simulering.domene.ForrigeIverksetting
import no.nav.utsjekk.simulering.domene.Simulering

fun SimuleringRequestTilleggsstønaderDto.tilInterntFormat(): Simulering {
    return Simulering(
        behandlingsinformasjon =
            Behandlingsinformasjon(
                saksbehandlerId = this.saksbehandler,
                beslutterId = this.saksbehandler,
                fagsakId = this.sakId,
                fagsystem = Fagsystem.TILLEGGSSTØNADER,
                behandlingId = this.behandlingId,
                personident = this.personident.verdi,
                vedtaksdato = this.vedtakstidspunkt.toLocalDate(),
                brukersNavKontor = this.utbetalinger.firstOrNull()?.brukersNavKontor,
                iverksettingId = null,
            ),
        nyTilkjentYtelse = this.utbetalinger.tilTilkjentYtelse(),
        forrigeIverksetting =
            this.forrigeIverksetting?.let {
                ForrigeIverksetting(
                    behandlingId = it.behandlingId,
                    iverksettingId = it.iverksettingId,
                )
            },
    )
}

fun SimuleringRequestDto.tilInterntFormat(fagsystem: Fagsystem): Simulering {
    return Simulering(
        behandlingsinformasjon =
            Behandlingsinformasjon(
                saksbehandlerId = this.saksbehandler,
                beslutterId = this.saksbehandler,
                fagsakId = this.sakId,
                fagsystem = fagsystem,
                behandlingId = this.behandlingId,
                personident = this.personident.verdi,
                vedtaksdato = this.vedtakstidspunkt.toLocalDate(),
                brukersNavKontor = null,
                iverksettingId = null,
            ),
        nyTilkjentYtelse = this.utbetalinger.tilTilkjentYtelse(),
        forrigeIverksetting =
            this.forrigeIverksetting?.let {
                ForrigeIverksetting(
                    behandlingId = it.behandlingId,
                    iverksettingId = null,
                )
            },
    )
}
