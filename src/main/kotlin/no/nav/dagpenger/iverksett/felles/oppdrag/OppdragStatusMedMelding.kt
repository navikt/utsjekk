package no.nav.dagpenger.iverksett.felles.oppdrag

import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus

data class OppdragStatusMedMelding(
    val status: OppdragStatus,
    val feilmelding: String?,
)
