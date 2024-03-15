package no.nav.dagpenger.iverksett.utbetaling.task

import no.nav.dagpenger.iverksett.felles.oppdrag.OppdragClient
import no.nav.dagpenger.iverksett.felles.opprettNesteTask
import no.nav.dagpenger.iverksett.utbetaling.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksettingsresultat
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.domene.Stønadsdata
import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.domene.behandlingId
import no.nav.dagpenger.iverksett.utbetaling.domene.lagAndelData
import no.nav.dagpenger.iverksett.utbetaling.domene.personident
import no.nav.dagpenger.iverksett.utbetaling.domene.sakId
import no.nav.dagpenger.iverksett.utbetaling.domene.tilAndelData
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingsresultatService
import no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.Utbetalingsgenerator
import no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.domene.Behandlingsinformasjon
import no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.domene.BeregnetUtbetalingsoppdrag
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
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
    beskrivelse = "Utfører iverksetting av utbetaling mot økonomi.",
)
class IverksettMotOppdragTask(
    private val iverksettingService: IverksettingService,
    private val oppdragClient: OppdragClient,
    private val taskService: TaskService,
    private val iverksettingsresultatService: IverksettingsresultatService,
) : AsyncTaskStep {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val payload = objectMapper.readValue(task.payload, TaskPayload::class.java)

        val iverksetting =
            iverksettingService.hentIverksetting(
                fagsystem = payload.fagsystem,
                sakId = payload.sakId,
                behandlingId = payload.behandlingId,
                iverksettingId = payload.iverksettingId,
            )
                ?: error(
                    "Fant ikke iverksetting for fagsystem ${payload.fagsystem}, behandling ${payload.behandlingId}" +
                        " og iverksettingId ${payload.iverksettingId}",
                )

        val forrigeIverksettResultat =
            iverksetting.behandling.forrigeBehandlingId?.let {
                iverksettingsresultatService.hentIverksettingsresultat(
                    fagsystem = iverksetting.fagsak.fagsystem,
                    sakId = iverksetting.sakId,
                    behandlingId = it,
                    iverksettingId = iverksetting.behandling.forrigeIverksettingId,
                )
                    ?: error("Kunne ikke finne iverksettresultat for iverksetting $iverksetting")
            }

        val beregnetUtbetalingsoppdrag = lagUtbetalingsoppdrag(iverksetting, forrigeIverksettResultat)

        val tilkjentYtelse =
            oppdaterTilkjentYtelse(
                tilkjentYtelse = iverksetting.vedtak.tilkjentYtelse,
                beregnetUtbetalingsoppdrag = beregnetUtbetalingsoppdrag,
                forrigeIverksettingsresultat = forrigeIverksettResultat,
                behandlingId = payload.behandlingId,
                fagsystem = iverksetting.fagsak.fagsystem,
                sakId = iverksetting.sakId,
                iverksettingId = iverksetting.behandling.iverksettingId,
            )

        if (beregnetUtbetalingsoppdrag.utbetalingsoppdrag.utbetalingsperiode.isNotEmpty()) {
            iverksettUtbetaling(tilkjentYtelse)
        } else {
            iverksettingsresultatService.oppdaterOppdragResultat(
                fagsystem = iverksetting.fagsak.fagsystem,
                sakId = iverksetting.sakId,
                behandlingId = iverksetting.behandlingId,
                oppdragResultat = OppdragResultat(OppdragStatus.OK_UTEN_UTBETALING),
                iverksettingId = iverksetting.behandling.iverksettingId,
            )
            log.warn(
                "Iverksetter ikke noe mot oppdrag. Ingen perioder i utbetalingsoppdraget for iverksetting $iverksetting",
            )
        }

        iverksettingService.publiserStatusmelding(iverksetting)
    }

    private fun lagUtbetalingsoppdrag(
        iverksetting: Iverksetting,
        forrigeIverksettingsresultat: Iverksettingsresultat?,
    ): BeregnetUtbetalingsoppdrag {
        val behandlingsinformasjon =
            Behandlingsinformasjon(
                saksbehandlerId = iverksetting.vedtak.saksbehandlerId,
                beslutterId = iverksetting.vedtak.beslutterId,
                fagsystem = iverksetting.fagsak.fagsystem,
                fagsakId = iverksetting.sakId,
                behandlingId = iverksetting.behandlingId,
                personident = iverksetting.personident,
                brukersNavKontor = iverksetting.vedtak.brukersNavKontor,
                vedtaksdato = iverksetting.vedtak.vedtakstidspunkt.toLocalDate(),
                iverksettingId = iverksetting.behandling.iverksettingId,
            )

        val nyeAndeler = iverksetting.vedtak.tilkjentYtelse.lagAndelData()
        val forrigeAndeler = forrigeIverksettingsresultat?.tilkjentYtelseForUtbetaling.lagAndelData()
        val sisteAndelPerKjede =
            forrigeIverksettingsresultat?.tilkjentYtelseForUtbetaling?.sisteAndelPerKjede
                ?.mapValues { it.value.tilAndelData() }
                ?: emptyMap()

        return Utbetalingsgenerator.lagUtbetalingsoppdrag(
            behandlingsinformasjon = behandlingsinformasjon,
            nyeAndeler = nyeAndeler,
            forrigeAndeler = forrigeAndeler,
            sisteAndelPerKjede = sisteAndelPerKjede,
        )
    }

    private fun oppdaterTilkjentYtelse(
        tilkjentYtelse: TilkjentYtelse,
        beregnetUtbetalingsoppdrag: BeregnetUtbetalingsoppdrag,
        forrigeIverksettingsresultat: Iverksettingsresultat?,
        behandlingId: String,
        fagsystem: Fagsystem,
        sakId: String,
        iverksettingId: String?,
    ): TilkjentYtelse {
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
            fagsystem = fagsystem,
            sakId = sakId,
            behandlingId = behandlingId,
            iverksettingId = iverksettingId,
            tilkjentYtelseForUtbetaling = nyTilkjentYtelseMedSisteAndelIKjede,
        )

        return nyTilkjentYtelseMedSisteAndelIKjede
    }

    private fun iverksettUtbetaling(tilkjentYtelse: TilkjentYtelse) {
        tilkjentYtelse.utbetalingsoppdrag?.let { utbetalingsoppdrag ->
            if (utbetalingsoppdrag.utbetalingsperiode.isNotEmpty()) {
                oppdragClient.iverksettOppdrag(utbetalingsoppdrag)
            } else {
                log.warn("Iverksetter ikke noe mot oppdrag. Ingen utbetalingsperioder i utbetalingsoppdraget.")
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
                it.value.maxBy { andel -> andel.periodeId!! }
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
        .mapValues { entry ->
            entry.value.sortedWith(
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
