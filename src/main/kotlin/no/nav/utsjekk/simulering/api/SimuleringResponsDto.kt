package no.nav.utsjekk.simulering.api

import no.nav.utsjekk.simulering.client.dto.SimuleringResponse
import java.time.LocalDate

data class SimuleringResponsDto(val oppsummeringer: List<OppsummeringForPeriode>, val rådata: SimuleringResponse)

data class OppsummeringForPeriode(
    val fom: LocalDate,
    val tom: LocalDate,
    val tidligereUtbetalt: Int,
    val nyUtbetaling: Int,
    val totalEtterbetaling: Int,
    val totalFeilutbetaling: Int,
)
