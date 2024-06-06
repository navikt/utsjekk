package no.nav.utsjekk.simulering.client.dto

import java.time.LocalDate

data class SimuleringResponse(
    val gjelderId: String,
    val gjelderNavn: String,
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
    val fagSystemId: String,
    val utbetalesTilId: String,
    val utbetalesTilNavn: String,
    val forfall: LocalDate,
    val feilkonto: Boolean,
    val detaljer: List<Detaljer>,
)

data class Detaljer(
    val faktiskFom: LocalDate,
    val faktiskTom: LocalDate,
    val konto: String,
    val belop: Int,
    val tilbakeforing: Boolean,
    val sats: Double,
    val typeSats: String,
    val antallSats: Int,
    val uforegrad: Int,
    val utbetalingsType: String,
    val klassekode: String,
    val klassekodeBeskrivelse: String?,
    val refunderesOrgNr: String?,
)
