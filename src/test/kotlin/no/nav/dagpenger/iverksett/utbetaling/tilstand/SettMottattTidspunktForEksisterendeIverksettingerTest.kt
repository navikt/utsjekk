package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.Integrasjonstest
import no.nav.dagpenger.iverksett.utbetaling.domene.behandlingId
import no.nav.dagpenger.iverksett.utbetaling.domene.sakId
import no.nav.dagpenger.iverksett.utbetaling.lagIverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.task.IverksettMotOppdragTask
import no.nav.dagpenger.iverksett.utbetaling.task.TaskPayload
import no.nav.dagpenger.iverksett.utbetaling.util.enIverksetting
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.dagpenger.kontrakter.felles.somUUID
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

class SettMottattTidspunktForEksisterendeIverksettingerTest : Integrasjonstest() {
    @Autowired
    private lateinit var settMottattTidspunktForEksisterendeIverksettinger:
        SettMottattTidspunktForEksisterendeIverksettinger

    @Autowired
    private lateinit var taskService: TaskService

    @Autowired
    private lateinit var iverksettingRepository: IverksettingRepository

    @Test
    fun `migrer iverksettinger`() {
        val iverksettingData = enIverksetting()
        iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData).copy(mottattTidspunkt = null))
        val task =
            Task(
                type = IverksettMotOppdragTask.TYPE,
                payload =
                    objectMapper.writeValueAsString(
                        TaskPayload(
                            fagsystem = iverksettingData.fagsak.fagsystem,
                            sakId = iverksettingData.sakId,
                            behandlingId = iverksettingData.behandlingId,
                            iverksettingId = iverksettingData.behandling.iverksettingId,
                        ),
                    ),
            )
        taskService.save(task)

        val iverksettingData2 = enIverksetting(iverksettingId = "ABCD")
        iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData2).copy(mottattTidspunkt = null))
        val task2 =
            Task(
                type = IverksettMotOppdragTask.TYPE,
                payload =
                    objectMapper.writeValueAsString(
                        TaskPayload(
                            fagsystem = iverksettingData2.fagsak.fagsystem,
                            sakId = iverksettingData2.sakId,
                            behandlingId = iverksettingData2.behandlingId,
                            iverksettingId = iverksettingData2.behandling.iverksettingId,
                        ),
                    ),
            )
        taskService.save(task2)

        val iverksettingData3 = enIverksetting()
        iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData3).copy(mottattTidspunkt = null))
        val task3 =
            Task(
                type = IverksettMotOppdragTask.TYPE,
                payload = objectMapper.writeValueAsString(iverksettingData3.behandlingId),
            )
        taskService.save(task3)

        val iverksettingData4 = enIverksetting()
        iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData4).copy(mottattTidspunkt = null))
        val task4 =
            Task(
                type = IverksettMotOppdragTask.TYPE,
                payload = iverksettingData4.behandlingId.somUUID.toString(),
            )
        taskService.save(task4)

        val iverksettingData5 = enIverksetting()
        iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData5).copy(mottattTidspunkt = LocalDateTime.now()))

        assertEquals(4, iverksettingRepository.findByEmptyMottattTidspunkt().size)

        settMottattTidspunktForEksisterendeIverksettinger.migrer()

        assertEquals(0, iverksettingRepository.findByEmptyMottattTidspunkt().size)
    }
}
