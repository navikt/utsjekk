package no.nav.dagpenger.iverksett.api.tilgangskontroll

import no.nav.dagpenger.iverksett.api.IverksettingService
import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.dagpenger.iverksett.infrastruktur.configuration.FeatureToggleConfig
import no.nav.dagpenger.iverksett.infrastruktur.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.infrastruktur.transformer.tilSakIdentifikator
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class IverksettingTilgangskontrollService(
    private val iverksettingService: IverksettingService,
    private val featureToggleService: FeatureToggleService,
    @Value("\${BESLUTTER_GRUPPE}") private val beslutterGruppe: String,
    @Value("\${APP_MED_SYSTEMTILGANG}") private val appMedSystemtilgang: String,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun valider(iverksett: IverksettDto) {
        if (featureToggleService.isEnabled(FeatureToggleConfig.TILGANGSKONTROLL, false)) {
            validerFørsteVedtakPåSakSendesAvBeslutter(iverksett)
            validerSystemTilgangErLov()
        }
    }

    private fun validerSystemTilgangErLov() {
        if(TokenContext.erSystemtoken() && TokenContext.hentKlientnavn()!=appMedSystemtilgang) {
            throw ApiFeil("Forsøker å gjøre systemkall fra ${TokenContext.hentKlientnavn()} uten å være godkjent app", HttpStatus.FORBIDDEN)
        }
    }

    private fun validerFørsteVedtakPåSakSendesAvBeslutter(iverksett: IverksettDto) {
        val sakId = iverksett.tilSakIdentifikator()
        if(iverksettingService.erFørsteVedtakPåSak(sakId) && !erBeslutter()) {
            throw ApiFeil("Første vedtak på en sak må sendes av en ansatt med beslutter-rolle", HttpStatus.FORBIDDEN)
        }
    }

    private fun erBeslutter(): Boolean {
        return TokenContext.hentGrupper().contains(beslutterGruppe)
    }
}
