package no.nav.dagpenger.iverksett.utbetaling.task

import no.nav.dagpenger.iverksett.Integrasjonstest
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.iverksett.utbetaling.util.enIverksetting
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VentePåStatusFraØkonomiIntegrasjonsTest : Integrasjonstest() {
    @Autowired
    lateinit var iverksettingService: IverksettingService

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    private lateinit var iverksettMotOppdragTask: IverksettMotOppdragTask

    @Autowired
    private lateinit var ventePåStatusFraØkonomiTask: VentePåStatusFraØkonomiTask

    @Test
    fun `publiserer status for iverksetting når man mottar kvittering fra oppdrag`() {
        iverksettingService.startIverksetting(enIverksetting())

        taskService.findAll().let { tasks ->
            iverksettMotOppdragTask.doTask(tasks.first())
        }
    }
}
