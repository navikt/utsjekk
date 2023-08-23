package no.nav.dagpenger.iverksett.konsumenter.økonomi.simulering

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.dagpenger.iverksett.infrastruktur.advice.Ressurs
import no.nav.dagpenger.iverksett.infrastruktur.transformer.toDomain
import no.nav.dagpenger.kontrakter.iverksett.SimuleringDto
import no.nav.dagpenger.kontrakter.oppdrag.simulering.BeriketSimuleringsresultat
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/simulering")
@ProtectedWithClaims(issuer = "azuread")
@Validated
class SimuleringController(
    private val simuleringService: SimuleringService,
) {

    @PostMapping(produces = ["application/json"])
    @Tag(name = "Simulering")
    @Operation(hidden = true)
    fun hentSimulering(@RequestBody simuleringDto: SimuleringDto): Ressurs<BeriketSimuleringsresultat> {
        val beriketSimuleringResultat =
            simuleringService.hentBeriketSimulering(simuleringDto.toDomain())
        return Ressurs.success(beriketSimuleringResultat)
    }
}
