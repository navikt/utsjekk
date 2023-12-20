package no.nav.dagpenger.iverksett.api.domene

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.util.UUID
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.AndelData
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.StønadTypeOgFerietilleggKeyDeserializer
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.StønadTypeOgFerietilleggKeySerializer
import no.nav.dagpenger.kontrakter.iverksett.Stønadsdata
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsoppdrag

data class TilkjentYtelse(
    val id: UUID = UUID.randomUUID(),
    val utbetalingsoppdrag: Utbetalingsoppdrag? = null,
    val andelerTilkjentYtelse: List<AndelTilkjentYtelse>,
    val sisteAndelIKjede: AndelTilkjentYtelse? = null,
    @JsonSerialize(keyUsing = StønadTypeOgFerietilleggKeySerializer::class)
    @JsonDeserialize(keyUsing = StønadTypeOgFerietilleggKeyDeserializer::class)
    val sisteAndelPerKjede: Map<Stønadsdata, AndelTilkjentYtelse> = sisteAndelIKjede?.let {
        mapOf(it.stønadsdata to it)
    } ?: emptyMap(),
)

fun TilkjentYtelse?.lagAndelData(): List<AndelData> =
    this?.andelerTilkjentYtelse?.map {
        it.tilAndelData()
    } ?: emptyList()
