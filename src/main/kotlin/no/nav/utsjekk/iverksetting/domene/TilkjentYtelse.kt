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
    @JsonSerialize(keyUsing = KjedenøkkelKeySerializer::class)
    @JsonDeserialize(keyUsing = KjedenøkkelKeyDeserializer::class)
    val sisteAndelPerKjede: Map<Kjedenøkkel, AndelTilkjentYtelse> =
        sisteAndelIKjede?.let {
            mapOf(it.stønadsdata.tilKjedenøkkel() to it)
        } ?: emptyMap(),
)

fun TilkjentYtelse?.lagAndelData(): List<AndelData> =
    this?.andelerTilkjentYtelse?.map {
        it.tilAndelData()
    } ?: emptyList()
