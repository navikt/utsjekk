package no.nav.utsjekk.iverksetting.task

import no.nav.familie.prosessering.internal.TaskService
import no.nav.utsjekk.Integrasjonstest
import no.nav.utsjekk.initializers.KafkaContainerInitializer
import no.nav.utsjekk.iverksetting.domene.behandlingId
import no.nav.utsjekk.iverksetting.domene.sakId
import no.nav.utsjekk.iverksetting.tilstand.IverksettingService
import no.nav.utsjekk.iverksetting.util.enIverksetting
import no.nav.utsjekk.kontrakter.felles.objectMapper
import no.nav.utsjekk.kontrakter.iverksett.IverksettStatus
import no.nav.utsjekk.kontrakter.iverksett.StatusEndretMelding
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
