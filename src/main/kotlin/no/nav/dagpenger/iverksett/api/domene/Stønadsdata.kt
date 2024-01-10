package no.nav.dagpenger.iverksett.api.domene

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.dagpenger.kontrakter.iverksett.*


sealed class Stønadsdata(open val stønadstype: StønadType) {

    fun tilKlassifisering(): String =
            when (this) {
                is StønadsdataDagpenger -> this.tilKlassifiseringDagpenger()
                is StønadsdataTiltakspenger -> this.tilKlassifiseringTiltakspenger()
            }

    companion object {
        @JsonCreator
        @JvmStatic
        fun deserialize(json: JsonNode): Stønadsdata {
            val stønadstype = json.findValue("stønadstype").asText()
            return Result.runCatching { StønadTypeDagpenger.valueOf(stønadstype) }.fold(
                    onSuccess = {
                        val ferietillegg = json.findValue("ferietillegg")?.asText()
                        if (ferietillegg != null && ferietillegg != "null") {
                            StønadsdataDagpenger(it, Ferietillegg.valueOf(ferietillegg))
                        } else {
                            StønadsdataDagpenger(it)
                        }
                    },
                    onFailure = {
                        val stønadstypeTiltakspenger = StønadTypeTiltakspenger.valueOf(stønadstype)
                        val barnetillegg = json.findValue("barnetillegg")?.asBoolean()
                        StønadsdataTiltakspenger(stønadstypeTiltakspenger, barnetillegg ?: false)
                    }
            )
        }
    }
}

data class StønadsdataDagpenger(override val stønadstype: StønadTypeDagpenger, val ferietillegg: Ferietillegg? = null) : Stønadsdata(stønadstype) {
    fun tilKlassifiseringDagpenger(): String =
            when (this.stønadstype) {
                StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR -> when (ferietillegg) {
                    Ferietillegg.ORDINÆR -> "DPORASFE"
                    Ferietillegg.AVDØD -> "DPORASFE-IOP"
                    null -> "DPORAS"
                }

                StønadTypeDagpenger.DAGPENGER_PERMITTERING_ORDINÆR -> when (ferietillegg) {
                    Ferietillegg.ORDINÆR -> "DPPEASFE1"
                    Ferietillegg.AVDØD -> "DPPEASFE1-IOP"
                    null -> "DPPEAS"
                }

                StønadTypeDagpenger.DAGPENGER_PERMITTERING_FISKEINDUSTRI -> when (ferietillegg) {
                    Ferietillegg.ORDINÆR -> "DPPEFIFE1"
                    Ferietillegg.AVDØD -> "DPPEFIFE1-IOP"
                    null -> "DPPEFI"
                }

                StønadTypeDagpenger.DAGPENGER_EØS -> when (ferietillegg) {
                    Ferietillegg.ORDINÆR -> "DPFEASISP"
                    Ferietillegg.AVDØD -> throw IllegalArgumentException("Eksport-gruppen har ingen egen kode for ferietillegg til avdød")
                    null -> "DPDPASISP1"
                }
            }
}

data class StønadsdataTiltakspenger(
        override val stønadstype: StønadTypeTiltakspenger,
        val barnetillegg: Boolean = false
) : Stønadsdata(stønadstype) {
    fun tilKlassifiseringTiltakspenger(): String =
            if (barnetillegg) {
                when (this.stønadstype) {
                    StønadTypeTiltakspenger.ARBEIDSFORBEREDENDE_TRENING -> "TPBTAF"
                    StønadTypeTiltakspenger.ARBEIDSRETTET_REHABILITERING -> "TPBTARREHABAGDAG"
                    StønadTypeTiltakspenger.ARBEIDSTRENING -> "TPBTATTILT"
                    StønadTypeTiltakspenger.AVKLARING -> "TPBTAAGR"
                    StønadTypeTiltakspenger.DIGITAL_JOBBKLUBB -> "TPBTDJK"
                    StønadTypeTiltakspenger.ENKELTPLASS_AMO -> "TPBTEPAMO"
                    StønadTypeTiltakspenger.ENKELTPLASS_VGS_OG_HØYERE_YRKESFAG -> "TPBTEPVGSHOY"
                    StønadTypeTiltakspenger.FORSØK_OPPLÆRING_LENGRE_VARIGHET -> "TPBTFLV"
                    StønadTypeTiltakspenger.GRUPPE_AMO -> "TPBTGRAMO"
                    StønadTypeTiltakspenger.GRUPPE_VGS_OG_HØYERE_YRKESFAG -> "TPBTGRVGSHOY"
                    StønadTypeTiltakspenger.HØYERE_UTDANNING -> "TPBTHOYUTD"
                    StønadTypeTiltakspenger.INDIVIDUELL_JOBBSTØTTE -> "TPBTIPS"
                    StønadTypeTiltakspenger.INDIVIDUELL_KARRIERESTØTTE_UNG -> "TPBTIPSUNG"
                    StønadTypeTiltakspenger.JOBBKLUBB -> "TPBTJK2009"
                    StønadTypeTiltakspenger.OPPFØLGING -> "TPBTOPPFAGR"
                    StønadTypeTiltakspenger.UTVIDET_OPPFØLGING_I_NAV -> "TPBTUAOPPFL"
                    StønadTypeTiltakspenger.UTVIDET_OPPFØLGING_I_OPPLÆRING -> "TPBTUOPPFOPPL"
                }
            } else {
                when (this.stønadstype) {
                    StønadTypeTiltakspenger.ARBEIDSFORBEREDENDE_TRENING -> "TPTPAFT"
                    StønadTypeTiltakspenger.ARBEIDSRETTET_REHABILITERING -> "TPTPARREHABAGDAG"
                    StønadTypeTiltakspenger.ARBEIDSTRENING -> "TPTPATT"
                    StønadTypeTiltakspenger.AVKLARING -> "TPTPAAG"
                    StønadTypeTiltakspenger.DIGITAL_JOBBKLUBB -> "TPTPDJB"
                    StønadTypeTiltakspenger.ENKELTPLASS_AMO -> "TPTPEPAMO"
                    StønadTypeTiltakspenger.ENKELTPLASS_VGS_OG_HØYERE_YRKESFAG -> "TPTPEPVGSHOU"
                    StønadTypeTiltakspenger.FORSØK_OPPLÆRING_LENGRE_VARIGHET -> "TPTPFLV"
                    StønadTypeTiltakspenger.GRUPPE_AMO -> "TPTPGRAMO"
                    StønadTypeTiltakspenger.GRUPPE_VGS_OG_HØYERE_YRKESFAG -> "TPTPGRVGSHOY"
                    StønadTypeTiltakspenger.HØYERE_UTDANNING -> "TPTPHOYUTD"
                    StønadTypeTiltakspenger.INDIVIDUELL_JOBBSTØTTE -> "TPTPIPS"
                    StønadTypeTiltakspenger.INDIVIDUELL_KARRIERESTØTTE_UNG -> "TPTPIPSUNG"
                    StønadTypeTiltakspenger.JOBBKLUBB -> "TPTPJK2009"
                    StønadTypeTiltakspenger.OPPFØLGING -> "TPTPOPPFAG"
                    StønadTypeTiltakspenger.UTVIDET_OPPFØLGING_I_NAV -> "TPTPUAOPPF"
                    StønadTypeTiltakspenger.UTVIDET_OPPFØLGING_I_OPPLÆRING -> "TPTPUOPPFOPPL"
                }
            }
}


class StønadsdataKeySerializer : JsonSerializer<Stønadsdata>() {
    override fun serialize(value: Stønadsdata?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.let { jGen ->
            value?.let { stønadsdata ->
                jGen.writeFieldName(objectMapper.writeValueAsString(stønadsdata))
            } ?: jGen.writeNull()
        }
    }
}

class StønadsdataKeyDeserializer : KeyDeserializer() {
    override fun deserializeKey(key: String?, ctx: DeserializationContext?): Stønadsdata? {
        return key?.let { objectMapper.readValue(key, Stønadsdata::class.java) }
    }
}
