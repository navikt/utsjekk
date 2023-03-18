package no.nav.dagpenger.iverksett.infrastruktur

import no.nav.dagpenger.iverksett.kontrakter.felles.Enhet
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.personopplysning.Ident
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class FamilieIntegrasjonerClient(
    @Qualifier("azure") restOperations: RestOperations,
    @Value("\${FAMILIE_INTEGRASJONER_API_URL}")
    private val integrasjonUri: URI,
) : AbstractRestClient(restOperations, "familie.integrasjoner") {

    private val aktørUri =
        UriComponentsBuilder.fromUri(integrasjonUri).pathSegment(PATH_AKTØR).build().toUri()

    private fun arbeidsfordelingUri(tema: String) =
        UriComponentsBuilder.fromUri(integrasjonUri).pathSegment(PATH_ARBEIDSFORDELING, tema).build().toUri()

    private fun arbeidsfordelingOppfølingUri(tema: String) =
        UriComponentsBuilder.fromUri(integrasjonUri).pathSegment(PATH_ARBEIDSFORDELING_OPPFØLGING, tema).build().toUri()

    fun hentAktørId(personident: String): String {
        val response = postForEntity<Ressurs<MutableMap<*, *>>>(aktørUri, Ident(personident))
        return response.getDataOrThrow()["aktørId"].toString()
    }

    fun hentBehandlendeEnhetForOppfølging(personident: String): Enhet? {
        val response =
            postForEntity<Ressurs<List<Enhet>>>(arbeidsfordelingOppfølingUri(TEMA_ENSLIG_FORSØRGER), Ident(personident))
        return response.getDataOrThrow().firstOrNull()
    }

    fun hentBehandlendeEnhetForBehandling(personident: String): Enhet? {
        val response = postForEntity<Ressurs<List<Enhet>>>(arbeidsfordelingUri(TEMA_ENSLIG_FORSØRGER), Ident(personident))
        return response.getDataOrThrow().firstOrNull()
    }

    companion object {

        private const val TEMA_ENSLIG_FORSØRGER = "ENF" // NAY - 4489
        const val PATH_ARBEIDSFORDELING = "api/arbeidsfordeling/enhet"
        const val PATH_ARBEIDSFORDELING_OPPFØLGING = "api/arbeidsfordeling/oppfolging"
        const val PATH_AKTØR = "api/aktoer/v2/ENF"
        const val PATH_HENT_IDENTER = "api/personopplysning/v1/identer/ENF"
    }
}
