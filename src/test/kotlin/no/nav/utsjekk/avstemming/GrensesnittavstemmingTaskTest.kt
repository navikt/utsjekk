package no.nav.utsjekk.avstemming

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.utsjekk.felles.oppdrag.OppdragClient
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.objectMapper
import no.nav.utsjekk.kontrakter.oppdrag.GrensesnittavstemmingRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class GrensesnittavstemmingTaskTest {
    private val oppdragClient = mockk<OppdragClient>()
    private val taskService = mockk<TaskService>()
    private val grensesnittavstemmingTask = GrensesnittavstemmingTask(oppdragClient, taskService)

    @Test
    fun `doTask skal kalle oppdragClient med fradato fra payload og dato for triggerTid som parametere`() {
        val grensesnittavstemmingRequestSlot = slot<GrensesnittavstemmingRequest>()

        every { oppdragClient.grensesnittavstemming(any()) } just Runs

        grensesnittavstemmingTask.doTask(
            Task(
                type = GrensesnittavstemmingTask.TYPE,
                payload = payload,
                triggerTid = LocalDateTime.of(2018, 4, 19, 8, 0),
            ),
        )

        verify(exactly = 1) { oppdragClient.grensesnittavstemming(capture(grensesnittavstemmingRequestSlot)) }
        grensesnittavstemmingRequestSlot.captured.also { request ->
            assertEquals(LocalDate.of(2018, 4, 18).atStartOfDay(), request.fra)
            assertEquals(LocalDate.of(2018, 4, 19).atStartOfDay(), request.til)
            assertEquals(Fagsystem.DAGPENGER, request.fagsystem)
        }
    }

    @Test
    fun `onCompletion skal opprette ny grensesnittavstemmingTask med dato for forrige triggerTid som payload`() {
        val triggeTid = LocalDateTime.of(2018, 4, 19, 8, 0)
        val slot = slot<Task>()
        every { taskService.save(capture(slot)) } returns mockk()

        grensesnittavstemmingTask.onCompletion(
            Task(
                type = GrensesnittavstemmingTask.TYPE,
                payload = payload,
                triggerTid = triggeTid,
            ),
        )
        val forventetPayload =
            objectMapper.writeValueAsString(
                GrensesnittavstemmingPayload(
                    fraDato = LocalDate.of(2018, 4, 19),
                    fagsystem = Fagsystem.DAGPENGER,
                ),
            )

        assertEquals(forventetPayload, slot.captured.payload)
    }

    companion object {
        val payload: String =
            objectMapper.writeValueAsString(
                GrensesnittavstemmingPayload(
                    fraDato = LocalDate.of(2018, 4, 18),
                    fagsystem = Fagsystem.DAGPENGER,
                ),
            )
    }
}
