package no.nav.utsjekk.simulering.api

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.utsjekk.simulering.SimuleringService
import no.nav.utsjekk.simulering.api.Mapper.tilInterntFormat
import no.nav.utsjekk.simulering.client.dto.SimuleringResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/simulering")
@ProtectedWithClaims(issuer = "azuread")
class SimuleringController(private val simuleringService: SimuleringService) {
    @PostMapping
    fun hentSimulering(
        @RequestBody requestDto: SimuleringRequestDto,
    ): ResponseEntity<SimuleringResponse> {
        val respons = simuleringService.hentSimuleringsresultatMedOppsummering(requestDto.tilInterntFormat())
        return ResponseEntity.ok(respons)
    }
}
