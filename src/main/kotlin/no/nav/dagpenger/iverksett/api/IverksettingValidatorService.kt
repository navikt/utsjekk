package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.erKonsistentMed
import no.nav.dagpenger.iverksett.api.domene.personIdent
import no.nav.dagpenger.iverksett.api.domene.sakId
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.dagpenger.iverksett.infrastruktur.featuretoggle.FeatureToggleService
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class IverksettingValidatorService(
    private val iverksettResultatService: IverksettResultatService,
    private val iverksettingService: IverksettingService,
    private val featureToggleService: FeatureToggleService,
) {

    fun valider(iverksett: IverksettDagpenger) {
        /* Med DB-oppslag */
        validerAtBehandlingIkkeAlleredeErMottatt(iverksett)
        validerKonsistensMellomVedtak(iverksett)
        validerAtIverksettingErForSammeSakOgPersonSomForrige(iverksett)
        validerAtForrigeBehandlingErFerdigIverksattMotOppdrag(iverksett)
    }

    internal fun validerKonsistensMellomVedtak(iverksett: IverksettDagpenger) {
        val forrigeIverksettResultat = iverksett.behandling.forrigeBehandlingId?.let {
            iverksettResultatService.hentIverksettResultat(it) ?: throw ApiFeil(
                "Forrige behandling med id $it er ikke mottatt for iverksetting",
                HttpStatus.BAD_REQUEST,
            )
        }

        if (!forrigeIverksettResultat.erKonsistentMed(iverksett.forrigeIverksetting)) {
            throw ApiFeil(
                "Mottatt og faktisk forrige iverksatting er ikke konsistente",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun validerAtIverksettingErForSammeSakOgPersonSomForrige(iverksett: IverksettDagpenger) {
        val forrigeIverksett = try {
            iverksettingService.hentForrigeIverksett(iverksett)
        } catch (e: IllegalStateException) {
            throw ApiFeil(e.message ?: "Fant ikke forrige iverksetting", HttpStatus.CONFLICT)
        }

        val forrigeSakId = forrigeIverksett?.sakId
        if (forrigeSakId != null && forrigeSakId != iverksett.sakId) {
            throw ApiFeil(
                "Forrige behandling er knyttet til en annen sak enn denne iverksettingen gjelder",
                HttpStatus.BAD_REQUEST,
            )
        }

        val forrigePersonident = forrigeIverksett?.personIdent
        if (forrigePersonident != null && forrigePersonident != iverksett.personIdent) {
            throw ApiFeil(
                "Forrige behandling er knyttet til en annen person enn denne iverksettingen gjelder",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun validerAtForrigeBehandlingErFerdigIverksattMotOppdrag(iverksett: IverksettDagpenger?) {
        iverksett?.behandling?.forrigeBehandlingId?.apply {
            val forrigeResultat = iverksettResultatService.hentIverksettResultat(this)

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

    internal fun validerAtBehandlingIkkeAlleredeErMottatt(iverksett: IverksettDagpenger) {
        if (iverksettingService.hentIverksetting(iverksett.behandling.behandlingId) != null) {
            throw ApiFeil(
                "Behandling med id ${iverksett.behandling.behandlingId} er allerede mottattt",
                HttpStatus.CONFLICT,
            )
        }
    }
}
