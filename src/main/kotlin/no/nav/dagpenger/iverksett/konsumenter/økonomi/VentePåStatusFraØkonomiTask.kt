package no.nav.dagpenger.iverksett.konsumenter.økonomi

import java.util.UUID
import no.nav.dagpenger.iverksett.api.IverksettingRepository
import no.nav.dagpenger.iverksett.api.IverksettingService
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.tilstand.IverksettingsresultatService
import no.nav.dagpenger.iverksett.infrastruktur.repository.findByIdOrThrow
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

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
    private val iverksettingsresultatService: IverksettingsresultatService,
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data
        val tilkjentYtelse = iverksettingsresultatService.hentTilkjentYtelse(behandlingId)

        if (tilkjentYtelse.harIngenUtbetaling()) {
            logger.info("Iverksetting har ikke utbetalingsoppdrag. Sjekker ikke status fra OS/UR")
            return
        }

        iverksettingService.sjekkStatusPåIverksettOgOppdaterTilstand(
            stønadstype = iverksett.fagsak.stønadstype,
            personident = iverksett.søker.personident,
            behandlingId = behandlingId,
        )
    }

    companion object {
        const val TYPE = "sjekkStatusPåOppdrag"
    }

    fun TilkjentYtelse?.harIngenUtbetaling(): Boolean {
        return this?.utbetalingsoppdrag?.utbetalingsperiode.isNullOrEmpty()
    }
}
