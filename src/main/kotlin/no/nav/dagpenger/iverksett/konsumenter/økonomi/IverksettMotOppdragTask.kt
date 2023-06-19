package no.nav.dagpenger.iverksett.konsumenter.økonomi

import no.nav.dagpenger.iverksett.api.IverksettingRepository
import no.nav.dagpenger.iverksett.api.domene.stemmerMed
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.repository.findByIdOrThrow
import no.nav.dagpenger.iverksett.konsumenter.opprettNesteTask
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = IverksettMotOppdragTask.TYPE,
    beskrivelse = "Utfører iverksetting av utbetalning mot økonomi.",
)
class IverksettMotOppdragTask(
    private val iverksettingRepository: IverksettingRepository,
    private val oppdragClient: OppdragClient,
    private val taskService: TaskService,
    private val iverksettResultatService: IverksettResultatService,
) : AsyncTaskStep {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingRepository.findByIdOrThrow(behandlingId).data
        val forrigeIverksettResultat = iverksett.behandling.forrigeBehandlingId?.let {
            iverksettResultatService.hentIverksettResultat(it)
                ?: error("Kunne ikke finne iverksettresultat for behandlingId=$it")
        }

        if (!forrigeIverksettResultat.stemmerMed(iverksett.forrigeIverksetting)) {
            error("Lagret forrige tilkjent ytelse stemmer ikke med mottatt forrige tilkjent ytelse")
        }

        iverksett.vedtak.tilkjentYtelse?.toMedMetadata(
            saksbehandlerId = iverksett.vedtak.saksbehandlerId,
            stønadType = iverksett.fagsak.stønadstype,
            sakId = iverksett.fagsak.fagsakId,
            personIdent = iverksett.søker.personIdent,
            behandlingId = iverksett.behandling.behandlingId,
            vedtaksdato = iverksett.vedtak.vedtakstidspunkt.toLocalDate(),
        )?.let { tilkjentYtelseMedMetaData ->
            lagTilkjentYtelseMedUtbetalingsoppdrag(
                tilkjentYtelseMedMetaData,
                forrigeIverksettResultat?.tilkjentYtelseForUtbetaling,
                iverksett.erGOmregning(),
            )
        }?.also { tilkjentYtelseMedUtbetalingsoppdrag ->
            iverksettResultatService.oppdaterTilkjentYtelseForUtbetaling(
                behandlingId = behandlingId,
                tilkjentYtelseForUtbetaling = tilkjentYtelseMedUtbetalingsoppdrag,
            )
            tilkjentYtelseMedUtbetalingsoppdrag.utbetalingsoppdrag?.let { utbetalingsoppdrag ->
                if (utbetalingsoppdrag.utbetalingsperiode.isNotEmpty()) {
                    oppdragClient.iverksettOppdrag(utbetalingsoppdrag)
                } else {
                    log.warn("Iverksetter ikke noe mot oppdrag. Ingen utbetalingsperioder i utbetalingsoppdraget. behandlingId=$behandlingId")
                }
            }
        } ?: log.warn("Iverksetter ikke noe mot oppdrag. Ikke utbetalingsoppdrag. behandlingId=$behandlingId")
    }

    override fun onCompletion(task: Task) {
        taskService.save(task.opprettNesteTask())
    }

    companion object {

        const val TYPE = "utførIverksettingAvUtbetaling"
    }
}
