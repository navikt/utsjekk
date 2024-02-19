package no.nav.dagpenger.iverksett.utbetaling.task

import no.nav.dagpenger.iverksett.felles.opprettNesteTask
import no.nav.familie.prosessering.domene.Task
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class TaskTypeTest {
    @Test
    fun `test taskflyt`() {
        val iverksettMotOppdragTask = Task(IverksettMotOppdragTask.TYPE, "")
        assertEquals(IverksettMotOppdragTask.TYPE, iverksettMotOppdragTask.type)
        assertTrue(iverksettMotOppdragTask.triggerTid.isBefore(LocalDateTime.now().plusSeconds(1)))

        val ventePåStatusFraØkonomiTask = iverksettMotOppdragTask.opprettNesteTask()
        assertEquals(VentePåStatusFraØkonomiTask.TYPE, ventePåStatusFraØkonomiTask.type)
        assertTrue(ventePåStatusFraØkonomiTask.triggerTid.isAfter(LocalDateTime.now().plusSeconds(2)))
    }
}
