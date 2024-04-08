package no.nav.utsjekk.utbetaling.task

import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.utbetaling.domene.Iverksetting
import no.nav.utsjekk.utbetaling.domene.behandlingId

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
