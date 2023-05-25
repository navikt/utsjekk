package no.nav.dagpenger.iverksett.konsumenter.oppgave

import no.nav.dagpenger.iverksett.api.IverksettingRepository
import no.nav.dagpenger.iverksett.kontrakter.iverksett.IverksettDagpenger
import no.nav.dagpenger.iverksett.infrastruktur.repository.findByIdOrThrow
import no.nav.dagpenger.iverksett.konsumenter.opprettNestePubliseringTask
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = OpprettOppfølgingsOppgaveForDagpengerTask.TYPE,
    beskrivelse = "Oppretter oppgave om at bruker har innvilget dagpenger",
)
class OpprettOppfølgingsOppgaveForDagpengerTask(
    private val oppgaveService: OppgaveService,
    private val iverksettingRepository: IverksettingRepository,
    private val taskService: TaskService,
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId)
        if (iverksett.data !is IverksettDagpenger) {
            logger.info(
                "Oppretter ikke oppfølgningsoppgave for behandling=$behandlingId" +
                    " då den ikke er dagpenger (${iverksett::class.java.simpleName})",
            )
            return
        }

        if (oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett.data)) {
            val oppgaveId = oppgaveService.opprettVurderHenvendelseOppgave(iverksett.data)
            logger.info("Opprettet oppgave for behandling=$behandlingId oppgaveID=$oppgaveId")
        }
    }

    override fun onCompletion(task: Task) {
        taskService.save(task.opprettNestePubliseringTask())
    }

    companion object {

        const val TYPE = "opprettOppfølgingOppgaveForInnvilgetDagpenger"
    }
}
