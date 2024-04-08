package no.nav.utsjekk.utbetaling.domene.transformer

import no.nav.utsjekk.kontrakter.iverksett.IverksettDto
import no.nav.utsjekk.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.utsjekk.utbetaling.api.TokenContext
import no.nav.utsjekk.utbetaling.domene.Behandlingsdetaljer
import no.nav.utsjekk.utbetaling.domene.Fagsakdetaljer
import no.nav.utsjekk.utbetaling.domene.Iverksetting
import no.nav.utsjekk.utbetaling.domene.KonsumentConfig
import no.nav.utsjekk.utbetaling.domene.Søker
import no.nav.utsjekk.utbetaling.domene.Vedtaksdetaljer
import org.springframework.stereotype.Component

@Component
class IverksettDtoMapper(val konsumentConfig: KonsumentConfig) {
    fun tilDomene(dto: IverksettDto) =
        Iverksetting(
            fagsak = dto.tilFagsak(),
            søker = dto.personident.verdi.tilSøker(),
            behandling = dto.tilBehandling(),
            vedtak = dto.vedtak.toDomain(),
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
