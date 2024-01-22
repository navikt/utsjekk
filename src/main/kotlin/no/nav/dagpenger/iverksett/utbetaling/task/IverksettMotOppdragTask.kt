package no.nav.dagpenger.iverksett.utbetaling.task

import no.nav.dagpenger.iverksett.felles.oppdrag.OppdragClient
import no.nav.dagpenger.iverksett.felles.opprettNesteTask
import no.nav.dagpenger.iverksett.utbetaling.domene.*
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingsresultatService
import no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.Utbetalingsgenerator
import no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.domene.Behandlingsinformasjon
import no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.domene.BeregnetUtbetalingsoppdrag
import no.nav.dagpenger.kontrakter.felles.GeneriskId
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.dagpenger.kontrakter.felles.somString
import no.nav.dagpenger.kontrakter.felles.somUUID
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(
    taskStepType = IverksettMotOppdragTask.TYPE,
    beskrivelse = "Utfører iverksetting av utbetalning mot økonomi.",
)
class IverksettMotOppdragTask(
    private val iverksettingService: IverksettingService,
    private val oppdragClient: OppdragClient,
    private val taskService: TaskService,
    private val iverksettingsresultatService: IverksettingsresultatService,
) : AsyncTaskStep {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val behandlingId = objectMapper.readValue(task.payload, GeneriskId::class.java)
        val iverksett =
            iverksettingService.hentIverksetting(behandlingId.somUUID)
                ?: error("Fant ikke iverksetting for behandlingId ${behandlingId.somString}")

        val forrigeIverksettResultat =
            iverksett.behandling.forrigeBehandlingId?.let {
                iverksettingsresultatService.hentIverksettResultat(it.somUUID)
                    ?: error("Kunne ikke finne iverksettresultat for behandlingId=${it.somString}")
            }

        lagOgSendUtbetalingsoppdragOgOppdaterTilkjentYtelse(iverksett, forrigeIverksettResultat, behandlingId)
    }

    private fun lagOgSendUtbetalingsoppdragOgOppdaterTilkjentYtelse(
        iverksetting: Iverksetting,
        forrigeIverksettingsresultat: Iverksettingsresultat?,
        behandlingId: GeneriskId,
    ) {
        val behandlingsinformasjon =
            Behandlingsinformasjon(
                saksbehandlerId = iverksetting.vedtak.saksbehandlerId,
                fagsakId = iverksetting.sakId,
                behandlingId = iverksetting.behandlingId,
                personident = iverksetting.personident,
                brukersNavKontor = iverksetting.vedtak.brukersNavKontor,
                vedtaksdato = iverksetting.vedtak.vedtakstidspunkt.toLocalDate(),
            )

        val nyeAndeler = iverksetting.vedtak.tilkjentYtelse.lagAndelData()
        val forrigeAndeler = forrigeIverksettingsresultat?.tilkjentYtelseForUtbetaling.lagAndelData()
        val sisteAndelPerKjede =
            forrigeIverksettingsresultat?.tilkjentYtelseForUtbetaling?.sisteAndelPerKjede
                ?.mapValues { it.value.tilAndelData() }
                ?: emptyMap()

        val beregnetUtbetalingsoppdrag =
            Utbetalingsgenerator.lagUtbetalingsoppdrag(
                behandlingsinformasjon = behandlingsinformasjon,
                nyeAndeler = nyeAndeler,
                forrigeAndeler = forrigeAndeler,
                sisteAndelPerKjede = sisteAndelPerKjede,
            )

        if (beregnetUtbetalingsoppdrag.utbetalingsoppdrag.utbetalingsperiode.isNotEmpty()) {
            oppdaterTilkjentYtelseOgIverksettOppdrag(
                iverksetting.vedtak.tilkjentYtelse,
                beregnetUtbetalingsoppdrag,
                forrigeIverksettingsresultat,
                behandlingId,
            )
        } else {
            log.warn("Iverksetter ikke noe mot oppdrag. Ikke utbetalingsoppdrag. behandlingId=${behandlingId.somString}")
        }
    }

    private fun oppdaterTilkjentYtelseOgIverksettOppdrag(
        tilkjentYtelse: TilkjentYtelse,
        beregnetUtbetalingsoppdrag: BeregnetUtbetalingsoppdrag,
        forrigeIverksettingsresultat: Iverksettingsresultat?,
        behandlingId: GeneriskId,
    ) {
        val nyeAndelerMedPeriodeId =
            tilkjentYtelse.andelerTilkjentYtelse.map { andel ->
                val andelData = andel.tilAndelData()
                val andelDataMedPeriodeId =
                    beregnetUtbetalingsoppdrag.andeler.find { a -> andelData.id == a.id }
                        ?: throw IllegalStateException("Fant ikke andel med id ${andelData.id}")

                andel.copy(
                    periodeId = andelDataMedPeriodeId.periodeId,
                    forrigePeriodeId = andelDataMedPeriodeId.forrigePeriodeId,
                )
            }
        val nyTilkjentYtelse =
            tilkjentYtelse.copy(
                andelerTilkjentYtelse = nyeAndelerMedPeriodeId,
                utbetalingsoppdrag = beregnetUtbetalingsoppdrag.utbetalingsoppdrag,
            )
        val forrigeSisteAndelPerKjede =
            forrigeIverksettingsresultat?.tilkjentYtelseForUtbetaling?.sisteAndelPerKjede
                ?: emptyMap()
        val nyTilkjentYtelseMedSisteAndelIKjede =
            lagTilkjentYtelseMedSisteAndelPerKjede(nyTilkjentYtelse, forrigeSisteAndelPerKjede)

        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
            behandlingId = behandlingId.somUUID,
            tilkjentYtelseForUtbetaling = nyTilkjentYtelseMedSisteAndelIKjede,
        )

        nyTilkjentYtelseMedSisteAndelIKjede.utbetalingsoppdrag?.let { utbetalingsoppdrag ->
            if (utbetalingsoppdrag.utbetalingsperiode.isNotEmpty()) {
                oppdragClient.iverksettOppdrag(utbetalingsoppdrag)
            } else {
                log.warn("Iverksetter ikke noe mot oppdrag. Ingen utbetalingsperioder i utbetalingsoppdraget. behandlingId=$behandlingId")
            }
        }
    }

    private fun lagTilkjentYtelseMedSisteAndelPerKjede(
        tilkjentYtelse: TilkjentYtelse,
        forrigeSisteAndelPerKjede: Map<Stønadsdata, AndelTilkjentYtelse>,
    ): TilkjentYtelse {
        val beregnetSisteAndePerKjede =
            tilkjentYtelse.andelerTilkjentYtelse.groupBy {
                it.stønadsdata
            }.mapValues {
                it.value.maxBy { it.periodeId!! }
            }

        val nySisteAndelerPerKjede: Map<Stønadsdata, AndelTilkjentYtelse> =
            finnSisteAndelPerKjede(beregnetSisteAndePerKjede, forrigeSisteAndelPerKjede)

        return tilkjentYtelse.copy(
            sisteAndelPerKjede = nySisteAndelerPerKjede,
        )
    }

    /**
     * Finner riktig siste andel per kjede av andeler
     * Key er definert av stønadstype + ferietillegg
     * Funksjonen lager en map med kjedeId som key og en liste med de to andelene fra hver map
     * Deretter finner vi hvilke av de to vi skal bruke, Regelen er
     * 1. Bruk den med største periodeId
     * 2. Hvis periodeIdene er like, bruk den med størst til-og-med-dato
     */
    private fun finnSisteAndelPerKjede(
        nySisteAndePerKjede: Map<Stønadsdata, AndelTilkjentYtelse>,
        forrigeSisteAndelPerKjede: Map<Stønadsdata, AndelTilkjentYtelse>,
    ) = (nySisteAndePerKjede.asSequence() + forrigeSisteAndelPerKjede.asSequence())
        .groupBy({ it.key }, { it.value })
        .mapValues {
            it.value.sortedWith(
                compareByDescending<AndelTilkjentYtelse> { it.periodeId }.thenByDescending { it.periode.tom },
            ).first()
        }

    override fun onCompletion(task: Task) {
        taskService.save(task.opprettNesteTask())
    }

    companion object {
        const val TYPE = "utførIverksettingAvUtbetaling"
    }
}
