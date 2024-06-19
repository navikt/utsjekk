package no.nav.utsjekk.simulering.api

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.utsjekk.felles.http.advice.ApiFeil
import no.nav.utsjekk.iverksetting.api.TokenContext
import no.nav.utsjekk.iverksetting.domene.KonsumentConfig
import no.nav.utsjekk.kontrakter.felles.objectMapper
import no.nav.utsjekk.simulering.SimuleringService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException

@RestController
@RequestMapping("/api/simulering")
@ProtectedWithClaims(issuer = "azuread")
class SimuleringController(
    private val simuleringService: SimuleringService,
    private val konsumentConfig: KonsumentConfig,
) {
    @PostMapping("/tilleggsstonader")
    fun hentSimulering(
        @RequestBody requestDto: SimuleringRequestTilleggsstønaderDto,
    ) = catchSimuleringsfeil {
        val respons = simuleringService.hentSimuleringsresultatMedOppsummering(requestDto.tilInterntFormat())
        ResponseEntity.ok(respons)
    }

    @PostMapping
    fun hentSimulering(
        @RequestBody requestDto: SimuleringRequestDto,
    ) = catchSimuleringsfeil {
        val fagsystem = konsumentConfig.finnFagsystem(TokenContext.hentKlientnavn())
        val respons = simuleringService.hentSimuleringsresultatMedOppsummering(requestDto.tilInterntFormat(fagsystem))
        ResponseEntity.ok(respons)
    }

    private fun <T>catchSimuleringsfeil(block: () -> T) = try {
        block.invoke()
    } catch (e: HttpClientErrorException) {
        when (e.statusCode) {
            HttpStatus.NOT_FOUND,
            HttpStatus.BAD_REQUEST,
            HttpStatus.SERVICE_UNAVAILABLE,
            HttpStatus.CONFLICT,
            -> ResponseEntity.status(e.statusCode).body(e.message)

            else -> ResponseEntity.internalServerError()
        }
    }
}
