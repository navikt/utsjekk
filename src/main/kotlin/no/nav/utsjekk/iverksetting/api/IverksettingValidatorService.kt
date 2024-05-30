package no.nav.utsjekk.iverksetting.api

import no.nav.utsjekk.felles.http.advice.ApiFeil
import no.nav.utsjekk.iverksetting.domene.Iverksetting
import no.nav.utsjekk.iverksetting.domene.behandlingId
import no.nav.utsjekk.iverksetting.domene.sakId
import no.nav.utsjekk.iverksetting.tilstand.IverksettingService
import no.nav.utsjekk.iverksetting.tilstand.IverksettingsresultatService
import no.nav.utsjekk.kontrakter.oppdrag.OppdragStatus
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class IverksettingValidatorService(
    private val iverksettingsresultatService: IverksettingsresultatService,
    private val iverksettingService: IverksettingService,
) {
    fun valider(iverksetting: Iverksetting) {
        validerAtIverksettingIkkeAlleredeErMottatt(iverksetting)
        validerAtIverksettingGjelderSammeSakSomForrigeIverksetting(iverksetting)
        validerAtForrigeIverksettingErLikSisteMottatteIverksetting(iverksetting)
        validerAtForrigeIverksettingErFerdigIverksattMotOppdrag(iverksetting)
    }

    internal fun validerAtIverksettingGjelderSammeSakSomForrigeIverksetting(iverksetting: Iverksetting) {
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

    internal fun validerAtForrigeIverksettingErLikSisteMottatteIverksetting(iverksetting: Iverksetting) {
        val sisteMottatteIverksetting = iverksettingService.hentSisteMottatteIverksetting(iverksetting)
        sisteMottatteIverksetting?.let {
            if (it.behandlingId != iverksetting.behandling.forrigeBehandlingId ||
                it.behandling.iverksettingId != iverksetting.behandling.forrigeIverksettingId
            ) {
                throw ApiFeil(
                    "Forrige iverksetting stemmer ikke med siste mottatte iverksetting på saken. BehandlingId/IverksettingId forrige" +
                        " iverksetting: ${iverksetting.behandling.forrigeBehandlingId}/${iverksetting.behandling.forrigeIverksettingId}," +
                        " behandlingId/iverksettingId siste mottatte iverksetting: ${it.behandlingId}/${it.behandling.iverksettingId}",
                    HttpStatus.BAD_REQUEST,
                )
            }
        } ?: run {
            if (iverksetting.behandling.forrigeBehandlingId != null || iverksetting.behandling.forrigeIverksettingId != null) {
                throw ApiFeil(
                    "Det er ikke registrert noen tidligere iverksettinger på saken, men forrigeIverksetting er satt til " +
                        "behandling ${iverksetting.behandling.forrigeBehandlingId}/iverksetting " +
                        "${iverksetting.behandling.forrigeIverksettingId}",
                    HttpStatus.BAD_REQUEST,
                )
            }
        }
    }

    internal fun validerAtForrigeIverksettingErFerdigIverksattMotOppdrag(iverksetting: Iverksetting) {
        iverksetting.behandling.forrigeBehandlingId?.apply {
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
