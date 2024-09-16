package no.nav.utsjekk.simulering.api

import no.nav.utsjekk.felles.http.advice.ApiFeil
import no.nav.utsjekk.iverksetting.domene.behandlingId
import no.nav.utsjekk.iverksetting.tilstand.IverksettingService
import no.nav.utsjekk.iverksetting.tilstand.IverksettingsresultatService
import no.nav.utsjekk.kontrakter.oppdrag.OppdragStatus
import no.nav.utsjekk.simulering.domene.Simulering
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class SimuleringValidator(
    private val iverksettingsresultatService: IverksettingsresultatService,
    private val iverksettingService: IverksettingService,
) {
    fun valider(simulering: Simulering) {
        forrigeIverksettingSkalVæreFerdigstilt(simulering)
        forrigeIverksettingErLikSisteMottatteIverksetting(simulering)
    }

    internal fun forrigeIverksettingSkalVæreFerdigstilt(simulering: Simulering) {
        simulering.forrigeIverksetting?.apply {
            val forrigeResultat =
                iverksettingsresultatService.hentIverksettingsresultat(
                    fagsystem = simulering.behandlingsinformasjon.fagsystem,
                    sakId = simulering.behandlingsinformasjon.fagsakId,
                    behandlingId = this.behandlingId,
                    iverksettingId = this.iverksettingId,
                )
            val ferdigstilteStatuser = listOf(OppdragStatus.KVITTERT_OK, OppdragStatus.OK_UTEN_UTBETALING)
            if (!ferdigstilteStatuser.contains(forrigeResultat?.oppdragResultat?.oppdragStatus)) {
                throw ApiFeil("Forrige iverksetting er ikke ferdig iverksatt mot Oppdragssystemet", HttpStatus.CONFLICT)
            }
        }
    }

    internal fun forrigeIverksettingErLikSisteMottatteIverksetting(simulering: Simulering) {
        val sisteMottatteIverksetting =
            iverksettingService.hentSisteMottatteIverksetting(
                fagsystem = simulering.behandlingsinformasjon.fagsystem,
                sakId = simulering.behandlingsinformasjon.fagsakId,
            )

        if (sisteMottatteIverksetting != null) {
            if (sisteMottatteIverksetting.behandlingId != simulering.forrigeIverksetting?.behandlingId ||
                sisteMottatteIverksetting.behandling.iverksettingId != simulering.forrigeIverksetting.iverksettingId
            ) {
                throw ApiFeil(
                    "Forrige iverksetting stemmer ikke med siste mottatte iverksetting på saken. BehandlingId/IverksettingId forrige" +
                        " iverksetting: ${simulering.forrigeIverksetting?.behandlingId}/${simulering.forrigeIverksetting?.iverksettingId}," +
                        " behandlingId/iverksettingId siste mottatte iverksetting: ${sisteMottatteIverksetting.behandlingId}/${sisteMottatteIverksetting.behandling.iverksettingId}",
                    HttpStatus.BAD_REQUEST,
                )
            }
        } else {
            if (simulering.forrigeIverksetting != null) {
                throw ApiFeil(
                    "Det er ikke registrert noen tidligere iverksettinger på saken, men forrigeIverksetting er satt til " +
                        "behandling ${simulering.forrigeIverksetting.behandlingId}/iverksetting " +
                        "${simulering.forrigeIverksetting.iverksettingId}",
                    HttpStatus.BAD_REQUEST,
                )
            }
        }
    }
}
