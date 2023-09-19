package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene

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
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger

data class StønadTypeOgFerietillegg(
    val stønadstype: StønadType,
    val ferietillegg: Ferietillegg? = null,
) {
    fun tilKlassifisering(): String =
        when (this.stønadstype) {
            is StønadTypeDagpenger -> tilKlassifiseringDagpenger()
            is StønadTypeTiltakspenger -> tilKlassifiseringTiltakspenger()
        }

    private fun tilKlassifiseringDagpenger(): String =
        when (this.stønadstype as StønadTypeDagpenger) {
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

    private fun tilKlassifiseringTiltakspenger(): String =
        when (this.stønadstype as StønadTypeTiltakspenger) {
            StønadTypeTiltakspenger.TILTAKSPENGER -> "TPTPTILTAK"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_HOYERE_UTDANNING -> "TPBTHOYUTD"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_JOBBKLUBB_2009 -> "TPBTJK2009"
            StønadTypeTiltakspenger.TILTAKSPENGER_UTDANNING -> "TPTPUTD"
            StønadTypeTiltakspenger.TILTAKSPENGER_AVKLARING_ANDRE_GRUPPER -> "TPTPAAG"
            StønadTypeTiltakspenger.TILTAKSPENGER_SEMESTERAVGIFT_OPPFOLGING_ANDRE_GRUPPER -> "TPSAOPPFAG"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_ENKELTPLASS_AMO -> "TPBTEPAMO"
            StønadTypeTiltakspenger.TILTAKSPENGER_HOYERE_UTDANNING -> "TPTPHOYUTD"
            StønadTypeTiltakspenger.TILTAKSPENGER_JOBBKLUBB_FRA_2009 -> "TPTPJK2009"
            StønadTypeTiltakspenger.TILTAKSPENGER_ARBEIDSMARKEDSTILTAK -> "TPTPTILTAK"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_GRUNNSKOLE_VGS_OG_HOYERE_YRKESFAG -> "TPBTGRVGSHOY"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_IPS -> "TPBTIPS"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_ARBEIDSTRENINGSTILTAK -> "TPBTATTILT"
            StønadTypeTiltakspenger.TILTAKSPENGER_ENKELTPLASS_VGS_ELLER_HOYERE_UTDANNING -> "TPTPEPVGSHOU"
            StønadTypeTiltakspenger.TILTAKSPENGER_EKSAMENSGEBYR_GRUNNSKOLE_VGS_OG_HOYERE_UTDANNING -> "TPEGGRVGSHOY"
            StønadTypeTiltakspenger.TILTAKSPENGER_GRUPPE_VGS_ELLER_HOYERE_UTDANNING -> "TPTPGRVGSHOY"
            StønadTypeTiltakspenger.TILTAKSPENGER_SKOLEPENGER_GRUPPE_AMO -> "TPSPGRAMO"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_GRUPPE_AMO -> "TPBTGRAMO"
            StønadTypeTiltakspenger.TILTAKSPENGER_INKLUDERINGSTILSKUDD -> "TPTPINKLTILSK"
            StønadTypeTiltakspenger.TILTAKSPENGER_SEMESTERAVGIFT_ENKELT_VGS_OG_HOYERE_UTDANNING -> "TPSAEPVGSHOY"
            StønadTypeTiltakspenger.TILTAKSPENGER_SKOLEPENGER_HOYERE_UTDANNING -> "TPSPHOYUTD"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_UTDANNING -> "TPBTUTD"
            StønadTypeTiltakspenger.TILTAKSPENGER_SKOLEPENGER_ARBEIDSTRNINGSTILTAK -> "TPSPATTILT"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_OPPFOLGING_ANDRE_GRUPPER -> "TPBTOPPFAGR"
            StønadTypeTiltakspenger.TILTAKSPENGER_EKSAMENSGEBYR_AMO -> "TPEGAMO"
            StønadTypeTiltakspenger.TILTAKSPENGER_FORSOK_LENGRE_VARIGHET -> "TPTPFLV"
            StønadTypeTiltakspenger.TILTAKSPENGER_UTVIDET_AVKLARING_OG_OPPFOLGING -> "TPTPUAOPPF"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_ARB_R_REHAB_ANDRE_GR_DAG -> "TPBTARREHABAGDAG"
            StønadTypeTiltakspenger.TILTAKSPENGER_MENTOR -> "TPTPM"
            StønadTypeTiltakspenger.TILTAKSPENGER_TILSYNSTILLEGG_AMO -> "TPTTAMO"
            StønadTypeTiltakspenger.TILTAKSPENGER_SKOLEPENGER_GRUPPE_VGS_OG_HOYERE -> "TPSPGRVGSHOY"
            StønadTypeTiltakspenger.TILTAKSPENGER_SKOLEPENGER_ENKELTPLASS_VGS_OG_HOYERE -> "TPSPEPVGSHOY"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_AVKLARING_ANDRE_GRUPPER -> "TPBTAAGR"
            StønadTypeTiltakspenger.TILTAKSPENGER_INDIVIDUELL_JOBBSTOTTE -> "TPTPIPS"
            StønadTypeTiltakspenger.TILTAKSPENGER_EKSAMENSGEBYR_ENKELTPLASS_AMO -> "TPEGEPAMO"
            StønadTypeTiltakspenger.TILTAKSPENGER_SEMESTERAVGIFT_ENKELTPLASS_AMO -> "TPSAEPAMO"
            StønadTypeTiltakspenger.TILTAKSPENGER_DIGITAL_JOBBKLUBB -> "TPTPDJB"
            StønadTypeTiltakspenger.TILTAKSPENGER_EKSAMENSGEBYR_FORSOK_FAG_YRKESOPPLAERING -> "TPEGFFYO"
            StønadTypeTiltakspenger.TILTAKSPENGER_ARBEIDSTRENINGSTILTAK -> "TPTPATT"
            StønadTypeTiltakspenger.TILTAKSPENGER_RES_BASERT_FINANS_FORM_BISTAND -> "TPTPRBFINFORM"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_ARBEIDSFORBEREDENDE -> "TPBTAF"
            StønadTypeTiltakspenger.TILTAKSPENGER_EKSAMESNSGEBYR_GR_AMO -> "TPEGGRAMO"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_UTV_OPPF_OPPLAERING -> "TPBTUOPPFOPPL"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_IPS_UNG -> "TPBTIPSUNG"
            StønadTypeTiltakspenger.TILTAKSPENGER_EKSAMENSGEBYR_HOYERE_UTDANNING -> "TPEGHOYUTD"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILSYN_ENKELPLASS_VGS_OG_HOYERE_YRKESFAG -> "TPBTEPVGSHOY"
            StønadTypeTiltakspenger.TILTAKSPENGER_GRUPPE_AMO -> "TPTPGRAMO"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_AMO -> "TPBTAMO"
            StønadTypeTiltakspenger.TILTAKSPENGER_SKOLEPENGER_ARBEIDSMARKEDSTILTAK -> "TPSPAMTILT"
            StønadTypeTiltakspenger.TILTAKSPENGER_DAGLIG_REISE_AMO -> "TPDRAMO"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_UTVIDET_AVKLARING_OPPFOLGING -> "TPBTUAOPPFL"
            StønadTypeTiltakspenger.TILTAKSPENGER_SEMESTERAVGIFT_HOYERE_UTDANNING -> "TPSAHOYUTD"
            StønadTypeTiltakspenger.TILTAKSPENGER_EKSAMENSGEBYR_ENKELT_VGS_OG_HOYERE_UTDANNING -> "TPEGEPVGSHOY"
            StønadTypeTiltakspenger.TILTAKSPENGER_AMO -> "TPTPAMO"
            StønadTypeTiltakspenger.TILTAKSPENGER_OPPFOLGING_ANDRE_GRUPPER -> "TPTPOPPFAG"
            StønadTypeTiltakspenger.TILTAKSPENGER_EKSAMENSGEBYR_ARBEIDSFORBEREDENDE -> "TPEGAFT"
            StønadTypeTiltakspenger.TILTAKSPENGER_SKOLEPENGER_ENKELTPLASS_AMO -> "TPSPEPAMO"
            StønadTypeTiltakspenger.TILTAKSPENGER_INDIVIDUELL_JOBBSTOTTE_UNG -> "TPTPIPSUNG"
            StønadTypeTiltakspenger.TILTAKSPENGER_EKSAMENSGEBYR_ARBEIDSTRENINGSTILTAK -> "TPEGATTILT"
            StønadTypeTiltakspenger.TILTAKSPENGER_UTVIDET_OPPFOLGIN_I_OPPLAERING -> "TPTPUOPPFOPPL"
            StønadTypeTiltakspenger.TILTAKSPENGER_SKOLEPENGER_UTDANNING -> "TPSPUTD1"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_DIGITAL_JOBBKLUBB -> "TPBTDJK"
            StønadTypeTiltakspenger.TILTAKSPENGER_ENKELTPLASS_AMO -> "TPTPEPAMO"
            StønadTypeTiltakspenger.TILTAKSPENGER_SKOLEPENGER_ARBEIDSFORBEREDENDE -> "TPSPAFT"
            StønadTypeTiltakspenger.TILTAKSPENGER_DAGLIG_REISE_AVKLARING_SKJERMET_VIRKSOMHET -> "TPDRASV"
            StønadTypeTiltakspenger.TILTAKSPENGER_BARNETILLEGG_FORSOK_LENGRE_VARIGHET -> "TPBTFLV"
            StønadTypeTiltakspenger.TILTAKSPENGER_SKOLEPENGER_FORSOK_HOYERE_UTDANNING -> "TPSPFHUTD"
            StønadTypeTiltakspenger.TILTAKSPENGER_ARBEIDSRETTET_REHAB_ANDRE_GR__DAG -> "TPTPARREHABAGDAG"
            StønadTypeTiltakspenger.TILTAKSPENGER_ARBEIDSFORBEREDENDE -> "TPTPAFT"
        }
}

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
