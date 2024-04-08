package no.nav.utsjekk.avstemming

import no.nav.familie.prosessering.internal.TaskService
import no.nav.utsjekk.felles.http.advice.ApiFeil
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.utbetaling.tilstand.erAktiv
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class AvstemmingService(private val taskService: TaskService) {
    fun opprettGrensesnittavstemmingTask(fagsystem: Fagsystem) {
        if (taskService.findAll()
                .any { it.type == GrensesnittavstemmingTask.TYPE && it.payload.contains(fagsystem.name) && it.erAktiv() }
        ) {
            logger.info("Plukkbar task for grensesnittavstemming allerede opprettet - lager ikke ny task")
            throw ApiFeil(
                httpStatus = HttpStatus.CONFLICT,
                feil = "Grensesnittavstemming kjører allerede for fagsystem $fagsystem",
            )
        }

        val grensesnittavstemmingDto =
            GrensesnittavstemmingDto(
                fagsystem = fagsystem,
                fraDato = LocalDate.now().minusDays(5),
                triggerTid = LocalDateTime.now(),
            )
        taskService.save(grensesnittavstemmingDto.tilTask())
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
