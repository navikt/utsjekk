package no.nav.utsjekk.simulering.domene

import java.time.LocalDate

data class SimuleringDetaljer(
    val gjelderId: String,
    val datoBeregnet: LocalDate,
    val totalBeløp: Int,
    val perioder: List<Periode>,
)

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate,
    val posteringer: List<Postering>,
)

data class Postering(
    val fagområde: Fagområde,
    val sakId: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val beløp: Int,
    val type: PosteringType,
    val klassekode: String,
)
