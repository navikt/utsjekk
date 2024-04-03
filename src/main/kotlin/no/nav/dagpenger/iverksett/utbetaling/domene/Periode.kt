package no.nav.dagpenger.iverksett.utbetaling.domene

import java.time.LocalDate

data class Periode(val fom: LocalDate, val tom: LocalDate) : Comparable<Periode> {
    init {
        require(tom >= fom) { "Tom-dato før fom-dato er ugyldig: $fom >= $tom" }
    }

    override fun compareTo(other: Periode): Int {
        return Comparator.comparing(Periode::fom).thenComparing(Periode::tom).compare(this, other)
    }
}
