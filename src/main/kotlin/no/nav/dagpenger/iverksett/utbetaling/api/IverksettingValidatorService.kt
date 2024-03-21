package no.nav.dagpenger.iverksett.utbetaling.api

import no.nav.dagpenger.iverksett.felles.http.advice.ApiFeil
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.sakId
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingsresultatService
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class IverksettingValidatorService(
    private val iverksettingsresultatService: IverksettingsresultatService,
    private val iverksettingService: IverksettingService,
) {
    fun valider(iverksetting: Iverksetting) {
        validerAtIverksettingIkkeAlleredeErMottatt(iverksetting)
        validerAtIverksettingErForSammeSakSomForrige(iverksetting)
        validerAtForrigeBehandlingErFerdigIverksattMotOppdrag(iverksetting)
    }

    internal fun validerAtIverksettingErForSammeSakSomForrige(iverksetting: Iverksetting) {
        val forrigeIverksett =
            try {
                iverksettingService.hentForrigeIverksetting(iverksetting)
            } catch (e: IllegalStateException) {
                throw ApiFeil(e.message ?: "Fant ikke forrige iverksetting", HttpStatus.CONFLICT)
            }

        val forrigeSakId = forrigeIverksett?.sakId
        if (forrigeSakId != null && forrigeSakId != iverksetting.sakId) {
            throw ApiFeil(
                "Forrige iverksetting er knyttet til en annen sak enn denne iverksettingen gjelder. " +
                    "SakId forrige iverksetting: $forrigeSakId, sakId nåværende iverksetting: ${iverksetting.sakId}",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun validerAtForrigeBehandlingErFerdigIverksattMotOppdrag(iverksetting: Iverksetting?) {
        iverksetting?.behandling?.forrigeBehandlingId?.apply {
            val forrigeResultat =
                iverksettingsresultatService.hentIverksettingsresultat(
                    fagsystem = iverksetting.fagsak.fagsystem,
                    sakId = iverksetting.sakId,
                    behandlingId = this,
                    iverksettingId = iverksetting.behandling.forrigeIverksettingId,
                )

            val forrigeErUtenUtbetalingsperioder =
                forrigeResultat?.tilkjentYtelseForUtbetaling?.utbetalingsoppdrag?.utbetalingsperiode?.isEmpty() ?: true
            val forrigeErKvittertOk =
                forrigeResultat?.oppdragResultat?.oppdragStatus == OppdragStatus.KVITTERT_OK

            val forrigeErOkMotOppdrag = forrigeErUtenUtbetalingsperioder || forrigeErKvittertOk
            if (!forrigeErOkMotOppdrag) {
                throw ApiFeil("Forrige iverksetting er ikke ferdig iverksatt mot Oppdragssystemet", HttpStatus.CONFLICT)
            }
        }
    }

    internal fun validerAtIverksettingIkkeAlleredeErMottatt(iverksetting: Iverksetting) {
        val iverksettinger =
            iverksettingService.hentIverksetting(
                fagsystem = iverksetting.fagsak.fagsystem,
                sakId = iverksetting.fagsak.fagsakId,
                behandlingId = iverksetting.behandling.behandlingId,
                iverksettingId = iverksetting.behandling.iverksettingId,
            )
        if (iverksettinger != null) {
            throw ApiFeil(
                "Iverksetting for $iverksetting er allerede mottatt",
                HttpStatus.CONFLICT,
            )
        }
    }
}
