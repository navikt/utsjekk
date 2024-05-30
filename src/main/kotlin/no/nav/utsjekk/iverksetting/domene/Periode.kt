package no.nav.utsjekk.iverksetting.domene

import java.time.LocalDate

data class Periode(val fom: LocalDate, val tom: LocalDate) : Comparable<Periode> {
    init {
        require(tom >= fom) { "Tom-dato $tom før fom-dato $fom er ugyldig" }
    }

    override fun compareTo(other: Periode): Int {
        return Comparator.comparing(Periode::fom).thenComparing(Periode::tom).compare(this, other)
    }
}
