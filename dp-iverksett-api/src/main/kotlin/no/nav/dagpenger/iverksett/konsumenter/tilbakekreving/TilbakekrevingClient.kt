package no.nav.dagpenger.iverksett.konsumenter.tilbakekreving

import no.nav.dagpenger.iverksett.kontrakter.felles.Fagsystem
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Behandling
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.FinnesBehandlingResponse
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.ForhåndsvisVarselbrevRequest
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Ytelsestype
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.tilbakekreving.KanBehandlingOpprettesManueltRespons
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class TilbakekrevingClient(
    @Qualifier("azure") restOperations: RestOperations,
    @Value("\${FAMILIE_TILBAKE_URL}") private val familieTilbakeUri: URI,
) :
    AbstractRestClient(restOperations, "familie.tilbakekreving") {

    private val hentForhåndsvisningVarselbrevUri: URI = UriComponentsBuilder.fromUri(familieTilbakeUri)
        .pathSegment("api/dokument/forhandsvis-varselbrev")
        .build()
        .toUri()

    private val opprettTilbakekrevingUri: URI =
        UriComponentsBuilder.fromUri(familieTilbakeUri).pathSegment("api/behandling/v1").build().toUri()

    private val opprettBehandlingManueltUri = UriComponentsBuilder.fromUri(familieTilbakeUri)
        .pathSegment("api/behandling/manuelt/task/v1")
        .build()
        .toUri()

    private fun finnesÅpenBehandlingUri(fagsakId: Long) = UriComponentsBuilder.fromUri(familieTilbakeUri)
        .pathSegment("api/fagsystem/${Fagsystem.DP}/fagsak/$fagsakId/finnesApenBehandling/v1")
        .build()
        .toUri()

    private fun finnBehandlingerUri(fagsakId: Long) = UriComponentsBuilder.fromUri(familieTilbakeUri)
        .pathSegment("api/fagsystem/${Fagsystem.DP}/fagsak/$fagsakId/behandlinger/v1")
        .build()
        .toUri()

    private fun kanBehandlingOpprettesManueltUri(fagsakId: Long, ytelsestype: Ytelsestype) = UriComponentsBuilder.fromUri(
        familieTilbakeUri,
    )
        .pathSegment("api", "ytelsestype", ytelsestype.name, "fagsak", fagsakId.toString(), "kanBehandlingOpprettesManuelt", "v1")
        .encode()
        .build()
        .toUri()

    fun hentForhåndsvisningVarselbrev(forhåndsvisVarselbrevRequest: ForhåndsvisVarselbrevRequest): ByteArray {
        return postForEntity(
            hentForhåndsvisningVarselbrevUri,
            forhåndsvisVarselbrevRequest,
            HttpHeaders().apply { accept = listOf(MediaType.APPLICATION_PDF) },
        )
    }

    fun opprettBehandling(opprettTilbakekrevingRequest: OpprettTilbakekrevingRequest) {
        postForEntity<Ressurs<String>>(opprettTilbakekrevingUri, opprettTilbakekrevingRequest)
    }

    fun finnesÅpenBehandling(fagsakId: Long): Boolean {
        val response: Ressurs<FinnesBehandlingResponse> = getForEntity(finnesÅpenBehandlingUri(fagsakId))
        return response.getDataOrThrow().finnesÅpenBehandling
    }

    fun finnBehandlinger(fagsakId: Long): List<Behandling> {
        val response: Ressurs<List<Behandling>> = getForEntity(finnBehandlingerUri(fagsakId))
        return response.getDataOrThrow()
    }

    fun kanBehandlingOpprettesManuelt(fagsakId: Long, ytelsestype: Ytelsestype): KanBehandlingOpprettesManueltRespons {
        val response: Ressurs<KanBehandlingOpprettesManueltRespons> =
            getForEntity(kanBehandlingOpprettesManueltUri(fagsakId, ytelsestype))
        return response.getDataOrThrow()
    }
}
