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
                StønadTypeDagpenger.DAGPENGER_ARBEIDSSOKER_ORDINAER -> when (ferietillegg) {
                    Ferietillegg.ORDINAER -> "DPORASFE"
                    Ferietillegg.AVDOD -> "DPORASFE-IOP"
                    null -> "DPORAS"
                }

                StønadTypeDagpenger.DAGPENGER_PERMITTERING_ORDINAER -> when (ferietillegg) {
                    Ferietillegg.ORDINAER -> "DPPEASFE1"
                    Ferietillegg.AVDOD -> "DPPEASFE1-IOP"
                    null -> "DPPEAS"
                }

                StønadTypeDagpenger.DAGPENGER_PERMITTERING_FISKEINDUSTRI -> when (ferietillegg) {
                    Ferietillegg.ORDINAER -> "DPPEFIFE1"
                    Ferietillegg.AVDOD -> "DPPEFIFE1-IOP"
                    null -> "DPPEFI"
                }

                StønadTypeDagpenger.DAGPENGER_EOS -> when (ferietillegg) {
                    Ferietillegg.ORDINAER -> "DPFEASISP"
                    Ferietillegg.AVDOD -> throw IllegalArgumentException("Eksport-gruppen har ingen egen kode for ferietillegg til avdød")
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
                    StønadTypeTiltakspenger.ENKELTPLASS_VGS_OG_HOYERE_YRKESFAG -> "TPBTEPVGSHOY"
                    StønadTypeTiltakspenger.FORSOK_OPPLAERING_LENGRE_VARIGHET -> "TPBTFLV"
                    StønadTypeTiltakspenger.GRUPPE_AMO -> "TPBTGRAMO"
                    StønadTypeTiltakspenger.GRUPPE_VGS_OG_HOYERE_YRKESFAG -> "TPBTGRVGSHOY"
                    StønadTypeTiltakspenger.HOYERE_UTDANNING -> "TPBTHOYUTD"
                    StønadTypeTiltakspenger.INDIVIDUELL_JOBBSTOTTE -> "TPBTIPS"
                    StønadTypeTiltakspenger.INDIVIDUELL_KARRIERESTOTTE_UNG -> "TPBTIPSUNG"
                    StønadTypeTiltakspenger.JOBBKLUBB -> "TPBTJK2009"
                    StønadTypeTiltakspenger.OPPFOLGING -> "TPBTOPPFAGR"
                    StønadTypeTiltakspenger.UTVIDET_OPPFOLGING_I_NAV -> "TPBTUAOPPFL"
                    StønadTypeTiltakspenger.UTVIDET_OPPFOLGING_I_OPPLAERING -> "TPBTUOPPFOPPL"
                }
            } else {
                when (this.stønadstype) {
                    StønadTypeTiltakspenger.ARBEIDSFORBEREDENDE_TRENING -> "TPTPAFT"
                    StønadTypeTiltakspenger.ARBEIDSRETTET_REHABILITERING -> "TPTPARREHABAGDAG"
                    StønadTypeTiltakspenger.ARBEIDSTRENING -> "TPTPATT"
                    StønadTypeTiltakspenger.AVKLARING -> "TPTPAAG"
                    StønadTypeTiltakspenger.DIGITAL_JOBBKLUBB -> "TPTPDJB"
                    StønadTypeTiltakspenger.ENKELTPLASS_AMO -> "TPTPEPAMO"
                    StønadTypeTiltakspenger.ENKELTPLASS_VGS_OG_HOYERE_YRKESFAG -> "TPTPEPVGSHOU"
                    StønadTypeTiltakspenger.FORSOK_OPPLAERING_LENGRE_VARIGHET -> "TPTPFLV"
                    StønadTypeTiltakspenger.GRUPPE_AMO -> "TPTPGRAMO"
                    StønadTypeTiltakspenger.GRUPPE_VGS_OG_HOYERE_YRKESFAG -> "TPTPGRVGSHOY"
                    StønadTypeTiltakspenger.HOYERE_UTDANNING -> "TPTPHOYUTD"
                    StønadTypeTiltakspenger.INDIVIDUELL_JOBBSTOTTE -> "TPTPIPS"
                    StønadTypeTiltakspenger.INDIVIDUELL_KARRIERESTOTTE_UNG -> "TPTPIPSUNG"
                    StønadTypeTiltakspenger.JOBBKLUBB -> "TPTPJK2009"
                    StønadTypeTiltakspenger.OPPFOLGING -> "TPTPOPPFAG"
                    StønadTypeTiltakspenger.UTVIDET_OPPFOLGING_I_NAV -> "TPTPUAOPPF"
                    StønadTypeTiltakspenger.UTVIDET_OPPFOLGING_I_OPPLAERING -> "TPTPUOPPFOPPL"
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
