package no.nav.dagpenger.iverksett.konsumenter.økonomi

import no.nav.dagpenger.kontrakter.iverksett.oppdrag.OppdragStatus

data class OppdragStatusMedMelding(
    val status: OppdragStatus,
    val feilmelding: String?,
)
