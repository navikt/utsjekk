package no.nav.dagpenger.iverksett.utbetaling.api


import no.nav.dagpenger.iverksett.felles.http.advice.ApiFeil
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

    fun hentKlientnavn(): String? {
        return Result.runCatching { SpringTokenValidationContextHolder().tokenValidationContext }
            .fold(
                onSuccess = {
                    @Suppress("UNCHECKED_CAST")
                   it.getClaims("azuread")?.get("azp_name") as String?

                },
                onFailure = { null },
            )


    }
}
