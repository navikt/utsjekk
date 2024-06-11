package no.nav.utsjekk.simulering.api

import no.nav.utsjekk.simulering.domene.SimuleringDetaljer
import java.time.LocalDate

data class SimuleringResponsDto(val oppsummeringer: List<OppsummeringForPeriode>, val detaljer: SimuleringDetaljer)

data class OppsummeringForPeriode(
    val fom: LocalDate,
    val tom: LocalDate,
    val tidligereUtbetalt: Int,
    val nyUtbetaling: Int,
    val totalEtterbetaling: Int,
    val totalFeilutbetaling: Int,
)
