package no.nav.utsjekk.iverksetting.task

import no.nav.utsjekk.iverksetting.domene.Iverksetting
import no.nav.utsjekk.iverksetting.domene.behandlingId
import no.nav.utsjekk.kontrakter.felles.Fagsystem

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
