package no.nav.dagpenger.iverksett.konsumenter.økonomi

import no.nav.dagpenger.iverksett.api.IverksettingService
import no.nav.dagpenger.iverksett.api.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.IverksettResultat
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.behandlingId
import no.nav.dagpenger.iverksett.api.domene.erKonsistentMed
import no.nav.dagpenger.iverksett.api.domene.lagAndelData
import no.nav.dagpenger.iverksett.api.domene.personIdent
import no.nav.dagpenger.iverksett.api.domene.sakId
import no.nav.dagpenger.iverksett.api.domene.tilAndelData
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.konsumenter.opprettNesteTask
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.Utbetalingsgenerator
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.Behandlingsinformasjon
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.StønadTypeOgFerietillegg
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

        // lagOgSendUtbetalingsoppdragOgOppdaterTilkjentYtelse(iverksett, forrigeIverksettResultat, behandlingId)
        nyLagOgSendUtbetalingsoppdragOgOppdaterTilkjentYtelse(iverksett, forrigeIverksettResultat, behandlingId)
    }

    private fun nyLagOgSendUtbetalingsoppdragOgOppdaterTilkjentYtelse(
        iverksett: IverksettDagpenger,
        forrigeIverksettResultat: IverksettResultat?,
        behandlingId: UUID,
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
        val sisteAndelPerKjede = forrigeIverksettResultat?.tilkjentYtelseForUtbetaling?.sisteAndelPerKjede
            ?.mapValues { it.value.tilAndelData() }
            ?: emptyMap()

        val beregnetUtbetalingsoppdrag = Utbetalingsgenerator.lagUtbetalingsoppdrag(
            behandlingsinformasjon = behandlingsinformasjon,
            nyeAndeler = nyeAndeler,
            forrigeAndeler = forrigeAndeler,
            sisteAndelPerKjede = sisteAndelPerKjede,
        )

        iverksett.vedtak.tilkjentYtelse?.let {
            val nyeAndelerMedPeriodeId = it.andelerTilkjentYtelse.map { andel ->
                val andelData = andel.tilAndelData()
                val andelDataMedPeriodeId = beregnetUtbetalingsoppdrag.andeler.find { a -> andelData.id == a.id }
                    ?: throw IllegalStateException("Fant ikke andel med id ${andelData.id}")

                andel.copy(
                    periodeId = andelDataMedPeriodeId.periodeId,
                    forrigePeriodeId = andelDataMedPeriodeId.forrigePeriodeId,
                )
            }
            val nyTilkjentYtelse = it.copy(
                andelerTilkjentYtelse = nyeAndelerMedPeriodeId,
                utbetalingsoppdrag = beregnetUtbetalingsoppdrag.utbetalingsoppdrag,
            )
            val nyTilkjentYtelseMedSisteAndelIKjede = lagTilkjentYtelseMedSisteAndelPerKjede(nyTilkjentYtelse)

            iverksettResultatService.oppdaterTilkjentYtelseForUtbetaling(
                behandlingId = behandlingId,
                tilkjentYtelseForUtbetaling = nyTilkjentYtelseMedSisteAndelIKjede,
            )

            nyTilkjentYtelseMedSisteAndelIKjede.utbetalingsoppdrag?.let { utbetalingsoppdrag ->
                if (utbetalingsoppdrag.utbetalingsperiode.isNotEmpty()) {
                    oppdragClient.iverksettOppdrag(utbetalingsoppdrag)
                } else {
                    log.warn("Iverksetter ikke noe mot oppdrag. Ingen utbetalingsperioder i utbetalingsoppdraget. behandlingId=$behandlingId")
                }
            }
        } ?: log.warn("Iverksetter ikke noe mot oppdrag. Ikke utbetalingsoppdrag. behandlingId=$behandlingId")
    }

    private fun lagTilkjentYtelseMedSisteAndelPerKjede(tilkjentYtelse: TilkjentYtelse): TilkjentYtelse {
        val beregnetSisteAndePerKjede = tilkjentYtelse.andelerTilkjentYtelse.groupBy {
            StønadTypeOgFerietillegg(it.stønadstype, it.ferietillegg)
        }.mapValues {
            it.value.maxBy { it.periodeId!! }
        }

        val forrigeSisteAndelPerKjede = tilkjentYtelse.sisteAndelPerKjede

        val nySisteAndelerPerKjede: Map<StønadTypeOgFerietillegg, AndelTilkjentYtelse> =
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
        nySisteAndePerKjede: Map<StønadTypeOgFerietillegg, AndelTilkjentYtelse>,
        forrigeSisteAndelPerKjede: Map<StønadTypeOgFerietillegg, AndelTilkjentYtelse>,
    ) = (nySisteAndePerKjede.asSequence() + forrigeSisteAndelPerKjede.asSequence())
        .groupBy({ it.key }, { it.value })
        .mapValues {
            it.value.sortedWith(compareBy<AndelTilkjentYtelse> { it.periodeId }.thenBy { it.periode.tom })
                .last()
        }

    private fun lagOgSendUtbetalingsoppdragOgOppdaterTilkjentYtelse(
        iverksett: IverksettDagpenger,
        forrigeIverksettResultat: IverksettResultat?,
        behandlingId: UUID,
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
