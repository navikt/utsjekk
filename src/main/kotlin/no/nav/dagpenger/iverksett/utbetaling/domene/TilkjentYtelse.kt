package no.nav.dagpenger.iverksett.utbetaling.domene

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import no.nav.dagpenger.iverksett.utbetaling.domene.transformer.RandomOSURId
import no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.domene.AndelData
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsoppdrag

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
