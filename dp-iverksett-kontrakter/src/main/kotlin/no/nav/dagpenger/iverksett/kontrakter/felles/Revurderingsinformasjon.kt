package no.nav.dagpenger.iverksett.kontrakter.felles

import no.nav.dagpenger.kontrakter.utbetaling.StønadType

enum class Opplysningskilde {
    INNSENDT_SØKNAD,
    MELDING_MODIA,
    INNSENDT_MELDEKORT
}

@Suppress("EnumEntryName", "unused")
enum class Revurderingsårsak(
    vararg stønadstyper: StønadType = arrayOf(
        StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER
    ),
) {
    ENDRING_INNTEKT(StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER),
    SYKDOM(StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER),
    ANNET,
    KLAGE_OMGJØRING,
    ANKE_OMGJØRING,
    ;

    val gjelderStønadstyper = stønadstyper.toSet()

    fun erGyldigForStønadstype(stønadType: StønadType): Boolean {
        return gjelderStønadstyper.contains(stønadType)
    }
}
