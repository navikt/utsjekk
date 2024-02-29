package no.nav.dagpenger.iverksett.felles

import no.nav.dagpenger.iverksett.utbetaling.task.IverksettMotOppdragTask
import no.nav.dagpenger.iverksett.utbetaling.task.VentePåStatusFraØkonomiTask
import no.nav.familie.prosessering.domene.Task
import java.time.LocalDateTime

class TaskType(
    val type: String,
    val triggerTidAntallSekunderFrem: Long? = null,
)

fun hovedflyt() =
    listOf(
        TaskType(IverksettMotOppdragTask.TYPE),
        TaskType(VentePåStatusFraØkonomiTask.TYPE, 20),
    )

fun TaskType.nesteHovedflytTask() = hovedflyt().zipWithNext().first { this.type == it.first.type }.second

fun Task.opprettNesteTask(): Task {
    val nesteTask = TaskType(this.type).nesteHovedflytTask()
    return lagTask(nesteTask)
}

private fun Task.lagTask(nesteTask: TaskType): Task {
    return if (nesteTask.triggerTidAntallSekunderFrem != null) {
        Task(
            type = nesteTask.type,
            payload = this.payload,
            properties = this.metadata,
        ).copy(
            triggerTid =
                LocalDateTime.now()
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
