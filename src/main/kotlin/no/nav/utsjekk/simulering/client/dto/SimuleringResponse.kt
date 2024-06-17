package no.nav.utsjekk.simulering.client.dto

import java.time.LocalDate

data class SimuleringResponse(
    val gjelderId: String,
    val datoBeregnet: LocalDate,
    val totalBelop: Int,
    val perioder: List<SimulertPeriode>,
)

data class SimulertPeriode(
    val fom: LocalDate,
    val tom: LocalDate,
    val utbetalinger: List<Utbetaling>,
)

// Dette er det samme som et stoppnivå i SOAP
data class Utbetaling(
    val fagområde: String,
    val fagSystemId: String,
    val utbetalesTilId: String,
    val forfall: LocalDate,
    val feilkonto: Boolean,
    val detaljer: List<PosteringDto>,
)

// Tilsvarer én rad i regnskapet
data class PosteringDto(
    val type: String,
    val faktiskFom: LocalDate,
    val faktiskTom: LocalDate,
    val belop: Int,
    val sats: Double,
    val satstype: String?,
    val klassekode: String,
    val trekkVedtakId: Long?,
    val refunderesOrgNr: String?,
)
