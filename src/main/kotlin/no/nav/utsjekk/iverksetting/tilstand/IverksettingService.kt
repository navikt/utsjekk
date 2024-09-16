package no.nav.utsjekk.iverksetting.tilstand

import io.micrometer.core.instrument.Metrics
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.error.TaskExceptionUtenStackTrace
import no.nav.familie.prosessering.internal.TaskService
import no.nav.utsjekk.felles.hovedflyt
import no.nav.utsjekk.felles.oppdrag.OppdragClient
import no.nav.utsjekk.iverksetting.domene.Iverksetting
import no.nav.utsjekk.iverksetting.domene.IverksettingEntitet
import no.nav.utsjekk.iverksetting.domene.OppdragResultat
import no.nav.utsjekk.iverksetting.domene.behandlingId
import no.nav.utsjekk.iverksetting.domene.sakId
import no.nav.utsjekk.iverksetting.featuretoggle.FeatureToggleService
import no.nav.utsjekk.iverksetting.featuretoggle.IverksettingErSkruddAvException
import no.nav.utsjekk.iverksetting.task.tilTaskPayload
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.objectMapper
import no.nav.utsjekk.kontrakter.iverksett.IverksettStatus
import no.nav.utsjekk.kontrakter.oppdrag.OppdragIdDto
import no.nav.utsjekk.kontrakter.oppdrag.OppdragStatus
import no.nav.utsjekk.status.StatusEndretProdusent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Properties

@Service
class IverksettingService(
    private val taskService: TaskService,
    private val oppdragClient: OppdragClient,
    private val iverksettingRepository: IverksettingRepository,
    private val iverksettingsresultatService: IverksettingsresultatService,
    private val featureToggleService: FeatureToggleService,
    private val statusEndretProdusent: StatusEndretProdusent,
) {
    @Transactional
    fun startIverksetting(iverksetting: Iverksetting) {
        if (featureToggleService.iverksettingErSkruddAvForFagsystem(iverksetting.fagsak.fagsystem)) {
            throw IverksettingErSkruddAvException(iverksetting.fagsak.fagsystem)
        }

        iverksettingRepository.insert(
            IverksettingEntitet(
                behandlingId = iverksetting.behandling.behandlingId,
                data = iverksetting,
                mottattTidspunkt = LocalDateTime.now(),
            ),
        )

        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = iverksetting.fagsak.fagsystem,
            sakId = iverksetting.sakId,
            behandlingId = iverksetting.behandling.behandlingId,
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
        sakId: String,
        behandlingId: String,
        iverksettingId: String? = null,
    ): Iverksetting? {
        val iverksettingerForFagsystem =
            iverksettingRepository
                .findByFagsakAndBehandlingAndIverksetting(
                    sakId,
                    behandlingId,
                    iverksettingId,
                ).filter { it.data.fagsak.fagsystem == fagsystem }
        return when (iverksettingerForFagsystem.size) {
            0 -> null
            1 -> iverksettingerForFagsystem.first().data
            else -> throw IllegalStateException(
                "Fant flere iverksettinger for behandling $behandlingId, fagsystem $fagsystem og iverksetting $iverksettingId",
            )
        }
    }

    fun hentForrigeIverksetting(iverksetting: Iverksetting): Iverksetting? =
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

    fun hentSisteMottatteIverksetting(
        fagsystem: Fagsystem,
        sakId: String,
    ): Iverksetting? =
        iverksettingRepository
            .findByFagsakIdAndFagsystem(
                fagsakId = sakId,
                fagsystem = fagsystem,
            ).maxByOrNull { it.mottattTidspunkt }
            ?.data

    private fun førsteHovedflytTask() = hovedflyt().first().type

    fun publiserStatusmelding(iverksetting: Iverksetting) {
        utledStatus(
            fagsystem = iverksetting.fagsak.fagsystem,
            sakId = iverksetting.sakId,
            behandlingId = iverksetting.behandlingId,
            iverksettingId = iverksetting.behandling.iverksettingId,
        )?.also { status ->
            statusEndretProdusent.sendStatusEndretEvent(iverksetting, status)
        }
    }

    fun utledStatus(
        fagsystem: Fagsystem,
        sakId: String,
        behandlingId: String,
        iverksettingId: String?,
    ): IverksettStatus? {
        val resultat =
            iverksettingsresultatService.hentIverksettingsresultat(
                fagsystem = fagsystem,
                sakId = sakId,
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

    fun sjekkStatusPåIverksettOgOppdaterTilstand(iverksetting: Iverksetting) {
        val oppdragId =
            OppdragIdDto(
                fagsystem = iverksetting.fagsak.fagsystem,
                sakId = iverksetting.sakId,
                behandlingId = iverksetting.behandlingId,
                iverksettingId = iverksetting.behandling.iverksettingId,
            )

        val (status, feilmelding) = oppdragClient.hentStatus(oppdragId)

        if (!status.erFerdigstilt()) {
            throw TaskExceptionUtenStackTrace("Har ikke fått kvittering fra OS, status=$status")
        }

        if (status.erFeilkvittering()) {
            logger.error("Fikk feilkvittering $status fra OS for iverksetting $iverksetting")
            secureLogger.error("Fikk feilkvittering fra OS for iverksetting $iverksetting: Status $status, feilmelding $feilmelding")
            feilKvitteringCounter.increment()
        }

        iverksettingsresultatService.oppdaterOppdragResultat(
            fagsystem = iverksetting.fagsak.fagsystem,
            sakId = iverksetting.sakId,
            behandlingId = iverksetting.behandlingId,
            iverksettingId = iverksetting.behandling.iverksettingId,
            oppdragResultat = OppdragResultat(oppdragStatus = status),
        )
        publiserStatusmelding(iverksetting)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")
        private val feilKvitteringCounter = Metrics.counter("iverksetting.feilkvittering")
    }
}

fun Task.erAktiv() =
    this.status != Status.AVVIKSHÅNDTERT &&
        this.status != Status.MANUELL_OPPFØLGING &&
        this.status != Status.FERDIG

private fun OppdragStatus.erFerdigstilt(): Boolean =
    this == OppdragStatus.KVITTERT_OK ||
        this == OppdragStatus.KVITTERT_TEKNISK_FEIL ||
        this == OppdragStatus.KVITTERT_FUNKSJONELL_FEIL ||
        this == OppdragStatus.KVITTERT_MED_MANGLER

private fun OppdragStatus.erFeilkvittering(): Boolean =
    this == OppdragStatus.KVITTERT_FUNKSJONELL_FEIL || this == OppdragStatus.KVITTERT_TEKNISK_FEIL
