package no.nav.dagpenger.iverksett.api.tilgangskontroll

import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.springframework.http.HttpStatus

object TokenContext {

    fun hentSaksbehandlerIdent(): String {
        return Result.runCatching { SpringTokenValidationContextHolder().tokenValidationContext }
            .fold(
                onSuccess = {
                    it.getClaims("azuread")?.get("NAVident")?.toString()
                        ?: throw ApiFeil("Fant ikke NAV-ident på saksbehandler-token", HttpStatus.BAD_REQUEST)
                },
                onFailure = { throw ApiFeil("Fant ikke NAV-ident på saksbehandler-token", HttpStatus.BAD_REQUEST) },
            )
    }

    fun erBeslutter(): Boolean {
        val beslutterRolle = System.getenv("BESLUTTER_GRUPPE")
        return hentGrupper().contains(beslutterRolle)
    }

    private fun hentGrupper(): List<String> {
        return Result.runCatching { SpringTokenValidationContextHolder().tokenValidationContext }
            .fold(
                onSuccess = {
                    @Suppress("UNCHECKED_CAST")
                    it.getClaims("azuread")?.get("groups") as List<String>? ?: emptyList()
                },
                onFailure = { emptyList() },
            )
    }
}
