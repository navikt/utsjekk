package no.nav.dagpenger.iverksett.konsumenter.vedtak

import no.nav.dagpenger.iverksett.api.IverksettingRepository
import no.nav.dagpenger.iverksett.infrastruktur.repository.findByIdOrThrow
import no.nav.dagpenger.iverksett.konsumenter.opprettNestePubliseringTask
import no.nav.dagpenger.kontrakter.iverksett.samordning.DagpengerVedtakhendelse
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = PubliserVedtakTilKafkaTask.TYPE,
    beskrivelse = "Publiserer vedtak på kafka.",
    settTilManuellOppfølgning = true,
)
class PubliserVedtakTilKafkaTask(
    private val taskService: TaskService,
    private val iverksettingRepository: IverksettingRepository,
    private val vedtakKafkaProducer: VedtakKafkaProducer,
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data
        vedtakKafkaProducer.sendVedtak(
            DagpengerVedtakhendelse(
                behandlingId = iverksett.behandling.behandlingId,
                personIdent = iverksett.søker.personIdent,
                stønadType = iverksett.fagsak.stønadstype,
            ),
        )
    }

    override fun onCompletion(task: Task) {
        taskService.save(task.opprettNestePubliseringTask())
    }

    companion object {

        const val TYPE = "publiserVedtakPåKafka"
    }
}
