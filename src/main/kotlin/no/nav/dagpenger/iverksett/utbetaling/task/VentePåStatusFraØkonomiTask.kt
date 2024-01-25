package no.nav.dagpenger.iverksett.utbetaling.task

import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingRepository
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingsresultatService
import no.nav.dagpenger.kontrakter.felles.GeneriskId
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.dagpenger.kontrakter.felles.somString
import no.nav.dagpenger.kontrakter.felles.somUUID
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
    beskrivelse = "Sjekker status på utbetalingsoppdraget i dp-oppdrag.",
)
class VentePåStatusFraØkonomiTask(
    private val iverksettingRepository: IverksettingRepository,
    private val iverksettingService: IverksettingService,
    private val iverksettingsresultatService: IverksettingsresultatService,
) : AsyncTaskStep {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        val behandlingId = objectMapper.readValue(task.payload, GeneriskId::class.java)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId.somUUID).data
        val tilkjentYtelse = iverksettingsresultatService.hentTilkjentYtelse(behandlingId.somUUID)

        if (tilkjentYtelse.harIngenUtbetaling()) {
            logger.info(
                "Iverksetting med behandlingsid ${behandlingId.somString} har ikke utbetalingsoppdrag. Sjekker ikke status fra OS/UR",
            )
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
