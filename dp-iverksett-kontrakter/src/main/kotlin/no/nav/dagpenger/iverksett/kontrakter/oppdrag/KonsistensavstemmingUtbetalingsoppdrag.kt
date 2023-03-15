package no.nav.dagpenger.iverksett.kontrakter.oppdrag

import java.time.LocalDateTime

data class KonsistensavstemmingUtbetalingsoppdrag(
    val fagsystem: String,
    val utbetalingsoppdrag: List<Utbetalingsoppdrag>,
    val avstemmingstidspunkt: LocalDateTime
)
