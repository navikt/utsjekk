package no.nav.dagpenger.iverksett.kontrakter.oppdrag

import java.time.LocalDateTime

data class GrensesnittavstemmingRequest(
    val fagsystem: String,
    val fra: LocalDateTime,
    val til: LocalDateTime
)
