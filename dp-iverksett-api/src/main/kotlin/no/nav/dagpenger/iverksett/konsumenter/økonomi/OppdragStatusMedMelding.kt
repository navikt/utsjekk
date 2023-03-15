package no.nav.dagpenger.iverksett.konsumenter.økonomi

import no.nav.dagpenger.iverksett.kontrakter.oppdrag.OppdragStatus

data class OppdragStatusMedMelding(
    val status: OppdragStatus,
    val melding: String,
)
