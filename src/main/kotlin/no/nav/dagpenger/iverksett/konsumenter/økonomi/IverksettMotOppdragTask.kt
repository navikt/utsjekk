package no.nav.dagpenger.iverksett.konsumenter.økonomi

import no.nav.dagpenger.iverksett.api.IverksettingService
import no.nav.dagpenger.iverksett.api.domene.erKonsistentMed
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
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
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.IverksettResultat
import no.nav.dagpenger.iverksett.api.domene.behandlingId
import no.nav.dagpenger.iverksett.api.domene.lagAndelData
import no.nav.dagpenger.iverksett.api.domene.personIdent
import no.nav.dagpenger.iverksett.api.domene.sakId
import no.nav.dagpenger.iverksett.api.domene.tilAndelData
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.Utbetalingsgenerator
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.Behandlingsinformasjon

@Service
@TaskStepBeskrivelse(
    taskStepType = IverksettMotOppdragTask.TYPE,
    beskrivelse = "Utfører iverksetting av utbetalning mot økonomi.",
)
class IverksettMotOppdragTask(
    private val iverksettingService: IverksettingService,
    private val oppdragClient: OppdragClient,
    private val taskService: TaskService,
    private val iverksettResultatService: IverksettResultatService,
) : AsyncTaskStep {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val iverksett = iverksettingService.hentIverksetting(behandlingId)
            ?: error("Fant ikke iverksetting for behandlingId $behandlingId")

        val forrigeIverksettResultat = iverksett.behandling.forrigeBehandlingId?.let {
            iverksettResultatService.hentIverksettResultat(it)
                ?: error("Kunne ikke finne iverksettresultat for behandlingId=$it")
        }

        if (!forrigeIverksettResultat.erKonsistentMed(iverksett.forrigeIverksetting)) {
            error("Lagret forrige tilkjent ytelse stemmer ikke med mottatt forrige tilkjent ytelse")
        }

        lagOgSendUtbetalingsoppdragOgOppdaterTilkjentYtelse(iverksett, forrigeIverksettResultat, behandlingId)
    }

    private fun nyLagOgSendUtbetalingsoppdragOgOppdaterTilkjentYtelse(
        iverksett: IverksettDagpenger,
        forrigeIverksettResultat: IverksettResultat?,
        behandlingId: UUID
    ) {
        val behandlingsinformasjon = Behandlingsinformasjon(
            saksbehandlerId = iverksett.vedtak.saksbehandlerId,
            fagsakId = iverksett.sakId.toString(),
            behandlingId = iverksett.behandlingId.toString(),
            personIdent = iverksett.personIdent,
            vedtaksdato = iverksett.vedtak.vedtakstidspunkt.toLocalDate(),
            opphørFra = null,
        )

        val nyeAndeler = iverksett.vedtak.tilkjentYtelse.lagAndelData()
        val forrigeAndeler = forrigeIverksettResultat?.tilkjentYtelseForUtbetaling.lagAndelData()
        val sisteAndelPerKjede =
            iverksett.vedtak.tilkjentYtelse?.sisteAndelPerKjede?.mapValues { it.value.tilAndelData() } ?: emptyMap()

        val beregnetUtbetalingsoppdrag = Utbetalingsgenerator.lagUtbetalingsoppdrag(
            behandlingsinformasjon = behandlingsinformasjon,
            nyeAndeler = nyeAndeler,
            forrigeAndeler = forrigeAndeler,
            sisteAndelPerKjede = sisteAndelPerKjede
        )

        // TODO det gjenstår å finne nye sisteAndelPerKjede basert på resultatet, oppdatere tilkjentytelse og iverksette.
        // Vi tror sisteAndelPerKjede kan løses ved en dobbel sortering på periodeId først, så tom.

    }

    private fun lagOgSendUtbetalingsoppdragOgOppdaterTilkjentYtelse(
        iverksett: IverksettDagpenger,
        forrigeIverksettResultat: IverksettResultat?,
        behandlingId: UUID
    ) {
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
