package no.nav.dagpenger.iverksett.avstemming

import no.nav.dagpenger.iverksett.utbetaling.tilstand.erAktiv
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class AvstemmingService(private val taskService: TaskService) {

    fun lagreGrensesnittavstemmingTask(): Boolean {
        if (taskService.findAll().any { it.type == GrensesnittavstemmingTask.TYPE && it.erAktiv() }) {
            logger.info("Plukkbar task for grensesnittavstemming allerede opprettet - lager ikke ny task")
            return false
        }

        val grensesnittavstemmingDto = GrensesnittavstemmingDto(
                stønadstype = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
                fraDato = LocalDate.now().minusDays(5),
                triggerTid = LocalDateTime.now(),
        )
        taskService.save(grensesnittavstemmingDto.tilTask())
        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}