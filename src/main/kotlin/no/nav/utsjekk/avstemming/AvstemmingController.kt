package no.nav.utsjekk.avstemming

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    path = ["/intern/avstemming"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
@ProtectedWithClaims(issuer = "azuread")
class AvstemmingController(private val avstemmingService: AvstemmingService) {
    @PostMapping("/start/{fagsystem}")
    @Tag(name = "Grensesnittavstemming")
    @Operation(summary = "Start grensesnittavstemming. Kjøres bare én gang")
    fun startGrensesnittavstemming(
        @PathVariable("fagsystem") fagsystem: Fagsystem,
    ): ResponseEntity<Void> {
        avstemmingService.opprettGrensesnittavstemmingTask(fagsystem)
        return ResponseEntity.accepted().build()
    }
}
