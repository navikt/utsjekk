package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.api.domene.Brev
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.erKonsistentMed
import no.nav.dagpenger.iverksett.api.domene.personIdent
import no.nav.dagpenger.iverksett.api.domene.sakId
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.dagpenger.iverksett.infrastruktur.configuration.FeatureToggleConfig
import no.nav.dagpenger.iverksett.infrastruktur.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.konsumenter.tilbakekreving.validerTilbakekreving
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

        validerTilbakekreving(iverksett)
    }

    fun validerBrev(iverksettData: IverksettDagpenger, brev: Brev?) {
        when (brev) {
            null -> validerUtenBrev(iverksettData)
            else -> validerSkalHaBrev(iverksettData)
        }
    }

    fun validerUtenBrev(iverksettData: IverksettDagpenger) {
        if (featureToggleService.isEnabled(FeatureToggleConfig.SKAL_SENDE_BREV, false) &&
            !iverksettData.skalIkkeSendeBrev()
        ) {
            throw ApiFeil(
                "Kan ikke ha iverksetting uten brev når det ikke er en migrering, " +
                    "g-omregning eller korrigering uten brev ",
                HttpStatus.BAD_REQUEST,
            )
        }
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

    internal fun validerSkalHaBrev(iverksettData: IverksettDagpenger) {
        if (featureToggleService.isEnabled(FeatureToggleConfig.SKAL_SENDE_BREV, false) &&
            iverksettData.skalIkkeSendeBrev()
        ) {
            throw ApiFeil(
                "Kan ikke ha iverksetting med brev når det er migrering, g-omregning eller korrigering uten brev",
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

            val forrigeErUtenUtbetalingsoppdrag =
                forrigeResultat?.tilkjentYtelseForUtbetaling?.utbetalingsoppdrag == null
            val forrigeErKvittertOk =
                forrigeResultat?.oppdragResultat?.oppdragStatus == OppdragStatus.KVITTERT_OK

            val forrigeErOkMotOppdrag = forrigeErUtenUtbetalingsoppdrag || forrigeErKvittertOk
            if (!forrigeErOkMotOppdrag) {
                throw ApiFeil("Forrige iverksetting  er ikke ferdig håndtert mhp oppdrag", HttpStatus.CONFLICT)
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

    internal fun validerTilbakekreving(iverksett: IverksettDagpenger) {
        if (!iverksett.vedtak.tilbakekreving.validerTilbakekreving()) {
            throw ApiFeil("Tilbakekreving er ikke gyldig", HttpStatus.BAD_REQUEST)
        }
    }
}
