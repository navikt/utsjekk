package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.felles.hovedflyt
import no.nav.dagpenger.iverksett.felles.oppdrag.OppdragClient
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.IverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.featuretoggle.FeatureToggleConfig
import no.nav.dagpenger.iverksett.utbetaling.featuretoggle.FeatureToggleService
import no.nav.dagpenger.kontrakter.felles.GeneriskId
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.felles.somString
import no.nav.dagpenger.kontrakter.felles.somUUID
import no.nav.dagpenger.kontrakter.iverksett.IverksettStatus
import no.nav.dagpenger.kontrakter.oppdrag.OppdragId
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.error.TaskExceptionUtenStackTrace
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
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
                iverksetting.behandling.behandlingId.somUUID,
                iverksettMedRiktigStønadstype,
            ),
        )

        iverksettingsresultatService.opprettTomtResultat(iverksetting.behandling.behandlingId.somUUID)

        taskService.save(
            Task(
                type = førsteHovedflytTask(),
                payload = iverksetting.behandling.behandlingId.somUUID.toString(),
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
            hentIverksetting(it.somUUID) ?: throw IllegalStateException(
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

    fun erFørsteVedtakPåSak(sakId: GeneriskId): Boolean {
        val vedtakForSak = iverksettingRepository.findByFagsakId(sakId.somString)

        return vedtakForSak.isEmpty()
    }

    private fun utledStønadstype(iverksetting: Iverksetting): StønadType =
        iverksetting.vedtak.tilkjentYtelse.andelerTilkjentYtelse.firstOrNull()?.stønadsdata?.stønadstype
            ?: hentForrigeIverksett(iverksetting)?.vedtak?.tilkjentYtelse?.andelerTilkjentYtelse?.firstOrNull()?.stønadsdata?.stønadstype
            ?: StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR

}

fun Task.erAktiv() = this.status != Status.AVVIKSHÅNDTERT &&
    this.status != Status.MANUELL_OPPFØLGING &&
    this.status != Status.FERDIG
