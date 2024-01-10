package no.nav.dagpenger.iverksett.api

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Properties
import java.util.UUID
import no.nav.dagpenger.iverksett.api.domene.IverksettingEntitet
import no.nav.dagpenger.iverksett.api.domene.Iverksetting
import no.nav.dagpenger.iverksett.api.domene.OppdragResultat
import no.nav.dagpenger.iverksett.api.tilstand.IverksettingsresultatService
import no.nav.dagpenger.iverksett.infrastruktur.configuration.FeatureToggleConfig
import no.nav.dagpenger.iverksett.infrastruktur.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.konsumenter.hovedflyt
import no.nav.dagpenger.iverksett.konsumenter.økonomi.OppdragClient
import no.nav.dagpenger.iverksett.konsumenter.økonomi.grensesnitt.GrensesnittavstemmingDto
import no.nav.dagpenger.iverksett.konsumenter.økonomi.grensesnitt.GrensesnittavstemmingTask
import no.nav.dagpenger.iverksett.konsumenter.økonomi.grensesnitt.tilTask
import no.nav.dagpenger.kontrakter.felles.SakIdentifikator
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.iverksett.IverksettStatus
import no.nav.dagpenger.kontrakter.oppdrag.OppdragId
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.error.TaskExceptionUtenStackTrace
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class IverksettingService(
    val taskService: TaskService,
    val oppdragClient: OppdragClient,
    val iverksettingRepository: IverksettingRepository,
    val iverksettingsresultatService: IverksettingsresultatService,
    val featureToggleService: FeatureToggleService,
) {

    @Transactional
    fun startIverksetting(iverksetting: Iverksetting) {
        if (featureToggleService.isEnabled(FeatureToggleConfig.STOPP_IVERKSETTING)) {
            error("Kan ikke iverksette akkurat nå")
        }

        val iverksettMedRiktigStønadstype = iverksetting.copy(
            fagsak = iverksetting.fagsak.copy(stønadstype = utledStønadstype(iverksetting))
        )
        iverksettingRepository.insert(
            IverksettingEntitet(
                iverksetting.behandling.behandlingId,
                iverksettMedRiktigStønadstype,
            ),
        )

        iverksettingsresultatService.opprettTomtResultat(iverksetting.behandling.behandlingId)

        taskService.save(
            Task(
                type = førsteHovedflytTask(),
                payload = iverksetting.behandling.behandlingId.toString(),
                properties = Properties().apply {
                    this["personIdent"] = iverksetting.søker.personident
                    this["behandlingId"] = iverksetting.behandling.behandlingId.toString()
                    this["saksbehandler"] = iverksetting.vedtak.saksbehandlerId
                    this["beslutter"] = iverksetting.vedtak.beslutterId
                },
            ),
        )
    }

    fun hentIverksetting(behandlingId: UUID): Iverksetting? {
        return iverksettingRepository.findById(behandlingId).getOrNull()?.data
    }

    fun hentForrigeIverksett(iverksetting: Iverksetting): Iverksetting? =
        iverksetting.behandling.forrigeBehandlingId?.let {
            hentIverksetting(it) ?: throw IllegalStateException(
                "Fant ikke forrige iverksetting med behandlingId ${iverksetting.behandling.behandlingId} " +
                    "og forrige behandlingId $it",
            )
        }

    private fun førsteHovedflytTask() = hovedflyt().first().type

    fun utledStatus(behandlingId: UUID): IverksettStatus? {
        val iverksettResultat = iverksettingsresultatService.hentIverksettResultat(behandlingId)
        return iverksettResultat?.let {
            it.oppdragResultat?.let { oppdragResultat ->
                return when (oppdragResultat.oppdragStatus) {
                    OppdragStatus.KVITTERT_OK -> IverksettStatus.OK
                    OppdragStatus.LAGT_PÅ_KØ -> IverksettStatus.SENDT_TIL_OPPDRAG
                    else -> IverksettStatus.FEILET_MOT_OPPDRAG
                }
            }
            it.tilkjentYtelseForUtbetaling?.let {ty ->
                if (ty.utbetalingsoppdrag?.utbetalingsperiode?.isEmpty() == true) {
                    return IverksettStatus.OK
                }
                return IverksettStatus.SENDT_TIL_OPPDRAG
            }
            return IverksettStatus.IKKE_PAABEGYNT
        }
    }

    fun sjekkStatusPåIverksettOgOppdaterTilstand(
        stønadstype: StønadType,
        personident: String,
        behandlingId: UUID,
    ) {
        val oppdragId = OppdragId(
            fagsystem = stønadstype.tilFagsystem(),
            personIdent = personident,
            behandlingsId = behandlingId,
        )

        val (status, melding) = oppdragClient.hentStatus(oppdragId)

        if (status != OppdragStatus.KVITTERT_OK) {
            throw TaskExceptionUtenStackTrace("Status fra oppdrag er ikke ok, status=$status melding=$melding")
        }

        iverksettingsresultatService.oppdaterOppdragResultat(
            behandlingId = behandlingId,
            OppdragResultat(oppdragStatus = status),
        )
    }

    fun lagreGrensesnittavstemmingTask(): Boolean {
        if (taskService.findAll().any { it.type == GrensesnittavstemmingTask.TYPE && it.erAktiv() }) {
            logger.info("Plukkbar task for grensesnittavstemming allerede opprettet - lager ikke ny task")
            return false
        }

        val grensesnittavstemmingDto = GrensesnittavstemmingDto(
            stønadstype = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
            fraDato = LocalDate.now().minusDays(5),
            triggerTid = LocalDateTime.now(),
        )
        taskService.save(grensesnittavstemmingDto.tilTask())
        return true
    }

    fun erFørsteVedtakPåSak(sakId: SakIdentifikator): Boolean {
        val vedtakForSak = iverksettingRepository
            .findBySakIdentifikator(sakId)

        return vedtakForSak.isEmpty()
    }

    private fun utledStønadstype(iverksetting: Iverksetting): StønadType =
        iverksetting.vedtak.tilkjentYtelse.andelerTilkjentYtelse.firstOrNull()?.stønadsdata?.stønadstype
            ?: hentForrigeIverksett(iverksetting)?.vedtak?.tilkjentYtelse?.andelerTilkjentYtelse?.firstOrNull()?.stønadsdata?.stønadstype
            ?: StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}

fun Task.erAktiv() = this.status != Status.AVVIKSHÅNDTERT &&
    this.status != Status.MANUELL_OPPFØLGING &&
    this.status != Status.FERDIG
