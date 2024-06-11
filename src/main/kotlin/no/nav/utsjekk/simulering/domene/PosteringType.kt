package no.nav.utsjekk.simulering.domene

enum class PosteringType(val kode: String) {
    YTELSE("YTEL"),
    FEILUTBETALING("FEIL"),
    FORSKUDSSKATT("SKAT"),
    JUSTERING("JUST"),
    TREKK("TREK"),
    MOTPOSTERING("MOTP"),
    ;

    companion object {
        fun fraKode(kode: String): PosteringType {
            for (type in PosteringType.entries) {
                if (type.kode == kode) {
                    return type
                }
            }
            throw IllegalArgumentException("PosteringType finnes ikke for kode $kode")
        }
    }
}
