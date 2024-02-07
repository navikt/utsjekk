package no.nav.dagpenger.iverksett.utbetaling.api

import no.nav.dagpenger.iverksett.felles.http.advice.ApiFeil
import no.nav.dagpenger.iverksett.utbetaling.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.utbetaling.domene.KonsumentConfig
import no.nav.dagpenger.iverksett.utbetaling.featuretoggle.FeatureToggleConfig
import no.nav.dagpenger.iverksett.utbetaling.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
@Profile("!local")
class IverksettingTilgangskontrollService(
    private val iverksettingService: IverksettingService,
    private val featureToggleService: FeatureToggleService,
    private val konsumentConfig: KonsumentConfig,
) : TilgangskontrollService {
    override fun valider(fagsakdetaljer: Fagsakdetaljer) {
        if (featureToggleService.isEnabled(FeatureToggleConfig.TILGANGSKONTROLL, false)) {
            validerFørsteVedtakPåSakSendesAvBeslutter(fagsakdetaljer)
            validerSystemTilgangErLov(fagsakdetaljer.fagsystem)
        }
    }

    private fun validerSystemTilgangErLov(fagsystem: Fagsystem) {
        val appMedSystemtilgang = konsumentConfig.configForFagsystem(fagsystem).klientapp
        if (TokenContext.erSystemtoken() && TokenContext.hentKlientnavn() != appMedSystemtilgang) {
            throw ApiFeil(
                "Forsøker å gjøre systemkall fra ${TokenContext.hentKlientnavn()} uten å være godkjent app",
                HttpStatus.FORBIDDEN,
            )
        }
    }

    private fun validerFørsteVedtakPåSakSendesAvBeslutter(fagsakdetaljer: Fagsakdetaljer) {
        if (iverksettingService.erFørsteVedtakPåSak(fagsakdetaljer.fagsakId, fagsakdetaljer.fagsystem) &&
            !erBeslutter(
                fagsakdetaljer.fagsystem,
            )
        ) {
            throw ApiFeil("Første vedtak på en sak må sendes av en ansatt med beslutter-rolle", HttpStatus.FORBIDDEN)
        }
    }

    private fun erBeslutter(fagsystem: Fagsystem): Boolean {
        val beslutterGruppe = konsumentConfig.configForFagsystem(fagsystem).grupper.beslutter
        return TokenContext.hentGrupper().contains(beslutterGruppe)
    }
}
