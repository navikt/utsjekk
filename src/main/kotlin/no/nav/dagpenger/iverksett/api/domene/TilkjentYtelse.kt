package no.nav.dagpenger.iverksett.api.domene

import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.iverksett.TilkjentYtelseStatus
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsoppdrag
import java.time.LocalDate
import java.util.UUID
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.AndelData
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.StønadTypeOgFerietillegg

data class TilkjentYtelse(
    val id: UUID = UUID.randomUUID(),
    val utbetalingsoppdrag: Utbetalingsoppdrag? = null,
    val status: TilkjentYtelseStatus = TilkjentYtelseStatus.IKKE_KLAR,
    val andelerTilkjentYtelse: List<AndelTilkjentYtelse>,
    val sisteAndelIKjede: AndelTilkjentYtelse? = null,
    val sisteAndelPerKjede: Map<StønadTypeOgFerietillegg, AndelTilkjentYtelse> = sisteAndelIKjede?.let {
        mapOf(
            StønadTypeOgFerietillegg(
                it.stønadstype,
                it.ferietillegg
            ) to it
        )
    } ?: emptyMap(),
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

fun IverksettResultat?.erKonsistentMed(forrigeIverksetting: IverksettDagpenger?): Boolean {
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

fun TilkjentYtelse?.lagAndelData(): List<AndelData> =
    this?.andelerTilkjentYtelse?.map {
        it.tilAndelData()
    } ?: emptyList()
