package no.nav.dagpenger.iverksett.felles.util

import no.nav.security.mock.oauth2.MockOAuth2Server
import java.util.UUID

object TokenUtil {
    fun onBehalfOfToken(
        mockOAuth2Server: MockOAuth2Server,
        saksbehandler: String,
        grupper: List<String> = emptyList(),
        klientnavn: String = "dev-gcp:teamdagpenger:dp-vedtak-iverksett",
    ): String {
        val thisId = UUID.randomUUID().toString()
        val clientId = UUID.randomUUID().toString()

        val claims =
            mapOf(
                "oid" to thisId,
                "azp" to clientId,
                "azp_name" to klientnavn,
                "name" to saksbehandler,
                "NAVident" to saksbehandler,
                "groups" to grupper,
            )

        return mockOAuth2Server.issueToken(
            issuerId = "azuread",
            subject = thisId,
            audience = "aud-localhost",
            claims = claims,
        ).serialize()
    }
}
