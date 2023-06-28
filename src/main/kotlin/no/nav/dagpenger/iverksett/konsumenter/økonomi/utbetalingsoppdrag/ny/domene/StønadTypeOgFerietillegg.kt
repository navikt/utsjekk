package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene

import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg

data class StønadTypeOgFerietillegg(
    val stønadstype: StønadType,
    val ferietillegg: Ferietillegg? = null,
)

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
