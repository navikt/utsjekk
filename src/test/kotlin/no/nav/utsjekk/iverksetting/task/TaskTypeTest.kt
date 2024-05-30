package no.nav.utsjekk.iverksetting.task

import no.nav.familie.prosessering.domene.Task
import no.nav.utsjekk.felles.opprettNesteTask
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
