package no.nav.dagpenger.iverksett.infrastruktur.transformer

import java.lang.IllegalStateException
import no.nav.dagpenger.iverksett.api.domene.Simulering
import no.nav.dagpenger.kontrakter.iverksett.SimuleringDto

fun SimuleringDto.toDomain(): Simulering {
    val andeler = this.utbetalinger.map { it.toDomain() }
    return Simulering(
        andelerTilkjentYtelse = andeler,
        tilkjentYtelse = this.utbetalinger.tilTilkjentYtelse() ?: tomTilkjentYtelse(),
        saksbehandlerId = this.saksbehandlerId,
        stønadstype = this.stønadstype,
        sakId = this.tilSakIdentifikator(),
        personIdent = this.personIdent,
        behandlingId = this.behandlingId,
        vedtaksdato = this.vedtaksdato,
        forrigeBehandlingId = this.forrigeBehandlingId,
    )
}

fun SimuleringDto.tilSakIdentifikator(): String {
    return this.sakId?.toString() ?: this.saksreferanse
    ?: throw IllegalStateException("SakId eller Saksreferanse må være satt på SimuleringDto")
}
