package no.nav.dagpenger.iverksett.kontrakter.oppdrag

import java.time.LocalDateTime

data class KonsistensavstemmingRequestV2(
    val fagsystem: String,
    val perioderForBehandlinger: List<PerioderForBehandling>,
    val avstemmingstidspunkt: LocalDateTime
)
