package no.nav.utsjekk.iverksetting.domene.transformer

import no.nav.utsjekk.iverksetting.api.TokenContext
import no.nav.utsjekk.iverksetting.domene.Behandlingsdetaljer
import no.nav.utsjekk.iverksetting.domene.Fagsakdetaljer
import no.nav.utsjekk.iverksetting.domene.Iverksetting
import no.nav.utsjekk.iverksetting.domene.KonsumentConfig
import no.nav.utsjekk.iverksetting.domene.Søker
import no.nav.utsjekk.iverksetting.domene.Vedtaksdetaljer
import no.nav.utsjekk.kontrakter.iverksett.IverksettDto
import no.nav.utsjekk.kontrakter.iverksett.VedtaksdetaljerDto
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
