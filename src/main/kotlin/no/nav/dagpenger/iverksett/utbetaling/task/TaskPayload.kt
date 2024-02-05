package no.nav.dagpenger.iverksett.utbetaling.task

import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.behandlingId
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.GeneriskId

data class TaskPayload(val fagsystem: Fagsystem, val behandlingId: GeneriskId, val iverksettingId: String? = null)

fun Iverksetting.tilTaskPayload() =
    TaskPayload(
        fagsystem = this.fagsak.fagsystem,
        behandlingId = this.behandlingId,
        iverksettingId = this.behandling.iverksettingId,
    )
