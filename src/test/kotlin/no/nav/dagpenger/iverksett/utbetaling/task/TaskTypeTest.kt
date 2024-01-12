package no.nav.dagpenger.iverksett.utbetaling.task

import no.nav.dagpenger.iverksett.felles.opprettNesteTask
import java.time.LocalDateTime
import no.nav.dagpenger.iverksett.utbetaling.task.IverksettMotOppdragTask
import no.nav.dagpenger.iverksett.utbetaling.task.VentePåStatusFraØkonomiTask
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TaskTypeTest {

    @Test
    fun `test taskflyt`() {
        val iverksettMotOppdragTask = Task(IverksettMotOppdragTask.TYPE, "")
        assertThat(iverksettMotOppdragTask.type).isEqualTo(IverksettMotOppdragTask.TYPE)
        assertThat(iverksettMotOppdragTask.triggerTid).isBefore(LocalDateTime.now().plusSeconds(1))

        val ventePåStatusFraØkonomiTask = iverksettMotOppdragTask.opprettNesteTask()
        assertThat(ventePåStatusFraØkonomiTask.type).isEqualTo(VentePåStatusFraØkonomiTask.TYPE)
        assertThat(ventePåStatusFraØkonomiTask.triggerTid).isAfter(LocalDateTime.now().plusSeconds(2))
    }
}
