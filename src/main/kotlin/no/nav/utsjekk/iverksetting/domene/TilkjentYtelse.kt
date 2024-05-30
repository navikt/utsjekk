package no.nav.utsjekk.iverksetting.domene

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import no.nav.utsjekk.iverksetting.domene.transformer.RandomOSURId
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.domene.AndelData
import no.nav.utsjekk.kontrakter.oppdrag.Utbetalingsoppdrag

data class TilkjentYtelse(
    val id: String = RandomOSURId.generate(),
    val utbetalingsoppdrag: Utbetalingsoppdrag? = null,
    val andelerTilkjentYtelse: List<AndelTilkjentYtelse>,
    val sisteAndelIKjede: AndelTilkjentYtelse? = null,
    @JsonSerialize(keyUsing = StønadsdataKeySerializer::class)
    @JsonDeserialize(keyUsing = StønadsdataKeyDeserializer::class)
    val sisteAndelPerKjede: Map<Stønadsdata, AndelTilkjentYtelse> =
        sisteAndelIKjede?.let {
            mapOf(it.stønadsdata to it)
        } ?: emptyMap(),
)

fun TilkjentYtelse?.lagAndelData(): List<AndelData> =
    this?.andelerTilkjentYtelse?.map {
        it.tilAndelData()
    } ?: emptyList()
