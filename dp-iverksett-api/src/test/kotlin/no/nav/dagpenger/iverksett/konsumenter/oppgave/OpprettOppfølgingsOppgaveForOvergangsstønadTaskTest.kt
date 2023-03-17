package no.nav.dagpenger.iverksett.konsumenter.oppgave

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.iverksett.api.IverksettingRepository
import no.nav.dagpenger.iverksett.infrastruktur.repository.findByIdOrThrow
import no.nav.dagpenger.iverksett.lagIverksett
import no.nav.dagpenger.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.Test
import java.util.UUID

internal class OpprettOppfølgingsOppgaveForOvergangsstønadTaskTest {

    private val oppgaveService = mockk<OppgaveService>()
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val taskService = mockk<TaskService>()

    private val taskStegService = OpprettOppfølgingsOppgaveForOvergangsstønadTask(
        oppgaveService,
        iverksettingRepository,
        taskService,
    )

    @Test
    internal fun `skal opprette oppfølgningsoppgave for overgangsstønad`() {
        every { iverksettingRepository.findByIdOrThrow(any()) } returns lagIverksett(opprettIverksettOvergangsstønad())
        every { oppgaveService.skalOppretteVurderHenvendelseOppgave(any()) } returns true
        every { oppgaveService.opprettVurderHenvendelseOppgave(any()) } returns 1

        taskStegService.doTask(opprettTask())

        verify(exactly = 1) { oppgaveService.skalOppretteVurderHenvendelseOppgave(any()) }
        verify(exactly = 1) { oppgaveService.opprettVurderHenvendelseOppgave(any()) }
    }

    @Test
    internal fun `onCompletion oppretter neste task i flyten`() {
        every { taskService.save(any()) } answers { firstArg() }
        val task = opprettTask()
        taskStegService.onCompletion(task)
        verify(exactly = 1) { taskService.save(any()) }
    }

    private fun opprettTask() = Task(OpprettOppfølgingsOppgaveForOvergangsstønadTask.TYPE, UUID.randomUUID().toString())
}
