package no.nav.dagpenger.iverksett.konsumenter

import no.nav.dagpenger.iverksett.konsumenter.arbeidsoppfolging.SendVedtakTilArbeidsoppfølgingTask
import no.nav.dagpenger.iverksett.konsumenter.arena.SendFattetVedtakTilArenaTask
import no.nav.dagpenger.iverksett.konsumenter.brev.DistribuerVedtaksbrevTask
import no.nav.dagpenger.iverksett.konsumenter.brev.JournalførVedtaksbrevTask
import no.nav.dagpenger.iverksett.konsumenter.oppgave.OpprettOppfølgingsOppgaveForDagpengerTask
import no.nav.dagpenger.iverksett.konsumenter.tilbakekreving.OpprettTilbakekrevingTask
import no.nav.dagpenger.iverksett.konsumenter.vedtak.PubliserVedtakTilKafkaTask
import no.nav.dagpenger.iverksett.konsumenter.økonomi.IverksettMotOppdragTask
import no.nav.dagpenger.iverksett.konsumenter.økonomi.VentePåStatusFraØkonomiTask
import no.nav.familie.prosessering.domene.Task
import java.time.LocalDateTime

class TaskType(
    val type: String,
    val triggerTidAntallSekunderFrem: Long? = null,
)

fun hovedflyt() = listOf(
    TaskType(OpprettTilbakekrevingTask.TYPE),
    TaskType(IverksettMotOppdragTask.TYPE),
    TaskType(VentePåStatusFraØkonomiTask.TYPE, 20), // går ikke videre ved migrering//korrigering_uten_brev
    TaskType(JournalførVedtaksbrevTask.TYPE),
    TaskType(DistribuerVedtaksbrevTask.TYPE),
)

fun publiseringsflyt() = listOf(
// Hopper til vedtakstatistikk ved migrering
    TaskType(SendFattetVedtakTilArenaTask.TYPE),
    TaskType(PubliserVedtakTilKafkaTask.TYPE),
    TaskType(SendVedtakTilArbeidsoppfølgingTask.TYPE),
    TaskType(OpprettOppfølgingsOppgaveForDagpengerTask.TYPE),
)

fun TaskType.nesteHovedflytTask() = hovedflyt().zipWithNext().first { this.type == it.first.type }.second
fun TaskType.nestePubliseringsflytTask() = publiseringsflyt().zipWithNext().first { this.type == it.first.type }.second

fun Task.opprettNesteTask(): Task {
    val nesteTask = TaskType(this.type).nesteHovedflytTask()
    return lagTask(nesteTask)
}

fun Task.opprettNestePubliseringTask(): Task {
    val nesteTask = TaskType(this.type).nestePubliseringsflytTask()
    return lagTask(nesteTask)
}

private fun Task.lagTask(nesteTask: TaskType): Task {
    return if (nesteTask.triggerTidAntallSekunderFrem != null) {
        Task(
            type = nesteTask.type,
            payload = this.payload,
            properties = this.metadata,
        ).copy(
            triggerTid = LocalDateTime.now()
                .plusSeconds(nesteTask.triggerTidAntallSekunderFrem),
        )
    } else {
        Task(
            type = nesteTask.type,
            payload = this.payload,
            properties = this.metadata,
        )
    }
}
