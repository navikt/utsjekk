package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.felles.hovedflyt
import no.nav.dagpenger.iverksett.felles.oppdrag.OppdragClient
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.IverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.domene.sakId
import no.nav.dagpenger.iverksett.utbetaling.featuretoggle.FeatureToggleConfig
import no.nav.dagpenger.iverksett.utbetaling.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.utbetaling.task.tilTaskPayload
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.GeneriskId
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomString
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.felles.objectMapper
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
import java.util.Properties
import java.util.UUID

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

        iverksettingRepository.insert(
            IverksettingEntitet(
                iverksetting.behandling.behandlingId.somUUID,
                iverksetting,
            ),
        )

        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = iverksetting.fagsak.fagsystem,
            sakId = iverksetting.sakId,
            behandlingId = iverksetting.behandling.behandlingId.somUUID,
            iverksettingId = iverksetting.behandling.iverksettingId,
        )

        taskService.save(
            Task(
                type = førsteHovedflytTask(),
                payload = objectMapper.writeValueAsString(iverksetting.tilTaskPayload()),
                properties =
                    Properties().apply {
                        this["personIdent"] = iverksetting.søker.personident
                        this["behandlingId"] = objectMapper.writeValueAsString(iverksetting.behandling.behandlingId)
                        this["saksbehandler"] = iverksetting.vedtak.saksbehandlerId
                        this["beslutter"] = iverksetting.vedtak.beslutterId
                    },
            ),
        )
    }

    fun hentIverksetting(
        fagsystem: Fagsystem,
        sakId: GeneriskId,
        behandlingId: GeneriskId,
        iverksettingId: String? = null,
    ): Iverksetting? {
        val iverksettingerForFagsystem =
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(sakId.somString, behandlingId.somUUID, iverksettingId)
                .filter { it.data.fagsak.fagsystem == fagsystem }
        return when (iverksettingerForFagsystem.size) {
            0 -> null
            1 -> iverksettingerForFagsystem.first().data
            else -> throw IllegalStateException(
                "Fant flere iverksettinger for behandling $behandlingId, fagsystem $fagsystem og iverksetting $iverksettingId",
            )
        }
    }

    fun hentForrigeIverksett(iverksetting: Iverksetting): Iverksetting? =
        iverksetting.behandling.forrigeBehandlingId?.let {
            hentIverksetting(
                fagsystem = iverksetting.fagsak.fagsystem,
                sakId = iverksetting.sakId,
                behandlingId = it,
                iverksettingId = iverksetting.behandling.forrigeIverksettingId,
            )
                ?: throw IllegalStateException(
                    "Fant ikke forrige iverksetting med behandlingId ${iverksetting.behandling.behandlingId} " +
                        "og forrige behandlingId $it for fagsystem ${iverksetting.fagsak.fagsystem}",
                )
        }

    private fun førsteHovedflytTask() = hovedflyt().first().type

    fun utledStatus(
        fagsystem: Fagsystem,
        sakId: String,
        behandlingId: UUID,
        iverksettingId: String? = null,
    ): IverksettStatus? {
        val resultat =
            iverksettingsresultatService.hentIverksettResultat(
                fagsystem = fagsystem,
                sakId = sakId.tilGeneriskId(),
                behandlingId = behandlingId,
                iverksettingId = iverksettingId,
            )

        if (resultat == null) {
            return null
        }

        if (resultat.oppdragResultat != null) {
            return when (resultat.oppdragResultat.oppdragStatus) {
                OppdragStatus.KVITTERT_OK -> IverksettStatus.OK
                OppdragStatus.LAGT_PÅ_KØ -> IverksettStatus.SENDT_TIL_OPPDRAG
                OppdragStatus.OK_UTEN_UTBETALING -> IverksettStatus.OK_UTEN_UTBETALING
                else -> IverksettStatus.FEILET_MOT_OPPDRAG
            }
        }

        return if (resultat.tilkjentYtelseForUtbetaling != null) {
            IverksettStatus.SENDT_TIL_OPPDRAG
        } else {
            IverksettStatus.IKKE_PÅBEGYNT
        }
    }

    fun sjekkStatusPåIverksettOgOppdaterTilstand(
        fagsystem: Fagsystem,
        sakId: GeneriskId,
        personident: String,
        behandlingId: GeneriskId,
        iverksettingId: String? = null,
    ) {
        val oppdragId =
            OppdragId(
                fagsystem = fagsystem,
                personIdent = personident,
                behandlingId = behandlingId,
            )

        val (status, melding) = oppdragClient.hentStatus(oppdragId)

        if (status != OppdragStatus.KVITTERT_OK) {
            throw TaskExceptionUtenStackTrace("Status fra oppdrag er ikke ok, status=$status melding=$melding")
        }

        iverksettingsresultatService.oppdaterOppdragResultat(
            fagsystem = fagsystem,
            sakId = sakId,
            behandlingId = behandlingId.somUUID,
            iverksettingId = iverksettingId,
            oppdragResultat = OppdragResultat(oppdragStatus = status),
        )
    }

    fun erFørsteVedtakPåSak(
        sakId: GeneriskId,
        fagsystem: Fagsystem,
    ): Boolean {
        val vedtakForSak =
            iverksettingRepository.findByFagsakId(sakId.somString)
                .filter { it.data.fagsak.fagsystem == fagsystem }
        return vedtakForSak.isEmpty()
    }
}

fun Task.erAktiv() =
    this.status != Status.AVVIKSHÅNDTERT &&
        this.status != Status.MANUELL_OPPFØLGING &&
        this.status != Status.FERDIG

private fun String.tilGeneriskId(): GeneriskId =
    Result.runCatching { UUID.fromString(this@tilGeneriskId) }.fold(
        onSuccess = { GeneriskIdSomUUID(it) },
        onFailure = { GeneriskIdSomString(this) },
    )
