package no.nav.dagpenger.iverksett.konsumenter.økonomi

import no.nav.dagpenger.iverksett.api.IverksettingRepository
import no.nav.dagpenger.iverksett.api.IverksettingService
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.configuration.FeatureToggleConfig
import no.nav.dagpenger.iverksett.infrastruktur.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.infrastruktur.repository.findByIdOrThrow
import no.nav.dagpenger.iverksett.konsumenter.opprettNesteTask
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = VentePåStatusFraØkonomiTask.TYPE,
    maxAntallFeil = 50,
    settTilManuellOppfølgning = true,
    triggerTidVedFeilISekunder = 30L,
    beskrivelse = "Sjekker status på utbetalningsoppdraget mot økonomi.",
)
class VentePåStatusFraØkonomiTask(
    private val iverksettingRepository: IverksettingRepository,
    private val iverksettingService: IverksettingService,
    private val taskService: TaskService,
    private val iverksettResultatService: IverksettResultatService,
    private val featureToggleService: FeatureToggleService,
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data
        val tilkjentYtelse = iverksettResultatService.hentTilkjentYtelse(behandlingId)

        if (tilkjentYtelse.harIngenUtbetaling()) {
            logger.info("Iverksetting har ikke utbetalingsoppdrag. Sjekker ikke status fra OS/UR")
            return
        }

        iverksettingService.sjekkStatusPåIverksettOgOppdaterTilstand(
            stønadstype = iverksett.fagsak.stønadstype,
            personIdent = iverksett.søker.personIdent,
            behandlingId = behandlingId,
        )
    }

    override fun onCompletion(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data

        if (!featureToggleService.isEnabled(FeatureToggleConfig.SKAL_SENDE_BREV)) {
            logger.warn(
                "Sender ikke ut brev fordi funksjonsbryteren for brevutsending er skrudd AV",
            )
        } else if (iverksett.skalIkkeSendeBrev()) {
            logger.info(
                "Journalfør ikke vedtaksbrev for behandling=$behandlingId fordi årsak=${iverksett.behandling.behandlingÅrsak}",
            )
        } else {
            taskService.save(task.opprettNesteTask())
        }
    }

    companion object {

        const val TYPE = "sjekkStatusPåOppdrag"
    }

    fun TilkjentYtelse?.harIngenUtbetaling(): Boolean {
        return this?.utbetalingsoppdrag?.utbetalingsperiode.isNullOrEmpty()
    }
}
