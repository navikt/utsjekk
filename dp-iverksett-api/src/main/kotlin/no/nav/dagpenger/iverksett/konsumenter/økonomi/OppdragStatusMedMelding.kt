package no.nav.dagpenger.iverksett.konsumenter.økonomi

import no.nav.familie.kontrakter.felles.oppdrag.OppdragStatus

data class OppdragStatusMedMelding(
    val status: OppdragStatus,
    val melding: String,
)
