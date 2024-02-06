package no.nav.dagpenger.iverksett.utbetaling.domene.transformer

import no.nav.dagpenger.iverksett.utbetaling.api.TokenContext
import no.nav.dagpenger.iverksett.utbetaling.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.utbetaling.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.KonsumentConfig
import no.nav.dagpenger.iverksett.utbetaling.domene.Søker
import no.nav.dagpenger.iverksett.utbetaling.domene.Vedtaksdetaljer
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import org.springframework.stereotype.Component

@Component
class IverksettDtoMapper(val konsumentConfig: KonsumentConfig) {
    fun tilDomene(dto: IverksettDto) =
        Iverksetting(
            fagsak = dto.tilFagsak(),
            søker = dto.personident.verdi.tilSøker(),
            behandling = dto.tilBehandling(),
            vedtak = dto.vedtak.toDomain(),
            forrigeIverksettingBehandlingId = dto.forrigeIverksetting?.behandlingId,
        )

    private fun VedtaksdetaljerDto.toDomain() =
        Vedtaksdetaljer(
            vedtakstidspunkt = this.vedtakstidspunkt,
            saksbehandlerId = this.saksbehandlerId,
            beslutterId = this.beslutterId,
            brukersNavKontor = this.brukersNavKontor,
            tilkjentYtelse = this.utbetalinger.tilTilkjentYtelse(),
        )

    private fun IverksettDto.tilFagsak() =
        Fagsakdetaljer(
            fagsakId = this.sakId,
            fagsystem = konsumentConfig.finnFagsystem(TokenContext.hentKlientnavn()),
        )

    private fun String.tilSøker(): Søker = Søker(personident = this)

    private fun IverksettDto.tilBehandling(): Behandlingsdetaljer =
        Behandlingsdetaljer(
            behandlingId = this.behandlingId,
            forrigeBehandlingId = this.forrigeIverksetting?.behandlingId,
        )
}
