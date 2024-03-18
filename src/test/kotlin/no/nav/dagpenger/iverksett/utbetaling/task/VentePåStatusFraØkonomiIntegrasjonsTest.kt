package no.nav.dagpenger.iverksett.utbetaling.task

import no.nav.dagpenger.iverksett.Integrasjonstest
import no.nav.dagpenger.iverksett.initializers.KafkaContainerInitializer
import no.nav.dagpenger.iverksett.utbetaling.domene.behandlingId
import no.nav.dagpenger.iverksett.utbetaling.domene.sakId
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.iverksett.utbetaling.util.enIverksetting
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.dagpenger.kontrakter.iverksett.IverksettStatus
import no.nav.dagpenger.kontrakter.iverksett.StatusEndretMelding
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
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

    @AfterEach
    fun cleanup() {
        taskService.deleteAll(taskService.findAll())
    }

    @Test
    fun `publiserer status for iverksetting når man mottar kvittering fra oppdrag`() {
        val iverksetting = enIverksetting()
        iverksettingService.startIverksetting(iverksetting)

        taskService.findAll().let { tasks ->
            iverksettMotOppdragTask.doTask(tasks.first())
            iverksettMotOppdragTask.onCompletion(tasks.first())
        }
        taskService.findAll().let { tasks ->
            ventePåStatusFraØkonomiTask.doTask(tasks.last())
        }

        val alleMeldinger = KafkaContainerInitializer.getAllRecords()
        val førsteMelding = objectMapper.readValue(alleMeldinger.first().value(), StatusEndretMelding::class.java)
        val andreMelding = objectMapper.readValue(alleMeldinger.last().value(), StatusEndretMelding::class.java)

        assertEquals(2, alleMeldinger.count())
        assertEquals(iverksetting.sakId, førsteMelding.sakId)
        assertEquals(iverksetting.sakId, andreMelding.sakId)
        assertEquals(iverksetting.behandlingId, førsteMelding.behandlingId)
        assertEquals(iverksetting.behandlingId, andreMelding.behandlingId)
        assertEquals(iverksetting.behandling.iverksettingId, førsteMelding.iverksettingId)
        assertEquals(iverksetting.behandling.iverksettingId, andreMelding.iverksettingId)
        assertEquals(IverksettStatus.SENDT_TIL_OPPDRAG, førsteMelding.status)
        assertEquals(IverksettStatus.OK, andreMelding.status)
    }
}
