package no.nav.dagpenger.iverksett.utbetaling.api

import no.nav.dagpenger.iverksett.felles.http.advice.ApiFeil
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.springframework.http.HttpStatus

object TokenContext {
    fun hentGrupper(): List<String> {
        return Result.runCatching { SpringTokenValidationContextHolder().tokenValidationContext }
            .fold(
                onSuccess = {
                    @Suppress("UNCHECKED_CAST")
                    it.getClaims("azuread")?.get("groups") as List<String>? ?: emptyList()
                },
                onFailure = { emptyList() },
            )
    }

    fun erSystemtoken(): Boolean {
        return Result.runCatching { SpringTokenValidationContextHolder().tokenValidationContext }
            .fold(
                onSuccess = {
                    @Suppress("UNCHECKED_CAST")
                    val roller = it.getClaims("azuread")?.get("roles") as List<String>? ?: emptyList()
                    roller.contains("access_as_application")
                },
                onFailure = { throw ApiFeil("Kunne ikke hente 'roles' på token", HttpStatus.UNAUTHORIZED) },
            )
    }

    fun hentKlientnavn(): String {
        val claims = SpringTokenValidationContextHolder().tokenValidationContext.getClaims("azuread")
        return Result.runCatching { claims.getStringClaim("azp_name") }.fold(
            onSuccess = { it.split(":").last() },
            onFailure = { throw IllegalArgumentException("Fant ikke claim azp_name på token", it) },
        )
    }
}
