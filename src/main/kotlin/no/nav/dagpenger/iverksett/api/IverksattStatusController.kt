package no.nav.dagpenger.iverksett.api

import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.dagpenger.kontrakter.iverksett.VedtaksstatusDto
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    path = ["/api/vedtakstatus"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
@Unprotected
class IverksattStatusController(
    private val vedtakStatusService: VedtakStatusService,
) {
    @GetMapping("/{personId}", produces = ["application/json"])
    @Tag(name = "Status")
    fun hentStatusForPerson(@PathVariable personId: String): ResponseEntity<VedtaksstatusDto> {
        val status = vedtakStatusService.getVedtakStatus(personId)
        return status?.let { ResponseEntity(status, HttpStatus.OK) } ?: ResponseEntity(null, HttpStatus.NOT_FOUND)
    }
}
