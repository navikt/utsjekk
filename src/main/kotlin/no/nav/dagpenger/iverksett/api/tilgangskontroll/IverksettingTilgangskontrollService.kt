package no.nav.dagpenger.iverksett.api.tilgangskontroll

import com.nimbusds.jwt.JWTParser
import no.nav.dagpenger.iverksett.api.IverksettingService
import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.dagpenger.iverksett.infrastruktur.configuration.FeatureToggleConfig
import no.nav.dagpenger.iverksett.infrastruktur.featuretoggle.FeatureToggleService
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class IverksettingTilgangskontrollService(
    private val iverksettingService: IverksettingService,
    private val featureToggleService: FeatureToggleService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun validerBeslutterkontekst() {
        if (!TokenContext.erBeslutter()) {
            throw ApiFeil("Varsel om rammevedtak kommer ikke fra beslutter", HttpStatus.FORBIDDEN)
        }
    }

    fun valider(iverksett: IverksettDto, bearerToken: String) {
        if (featureToggleService.isEnabled(FeatureToggleConfig.TILGANGSKONTROLL, false)) {
            validerAtRammevedtakSendesAvBeslutter(iverksett, bearerToken)
        }
        validerAtDetFinnesIverksattRammevedtak(iverksett)
    }

    internal fun validerAtRammevedtakSendesAvBeslutter(iverksett: IverksettDto, bearerToken: String) {
        if (iverksett.vedtak.vedtakstype == VedtakType.RAMMEVEDTAK) {
            val tokenGrupper = hentTokenGrupper(bearerToken)
            val beslutterGruppe = hentBeslutterGruppe()

            if (beslutterGruppe.isNullOrBlank() || !tokenGrupper.contains(beslutterGruppe)) {
                throw ApiFeil("Rammevedtak må sendes av en ansatt med beslutter-rolle", HttpStatus.FORBIDDEN)
            }
        }
    }

    internal fun validerAtDetFinnesIverksattRammevedtak(iverksett: IverksettDto) {
        // Utbetalingsvedtak skal avvises dersom stønadsmottaker ikke har iverksatt rammevedtak av beslutter
        // Vi kan bare sjekke at det finnes rammevedtak fordi alle rammevedtak må sendes av beslutter
        if (iverksett.vedtak.vedtakstype == VedtakType.UTBETALINGSVEDTAK) {
            try {
                iverksettingService.hentRammevedtak(iverksett)
            } catch (e: NoSuchElementException) {
                throw ApiFeil("Stønadsmottaker har ikke iverksatt rammevedtak", HttpStatus.CONFLICT)
            }
        }
    }

    private fun hentTokenGrupper(bearerToken: String): Array<String> {
        var grupper = emptyArray<String>()

        try {
            val tokenString = bearerToken.replace("Bearer ", "")
            val jwt = JWTParser.parse(tokenString)
            jwt.jwtClaimsSet.getStringArrayClaim("groups")?.let { grupper = it }
        } catch (e: Exception) {
            logger.error("Kunne ikke få grupper fra token. " + e.message)
        }

        return grupper
    }

    private fun hentBeslutterGruppe(): String? {
        return System.getProperty("BESLUTTER_GRUPPE", System.getenv("BESLUTTER_GRUPPE"))
    }
}
