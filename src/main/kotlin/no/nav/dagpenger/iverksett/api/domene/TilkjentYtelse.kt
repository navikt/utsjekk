package no.nav.dagpenger.iverksett.api.domene

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.util.UUID
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.AndelData
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.StønadTypeOgFerietillegg
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.StønadTypeOgFerietilleggKeyDeserializer
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.StønadTypeOgFerietilleggKeySerializer
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsoppdrag

data class TilkjentYtelse(
    val id: UUID = UUID.randomUUID(),
    val utbetalingsoppdrag: Utbetalingsoppdrag? = null,
    val andelerTilkjentYtelse: List<AndelTilkjentYtelse>,
    val sisteAndelIKjede: AndelTilkjentYtelse? = null,
    @JsonSerialize(keyUsing = StønadTypeOgFerietilleggKeySerializer::class)
    @JsonDeserialize(keyUsing = StønadTypeOgFerietilleggKeyDeserializer::class)
    val sisteAndelPerKjede: Map<StønadTypeOgFerietillegg, AndelTilkjentYtelse> = sisteAndelIKjede?.let {
        mapOf(
            StønadTypeOgFerietillegg(
                it.stønadstype,
                it.ferietillegg,
            ) to it,
        )
    } ?: emptyMap(),
)

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
                id = it.id,
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
