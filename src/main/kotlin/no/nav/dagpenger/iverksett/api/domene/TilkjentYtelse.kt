package no.nav.dagpenger.iverksett.api.domene

import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.iverksett.TilkjentYtelseStatus
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsoppdrag
import java.time.LocalDate
import java.util.UUID

data class TilkjentYtelse(
    val id: UUID = UUID.randomUUID(),
    val utbetalingsoppdrag: Utbetalingsoppdrag? = null,
    val status: TilkjentYtelseStatus = TilkjentYtelseStatus.IKKE_KLAR,
    val andelerTilkjentYtelse: List<AndelTilkjentYtelse>,
    val sisteAndelIKjede: AndelTilkjentYtelse? = null,
    val startdato: LocalDate,
) {

    fun toMedMetadata(
        saksbehandlerId: String,
        stønadType: StønadType,
        sakId: UUID,
        personIdent: String,
        behandlingId: UUID,
        vedtaksdato: LocalDate,
    ): TilkjentYtelseMedMetaData {
        return TilkjentYtelseMedMetaData(
            tilkjentYtelse = this,
            saksbehandlerId = saksbehandlerId,
            stønadstype = stønadType,
            sakId = sakId,
            personIdent = personIdent,
            behandlingId = behandlingId,
            vedtaksdato = vedtaksdato,
        )
    }
}

fun IverksettResultat?.stemmerMed(forrigeIverksetting: IverksettDagpenger?): Boolean {
    val andeler = this?.tilkjentYtelseForUtbetaling?.lagNormaliserteAndeler()
    val andreAndeler = forrigeIverksetting?.vedtak?.tilkjentYtelse?.lagNormaliserteAndeler()

    return andeler == andreAndeler
}

private fun TilkjentYtelse?.lagNormaliserteAndeler(): List<AndelTilkjentYtelse> {
    return this?.andelerTilkjentYtelse
        ?.sortedBy { it.periode.fom }
        ?.map {
            AndelTilkjentYtelse(
                beløp = it.beløp,
                periode = it.periode,
                stønadstype = it.stønadstype,
                ferietillegg = it.ferietillegg,
            )
        } ?: emptyList()
}
