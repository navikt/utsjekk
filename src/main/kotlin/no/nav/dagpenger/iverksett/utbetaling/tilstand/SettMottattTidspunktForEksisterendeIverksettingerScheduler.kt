package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.utbetaling.domene.IverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.domene.behandlingId
import no.nav.dagpenger.iverksett.utbetaling.domene.sakId
import no.nav.dagpenger.iverksett.utbetaling.task.IverksettMotOppdragTask
import no.nav.dagpenger.iverksett.utbetaling.task.TaskPayload
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class SettMottattTidspunktForEksisterendeIverksettingerScheduler(
    private val iverksettingRepository: IverksettingRepository,
    private val taskService: TaskService,
) {
    @Scheduled(initialDelay = 5, fixedDelay = 500, timeUnit = TimeUnit.MINUTES)
    fun migrer() {
        logger.info("Starter jobb for å sette mottatt tidspunkt på iverksettinger som mangler det")
        val iverksettingerUtenTimestamp = iverksettingRepository.findByEmptyMottattTidspunkt()
        iverksettingerUtenTimestamp.forEach {
            val taskTimestamp = hentOpprettetTidspunktForIverksettingstask(it)
            iverksettingRepository.settMottattTidspunktForIverksetting(it.copy(mottattTidspunkt = taskTimestamp))
            logger.info("Satt mottatt tidspunkt $taskTimestamp for iverksetting ${it.behandlingId}")
        }
        logger.info("Fullført jobb for å sette manglende mottatt tidspunkt")
    }

    private fun hentOpprettetTidspunktForIverksettingstask(iverksetting: IverksettingEntitet): LocalDateTime? {
        val taskPayload =
            TaskPayload(
                fagsystem = iverksetting.data.fagsak.fagsystem,
                sakId = iverksetting.data.sakId,
                behandlingId = iverksetting.data.behandlingId,
                iverksettingId = iverksetting.data.behandling.iverksettingId,
            )

        val iverksettTaskForIverksetting =
            taskService.finnTaskMedPayloadOgType(
                payload = objectMapper.writeValueAsString(taskPayload),
                type = IverksettMotOppdragTask.TYPE,
            ) ?: taskService.finnTaskMedPayloadOgType(
                payload = objectMapper.writeValueAsString(iverksetting.data.behandlingId),
                type = IverksettMotOppdragTask.TYPE,
            ) ?: taskService.finnTaskMedPayloadOgType(
                payload = iverksetting.behandlingId.toString(),
                type = IverksettMotOppdragTask.TYPE,
            )

        return iverksettTaskForIverksetting?.opprettetTid
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
