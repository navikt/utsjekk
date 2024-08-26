package no.nav.utsjekk.iverksetting.task

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.utsjekk.felles.Profiler
import no.nav.utsjekk.felles.oppdrag.OppdragClient
import no.nav.utsjekk.felles.opprettNesteTask
import no.nav.utsjekk.iverksetting.domene.AndelTilkjentYtelse
import no.nav.utsjekk.iverksetting.domene.Iverksetting
import no.nav.utsjekk.iverksetting.domene.Iverksettingsresultat
import no.nav.utsjekk.iverksetting.domene.Kjedenøkkel
import no.nav.utsjekk.iverksetting.domene.OppdragResultat
import no.nav.utsjekk.iverksetting.domene.StønadsdataTilleggsstønader
import no.nav.utsjekk.iverksetting.domene.StønadsdataTiltakspenger
import no.nav.utsjekk.iverksetting.domene.TilkjentYtelse
import no.nav.utsjekk.iverksetting.domene.behandlingId
import no.nav.utsjekk.iverksetting.domene.lagAndelData
import no.nav.utsjekk.iverksetting.domene.personident
import no.nav.utsjekk.iverksetting.domene.sakId
import no.nav.utsjekk.iverksetting.domene.tilAndelData
import no.nav.utsjekk.iverksetting.tilstand.IverksettingService
import no.nav.utsjekk.iverksetting.tilstand.IverksettingsresultatService
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.Utbetalingsgenerator
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.domene.Behandlingsinformasjon
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.domene.BeregnetUtbetalingsoppdrag
import no.nav.utsjekk.kontrakter.felles.BrukersNavKontor
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.objectMapper
import no.nav.utsjekk.kontrakter.oppdrag.OppdragStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
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
    private val environment: Environment,
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
            if (iverksetting.fagsak.fagsystem == Fagsystem.TILLEGGSSTØNADER &&
                iverksetting.behandlingId == "499" &&
                iverksetting.behandling.iverksettingId == "3121bfe5-d232-4f19-92c2-f552fb984f19" &&
                environment.activeProfiles.contains(Profiler.DEV)
            ) {
                iverksettUtbetalingPåNytt(tilkjentYtelse)
            } else {
                iverksettUtbetaling(tilkjentYtelse)
            }
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
                brukersNavKontor =
                    iverksetting.vedtak.tilkjentYtelse.andelerTilkjentYtelse
                        .finnBrukersNavKontor(),
                vedtaksdato = iverksetting.vedtak.vedtakstidspunkt.toLocalDate(),
                iverksettingId = iverksetting.behandling.iverksettingId,
            )

        val nyeAndeler = iverksetting.vedtak.tilkjentYtelse.lagAndelData()
        val forrigeAndeler = forrigeIverksettingsresultat?.tilkjentYtelseForUtbetaling.lagAndelData()
        val sisteAndelPerKjede =
            forrigeIverksettingsresultat
                ?.tilkjentYtelseForUtbetaling
                ?.sisteAndelPerKjede
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

    private fun iverksettUtbetalingPåNytt(tilkjentYtelse: TilkjentYtelse) {
        tilkjentYtelse.utbetalingsoppdrag?.let { utbetalingsoppdrag ->
            if (utbetalingsoppdrag.utbetalingsperiode.isNotEmpty()) {
                oppdragClient.iverksettOppdragPåNytt(utbetalingsoppdrag)
            } else {
                log.warn("Iverksetter ikke noe mot oppdrag. Ingen utbetalingsperioder i utbetalingsoppdraget.")
            }
        }
    }

    private fun lagTilkjentYtelseMedSisteAndelPerKjede(
        tilkjentYtelse: TilkjentYtelse,
        forrigeSisteAndelPerKjede: Map<Kjedenøkkel, AndelTilkjentYtelse>,
    ): TilkjentYtelse {
        val beregnetSisteAndePerKjede =
            tilkjentYtelse.andelerTilkjentYtelse
                .groupBy {
                    it.stønadsdata.tilKjedenøkkel()
                }.mapValues {
                    it.value.maxBy { andel -> andel.periodeId!! }
                }

        val nySisteAndelerPerKjede: Map<Kjedenøkkel, AndelTilkjentYtelse> =
            finnSisteAndelPerKjede(beregnetSisteAndePerKjede, forrigeSisteAndelPerKjede)

        return tilkjentYtelse.copy(
            sisteAndelPerKjede = nySisteAndelerPerKjede,
        )
    }

    /**
     * Finner riktig siste andel per kjede av andeler
     * Funksjonen lager en map med kjedenøkkel som key og en liste med de to andelene fra hver map
     * Deretter finner vi hvilke av de to vi skal bruke, Regelen er
     * 1. Bruk den med største periodeId
     * 2. Hvis periodeIdene er like, bruk den med størst til-og-med-dato
     */
    private fun finnSisteAndelPerKjede(
        nySisteAndePerKjede: Map<Kjedenøkkel, AndelTilkjentYtelse>,
        forrigeSisteAndelPerKjede: Map<Kjedenøkkel, AndelTilkjentYtelse>,
    ) = (nySisteAndePerKjede.asSequence() + forrigeSisteAndelPerKjede.asSequence())
        .groupBy({ it.key }, { it.value })
        .mapValues { entry ->
            entry.value
                .sortedWith(
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

private fun List<AndelTilkjentYtelse>.finnBrukersNavKontor(): BrukersNavKontor? =
    firstNotNullOfOrNull {
        when (it.stønadsdata) {
            is StønadsdataTilleggsstønader -> it.stønadsdata.brukersNavKontor
            is StønadsdataTiltakspenger -> it.stønadsdata.brukersNavKontor
            else -> null
        }
    }
