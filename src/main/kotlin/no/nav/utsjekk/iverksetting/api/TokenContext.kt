package no.nav.utsjekk.iverksetting.api

import no.nav.security.token.support.spring.SpringTokenValidationContextHolder

object TokenContext {
    fun hentKlientnavn(): String {
        val claims = SpringTokenValidationContextHolder().getTokenValidationContext().getClaims("azuread")
        return Result.runCatching { claims.getStringClaim("azp_name") }.fold(
            onSuccess = { it.split(":").last() },
            onFailure = { throw IllegalArgumentException("Fant ikke claim azp_name på token", it) },
        )
    }

    fun erSystemkontekst(): Boolean {
        return SpringTokenValidationContextHolder().getTokenValidationContext().getClaims("azuread")
            .containsClaim("roles", "access_as_application")
    }
}
