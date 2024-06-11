package no.nav.utsjekk.simulering.domene

import no.nav.utsjekk.kontrakter.felles.Fagsystem

enum class Fagområde(val kode: String) {
    TILLEGGSSTØNADER("TILLST"),
    TILLEGGSSTØNADER_ARENA("TSTARENA"),
    TILLEGGSSTØNADER_ARENA_MANUELL_POSTERING("MTSTAREN"),

    DAGPENGER("DP"),
    DAGPENGER_MANUELL_POSTERING("MDP"),
    DAGPENGER_ARENA("DPARENA"),
    DAGPENGER_ARENA_MANUELL_POSTERING("MDPARENA"),

    TILTAKSPENGER("TILTPENG"),
    TILTAKSPENGER_ARENA("TPARENA"),
    TILTAKSPENGER_ARENA_MANUELL_POSTERING("MTPARENA"),
    ;

    companion object {
        fun fraKode(kode: String): Fagområde {
            for (fagområde in Fagområde.entries) {
                if (fagområde.kode == kode) {
                    return fagområde
                }
            }
            throw IllegalArgumentException("Fagområde finnes ikke for kode $kode")
        }
    }
}

fun hentFagområdeKoderFor(fagsystem: Fagsystem): Set<Fagområde> =
    when (fagsystem) {
        Fagsystem.DAGPENGER ->
            setOf(
                Fagområde.DAGPENGER,
                Fagområde.DAGPENGER_ARENA,
                Fagområde.DAGPENGER_ARENA_MANUELL_POSTERING,
                Fagområde.DAGPENGER_MANUELL_POSTERING,
            )

        Fagsystem.TILTAKSPENGER ->
            setOf(
                Fagområde.TILTAKSPENGER,
                Fagområde.TILTAKSPENGER_ARENA,
                Fagområde.TILTAKSPENGER_ARENA_MANUELL_POSTERING,
            )

        Fagsystem.TILLEGGSSTØNADER ->
            setOf(
                Fagområde.TILLEGGSSTØNADER,
                Fagområde.TILLEGGSSTØNADER_ARENA,
                Fagområde.TILLEGGSSTØNADER_ARENA_MANUELL_POSTERING,
            )
    }
