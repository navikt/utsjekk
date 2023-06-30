package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.SerializerProvider
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg
import java.io.IOException

data class StønadTypeOgFerietillegg(
    val stønadstype: StønadType,
    val ferietillegg: Ferietillegg? = null,
)

class StønadTypeOgFerietilleggKeySerializer : JsonSerializer<StønadTypeOgFerietillegg>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(value: StønadTypeOgFerietillegg?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.let { jGen ->
            value?.let { stønadtypeOgFerietillegg ->
                jGen.writeFieldName(objectMapper.writeValueAsString(stønadtypeOgFerietillegg))
            } ?: jGen.writeNull()
        }
    }
}

class StønadTypeOgFerietilleggKeyDeserializer : KeyDeserializer() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserializeKey(key: String?, ctxt: DeserializationContext?): StønadTypeOgFerietillegg? {
        return key?.let { objectMapper.readValue(key, StønadTypeOgFerietillegg::class.java) }
    }
}

fun StønadTypeOgFerietillegg.tilKlassifisering() = when (this.stønadstype) {
    StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER -> when (ferietillegg) {
        Ferietillegg.ORDINAER -> "DPORASFE"
        Ferietillegg.AVDOD -> "DPORASFE-IOP"
        null -> "DPORAS"
    }
    StønadType.DAGPENGER_PERMITTERING_ORDINAER -> when (ferietillegg) {
        Ferietillegg.ORDINAER -> "DPPEASFE1"
        Ferietillegg.AVDOD -> "DPPEASFE1-IOP"
        null -> "DPPEAS"
    }
    StønadType.DAGPENGER_PERMITTERING_FISKEINDUSTRI -> when (ferietillegg) {
        Ferietillegg.ORDINAER -> "DPPEFIFE1"
        Ferietillegg.AVDOD -> "DPPEFIFE1-IOP"
        null -> "DPPEFI"
    }
    StønadType.DAGPENGER_EOS -> when (ferietillegg) {
        Ferietillegg.ORDINAER -> "DPFEASISP"
        Ferietillegg.AVDOD -> throw IllegalArgumentException("Eksport-gruppen har ingen egen kode for ferietillegg til avdød")
        null -> "DPDPASISP1"
    }
    StønadType.TILTAKSPENGER -> "TPTPTILTAK"
}
