package no.nav.dagpenger.iverksett.kontrakter.felles

enum class Opplysningskilde {
    INNSENDT_SØKNAD,
    MELDING_MODIA,
    INNSENDT_MELDEKORT
}

@Suppress("EnumEntryName", "unused")
enum class Revurderingsårsak(
    vararg stønadstyper: StønadType = arrayOf(
        StønadType.DAGPENGER
    ),
) {
    ENDRING_INNTEKT(StønadType.DAGPENGER),
    SYKDOM(StønadType.DAGPENGER),
    ANNET,
    KLAGE_OMGJØRING,
    ANKE_OMGJØRING,
    ;

    val gjelderStønadstyper = stønadstyper.toSet()

    fun erGyldigForStønadstype(stønadType: StønadType): Boolean {
        return gjelderStønadstyper.contains(stønadType)
    }
}
