package no.nav.dagpenger.iverksett.utbetaling.task

import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.behandlingId
import no.nav.dagpenger.kontrakter.felles.Fagsystem

data class TaskPayload(
    val fagsystem: Fagsystem,
    val sakId: String,
    val behandlingId: String,
    val iverksettingId: String? = null,
)

fun Iverksetting.tilTaskPayload() =
    TaskPayload(
        fagsystem = this.fagsak.fagsystem,
        sakId = this.fagsak.fagsakId,
        behandlingId = this.behandlingId,
        iverksettingId = this.behandling.iverksettingId,
    )
