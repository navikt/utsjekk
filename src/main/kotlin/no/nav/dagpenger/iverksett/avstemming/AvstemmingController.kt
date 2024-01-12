package no.nav.dagpenger.iverksett.avstemming

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
        path = ["/api/avstemming"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
)
@ProtectedWithClaims(issuer = "azuread")
class AvstemmingController(private val avstemmingService: AvstemmingService) {

    @PostMapping("/start")
    @Tag(name = "Grensesnittavstemming")
    @Operation(summary = "Start grensesnittavstemming. Kjøres bare én gang")
    fun startGrensesnittavstemming(): ResponseEntity<Void> {
        return when (avstemmingService.lagreGrensesnittavstemmingTask()) {
            true -> ResponseEntity.accepted().build()
            false -> ResponseEntity.status(409).build()
        }
    }
}