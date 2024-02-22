package no.nav.dagpenger.iverksett.status

import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.iverksett.IverksettStatus

data class StatusEndretMelding(
    val sakId: String,
    val behandlingId: String,
    val iverksettingId: String?,
    val fagsystem: Fagsystem,
    val status: IverksettStatus,
)
