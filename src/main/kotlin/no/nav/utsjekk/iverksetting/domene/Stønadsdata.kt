package no.nav.utsjekk.iverksetting.domene

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.SerializerProvider
import no.nav.utsjekk.felles.http.ObjectMapperProvider.objectMapper
import no.nav.utsjekk.kontrakter.felles.BrukersNavKontor
import no.nav.utsjekk.kontrakter.felles.StønadType
import no.nav.utsjekk.kontrakter.felles.StønadTypeDagpenger
import no.nav.utsjekk.kontrakter.felles.StønadTypeTilleggsstønader
import no.nav.utsjekk.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.utsjekk.kontrakter.iverksett.Ferietillegg

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
    JsonSubTypes.Type(StønadsdataDagpenger::class, name = "dagpenger"),
    JsonSubTypes.Type(StønadsdataTiltakspenger::class, name = "tiltakspenger"),
    JsonSubTypes.Type(StønadsdataTilleggsstønader::class, name = "tilleggsstønader"),
)
sealed class Stønadsdata(
    open val stønadstype: StønadType,
) {
    fun tilKlassifisering(): String =
        when (this) {
            is StønadsdataDagpenger -> this.tilKlassifiseringDagpenger()
            is StønadsdataTiltakspenger -> this.tilKlassifiseringTiltakspenger()
            is StønadsdataTilleggsstønader -> this.tilKlassifiseringTilleggsstønader()
        }

    abstract fun tilKjedenøkkel(): Kjedenøkkel
}

data class StønadsdataDagpenger(
    override val stønadstype: StønadTypeDagpenger,
    val ferietillegg: Ferietillegg? = null,
) : Stønadsdata(stønadstype) {
    fun tilKlassifiseringDagpenger(): String =
        when (this.stønadstype) {
            StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR ->
                when (ferietillegg) {
                    Ferietillegg.ORDINÆR -> "DPORASFE"
                    Ferietillegg.AVDØD -> "DPORASFE-IOP"
                    null -> "DPORAS"
                }

            StønadTypeDagpenger.DAGPENGER_PERMITTERING_ORDINÆR ->
                when (ferietillegg) {
                    Ferietillegg.ORDINÆR -> "DPPEASFE1"
                    Ferietillegg.AVDØD -> "DPPEASFE1-IOP"
                    null -> "DPPEAS"
                }

            StønadTypeDagpenger.DAGPENGER_PERMITTERING_FISKEINDUSTRI ->
                when (ferietillegg) {
                    Ferietillegg.ORDINÆR -> "DPPEFIFE1"
                    Ferietillegg.AVDØD -> "DPPEFIFE1-IOP"
                    null -> "DPPEFI"
                }

            StønadTypeDagpenger.DAGPENGER_EØS ->
                when (ferietillegg) {
                    Ferietillegg.ORDINÆR -> "DPFEASISP"
                    Ferietillegg.AVDØD -> throw IllegalArgumentException("Eksport-gruppen har ingen egen kode for ferietillegg til avdød")
                    null -> "DPDPASISP1"
                }
        }

    // TODO faktisk meldekortId må inn her når grensesnittet er utvidet
    override fun tilKjedenøkkel(): Kjedenøkkel =
        KjedenøkkelMeldeplikt(klassifiseringskode = this.tilKlassifiseringDagpenger(), meldekortId = "TODO")
}

data class StønadsdataTiltakspenger(
    override val stønadstype: StønadTypeTiltakspenger,
    val barnetillegg: Boolean = false,
    val brukersNavKontor: BrukersNavKontor,
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

    // TODO faktisk meldekortId må inn her når grensesnittet er utvidet
    override fun tilKjedenøkkel(): Kjedenøkkel =
        KjedenøkkelMeldeplikt(klassifiseringskode = this.tilKlassifiseringTiltakspenger(), meldekortId = "TODO")
}

data class StønadsdataTilleggsstønader(
    override val stønadstype: StønadTypeTilleggsstønader,
    val brukersNavKontor: BrukersNavKontor? = null,
) : Stønadsdata(stønadstype) {
    fun tilKlassifiseringTilleggsstønader() =
        when (stønadstype) {
            StønadTypeTilleggsstønader.TILSYN_BARN_ENSLIG_FORSØRGER -> "TSTBASISP2-OP"
            StønadTypeTilleggsstønader.TILSYN_BARN_AAP -> "TSTBASISP4-OP"
            StønadTypeTilleggsstønader.TILSYN_BARN_ETTERLATTE -> "TSTBASISP5-OP"
        }

    override fun tilKjedenøkkel(): Kjedenøkkel = KjedenøkkelStandard(klassifiseringskode = this.tilKlassifiseringTilleggsstønader())
}
