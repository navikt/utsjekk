package no.nav.dagpenger.iverksett.utbetaling.task

import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingsresultatService
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
    private val iverksettingService: IverksettingService,
    private val iverksettingsresultatService: IverksettingsresultatService,
) : AsyncTaskStep {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        val payload = objectMapper.readValue(task.payload, TaskPayload::class.java)
        val iverksett =
            iverksettingService.hentIverksetting(
                fagsystem = payload.fagsystem,
                behandlingId = payload.behandlingId.somUUID,
                iverksettingId = payload.iverksettingId,
            ) ?: error(
                "Fant ikke iverksetting for fagsystem ${payload.fagsystem}, behandling ${payload.behandlingId.somString}" +
                    " og iverksettingId ${payload.iverksettingId}",
            )
        val tilkjentYtelse =
            iverksettingsresultatService.hentTilkjentYtelse(
                fagsystem = iverksett.fagsak.stønadstype.tilFagsystem(),
                behandlingId = payload.behandlingId.somUUID,
                iverksettingId = iverksett.behandling.iverksettingId,
            )

        if (tilkjentYtelse.harIngenUtbetaling()) {
            logger.info(
                "Iverksetting for fagsystem ${payload.fagsystem} med behandlingsid ${payload.behandlingId.somString} " +
                    "har ikke utbetalingsoppdrag. Sjekker ikke status fra OS/UR",
            )
            return
        }

        iverksettingService.sjekkStatusPåIverksettOgOppdaterTilstand(
            stønadstype = iverksett.fagsak.stønadstype,
            personident = iverksett.søker.personident,
            behandlingId = payload.behandlingId,
            iverksettingId = iverksett.behandling.iverksettingId,
        )
    }

    companion object {
        const val TYPE = "sjekkStatusPåOppdrag"
    }

    fun TilkjentYtelse?.harIngenUtbetaling(): Boolean {
        return this?.utbetalingsoppdrag?.utbetalingsperiode.isNullOrEmpty()
    }
}
