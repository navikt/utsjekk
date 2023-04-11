package no.nav.dagpenger.iverksett.konsumenter.økonomi.simulering

import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.dagpenger.iverksett.infrastruktur.transformer.toDomain
import no.nav.dagpenger.iverksett.kontrakter.iverksett.SimuleringDto
import no.nav.dagpenger.iverksett.kontrakter.simulering.BeriketSimuleringsresultat
import no.nav.dagpenger.iverksett.kontrakter.simulering.DetaljertSimuleringResultat
import no.nav.familie.kontrakter.felles.Ressurs
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

    @PostMapping()
    @Tag(name = "Simulering")
    fun hentSimuleringV2(@RequestBody simuleringDto: SimuleringDto): Ressurs<BeriketSimuleringsresultat> {
        val beriketSimuleringResultat =
            simuleringService.hentBeriketSimulering(simuleringDto.toDomain())
        return Ressurs.success(beriketSimuleringResultat)
    }
}
