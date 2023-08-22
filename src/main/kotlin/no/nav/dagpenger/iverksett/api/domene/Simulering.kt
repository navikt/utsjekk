package no.nav.dagpenger.iverksett.api.domene

import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.Behandlingsinformasjon
import no.nav.dagpenger.kontrakter.felles.SakIdentifikator
import no.nav.dagpenger.kontrakter.felles.StønadType
import java.time.LocalDate
import java.util.UUID

data class Simulering(
    val andelerTilkjentYtelse: List<AndelTilkjentYtelse>,
    val tilkjentYtelse: TilkjentYtelse,
    val saksbehandlerId: String,
    val stønadstype: StønadType,
    val sakId: UUID? = null,
    val saksreferanse: String? = null,
    val personIdent: String,
    val behandlingId: UUID,
    val vedtaksdato: LocalDate,
    val forrigeBehandlingId: UUID?,
) {
    init {
        SakIdentifikator.valider(sakId, saksreferanse)
    }
}

fun IverksettDagpenger.tilSimulering() = Simulering(
    andelerTilkjentYtelse = this.vedtak.tilkjentYtelse!!.andelerTilkjentYtelse,
    tilkjentYtelse = this.vedtak.tilkjentYtelse!!,
    saksbehandlerId = this.vedtak.saksbehandlerId,
    stønadstype = this.fagsak.stønadstype,
    sakId = this.fagsak.fagsakId,
    saksreferanse = this.fagsak.saksreferanse,
    personIdent = this.søker.personIdent,
    behandlingId = this.behandling.behandlingId,
    vedtaksdato = this.vedtak.vedtakstidspunkt.toLocalDate(),
    forrigeBehandlingId = this.behandling.forrigeBehandlingId,
)

fun Simulering.tilBehandlingsinformasjon(): Behandlingsinformasjon =
    Behandlingsinformasjon(
        saksbehandlerId = this.saksbehandlerId,
        fagsakId = this.sakId,
        saksreferanse = this.saksreferanse,
        behandlingId = this.behandlingId.toString(),
        personIdent = this.personIdent,
        vedtaksdato = this.vedtaksdato,
        opphørFra = null,
    )
