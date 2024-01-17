package no.nav.dagpenger.iverksett.utbetaling.api

import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingsresultatService
import no.nav.dagpenger.iverksett.felles.http.advice.ApiFeil
import no.nav.dagpenger.iverksett.utbetaling.domene.*
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.kontrakter.felles.somUUID
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class IverksettingValidatorService(
        private val iverksettingsresultatService: IverksettingsresultatService,
        private val iverksettingService: IverksettingService,
) {

    fun valider(iverksetting: Iverksetting) {
        /* Med DB-oppslag */
        validerAtBehandlingIkkeAlleredeErMottatt(iverksetting)
        validerAtIverksettingErForSammeSakOgPersonSomForrige(iverksetting)
        validerAtForrigeBehandlingErFerdigIverksattMotOppdrag(iverksetting)
    }

    internal fun validerAtIverksettingErForSammeSakOgPersonSomForrige(iverksetting: Iverksetting) {
        val forrigeIverksett = try {
            iverksettingService.hentForrigeIverksett(iverksetting)
        } catch (e: IllegalStateException) {
            throw ApiFeil(e.message ?: "Fant ikke forrige iverksetting", HttpStatus.CONFLICT)
        }

        val forrigeSakId = forrigeIverksett?.sakId
        if (forrigeSakId != null && forrigeSakId != iverksetting.sakId) {
            throw ApiFeil(
                "Forrige behandling er knyttet til en annen sak enn denne iverksettingen gjelder",
                HttpStatus.BAD_REQUEST,
            )
        }

        val forrigePersonident = forrigeIverksett?.personident
        if (forrigePersonident != null && forrigePersonident != iverksetting.personident) {
            throw ApiFeil(
                "Forrige behandling er knyttet til en annen person enn denne iverksettingen gjelder",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun validerAtForrigeBehandlingErFerdigIverksattMotOppdrag(iverksetting: Iverksetting?) {
        iverksetting?.behandling?.forrigeBehandlingId?.apply {
            val forrigeResultat = iverksettingsresultatService.hentIverksettResultat(this.somUUID)

            val forrigeErUtenUtbetalingsperioder =
                forrigeResultat?.tilkjentYtelseForUtbetaling?.utbetalingsoppdrag?.utbetalingsperiode?.isEmpty() ?: true
            val forrigeErKvittertOk =
                forrigeResultat?.oppdragResultat?.oppdragStatus == OppdragStatus.KVITTERT_OK

            val forrigeErOkMotOppdrag = forrigeErUtenUtbetalingsperioder || forrigeErKvittertOk
            if (!forrigeErOkMotOppdrag) {
                throw ApiFeil("Forrige iverksetting er ikke ferdig håndtert mhp oppdrag", HttpStatus.CONFLICT)
            }
        }
    }

    internal fun validerAtBehandlingIkkeAlleredeErMottatt(iverksetting: Iverksetting) {
        if (iverksettingService.hentIverksetting(iverksetting.behandling.behandlingId.somUUID) != null) {
            throw ApiFeil(
                "Behandling med id ${iverksetting.behandling.behandlingId} er allerede mottattt",
                HttpStatus.CONFLICT,
            )
        }
    }
}
