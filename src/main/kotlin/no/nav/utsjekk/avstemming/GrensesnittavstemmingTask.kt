package no.nav.utsjekk.avstemming

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.utsjekk.felles.oppdrag.OppdragClient
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.objectMapper
import no.nav.utsjekk.kontrakter.oppdrag.GrensesnittavstemmingRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

data class GrensesnittavstemmingPayload(val fraDato: LocalDate, val fagsystem: Fagsystem)

@Service
@TaskStepBeskrivelse(taskStepType = GrensesnittavstemmingTask.TYPE, beskrivelse = "Utfører grensesnittavstemming mot økonomi.")
class GrensesnittavstemmingTask(
    private val oppdragClient: OppdragClient,
    private val taskService: TaskService,
) : AsyncTaskStep {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun doTask(task: Task) {
        with(objectMapper.readValue<GrensesnittavstemmingPayload>(task.payload)) {
            val fraTidspunkt = fraDato.atStartOfDay()
            val tilTidspunkt = task.triggerTid.toLocalDate().atStartOfDay()

            logger.info("Gjør avstemming for $fagsystem mot oppdrag fra $fraTidspunkt til $tilTidspunkt, task-id ${task.id}")
            val grensesnittavstemmingRequest =
                GrensesnittavstemmingRequest(
                    fagsystem = fagsystem,
                    fra = fraTidspunkt,
                    til = tilTidspunkt,
                )
            oppdragClient.grensesnittavstemming(grensesnittavstemmingRequest)
        }
    }

    override fun onCompletion(task: Task) {
        val payload = objectMapper.readValue<GrensesnittavstemmingPayload>(task.payload)
        val nesteFradato = task.triggerTid.toLocalDate()
        opprettGrensesnittavstemmingTask(GrensesnittavstemmingDto(fagsystem = payload.fagsystem, fraDato = nesteFradato))
    }

    fun opprettGrensesnittavstemmingTask(grensesnittavstemmingDto: GrensesnittavstemmingDto) =
        grensesnittavstemmingDto.tilTask()
            .let { taskService.save(it) }

    companion object {
        const val TYPE = "utførGrensesnittavstemming"
    }
}
