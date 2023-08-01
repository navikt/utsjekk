package no.nav.dagpenger.iverksett.infrastruktur.util

import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.Behandlingsinformasjon
import no.nav.dagpenger.kontrakter.felles.StønadType
import java.time.LocalDate
import java.util.UUID

data class TilkjentYtelseMedMetaData(
    val tilkjentYtelse: TilkjentYtelse,
    val saksbehandlerId: String,
    val stønadstype: StønadType,
    val sakId: UUID,
    val personIdent: String,
    val behandlingId: UUID,
    val vedtaksdato: LocalDate,
)

fun TilkjentYtelseMedMetaData.tilBehandlingsinformasjon() = Behandlingsinformasjon(
    saksbehandlerId = this.saksbehandlerId,
    fagsakId = this.sakId.toString(),
    behandlingId = this.behandlingId.toString(),
    personIdent = this.personIdent.toString(),
    vedtaksdato = this.vedtaksdato,
    opphørFra = null,
)
