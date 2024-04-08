package no.nav.utsjekk.avstemming

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.utsjekk.felles.http.advice.ApiFeil
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.objectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class AvstemmingServiceTest {
    private val taskServiceMock = mockk<TaskService>()
    private val avstemmingService = AvstemmingService(taskServiceMock)

    @Test
    fun `skal lage ny task når grensesnittavstemming kjører for annet fagsystem`() {
        val payloadDagpengerTask =
            objectMapper.writeValueAsString(
                GrensesnittavstemmingPayload(
                    fraDato = LocalDate.now().minusDays(1),
                    fagsystem = Fagsystem.DAGPENGER,
                ),
            )
        every {
            taskServiceMock.findAll()
        } returns listOf(Task(type = GrensesnittavstemmingTask.TYPE, payload = payloadDagpengerTask, status = Status.KLAR_TIL_PLUKK))
        every {
            taskServiceMock.save(any())
        } returns Task(type = GrensesnittavstemmingTask.TYPE, payload = payloadDagpengerTask, status = Status.UBEHANDLET)

        assertDoesNotThrow {
            avstemmingService.opprettGrensesnittavstemmingTask(fagsystem = Fagsystem.TILTAKSPENGER)
        }
    }

    @Test
    fun `skal ikke lage ny task når grensesnittavstemming kjører for samme fagsystem`() {
        val payloadDagpengerTask =
            objectMapper.writeValueAsString(
                GrensesnittavstemmingPayload(
                    fraDato = LocalDate.now().minusDays(1),
                    fagsystem = Fagsystem.DAGPENGER,
                ),
            )
        every {
            taskServiceMock.findAll()
        } returns listOf(Task(type = GrensesnittavstemmingTask.TYPE, payload = payloadDagpengerTask, status = Status.KLAR_TIL_PLUKK))

        assertThrows<ApiFeil> {
            avstemmingService.opprettGrensesnittavstemmingTask(fagsystem = Fagsystem.DAGPENGER)
        }
    }
}
