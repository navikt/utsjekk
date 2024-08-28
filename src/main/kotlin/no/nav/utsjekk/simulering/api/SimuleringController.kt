package no.nav.utsjekk.simulering.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.utsjekk.iverksetting.api.TokenContext
import no.nav.utsjekk.iverksetting.domene.KonsumentConfig
import no.nav.utsjekk.simulering.SimuleringService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException

@RestController
@RequestMapping(
    path = ["/api/simulering"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
@ProtectedWithClaims(issuer = "azuread")
@Tag(name = "Simulering")
class SimuleringController(
    private val simuleringService: SimuleringService,
    private val konsumentConfig: KonsumentConfig,
) {
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/tilleggsstonader"])
    @Operation(
        summary = "Simulering av utbetaling for tilleggsstønader",
        description = "Simulerer iverksetting",
        deprecated = true,
    )
    fun hentSimulering(
        @RequestBody requestDto: SimuleringRequestTilleggsstønaderDto,
    ) = catchSimuleringsfeil {
        val respons = simuleringService.hentSimuleringsresultatMedOppsummering(requestDto.tilInterntFormat())
        respons?.let { ResponseEntity.ok(it) } ?: ResponseEntity.noContent().build()
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/v2"])
    @Operation(
        summary = "Simulering av utbetaling",
        description = "Simulerer iverksetting",
    )
    @ApiResponse(responseCode = "200", description = "simulering utført ok")
    @ApiResponse(responseCode = "204", description = "ingen endring i utbetaling på saken, simulering utføres ikke")
    @ApiResponse(responseCode = "400", description = "ugyldig format på simulering")
    @ApiResponse(responseCode = "409", description = "simuleringen er i konflikt med tidligere utbetalinger")
    @ApiResponse(responseCode = "503", description = "OS/UR er midlertidig stengt")
    fun hentSimulering(
        @RequestBody requestDto: SimuleringRequestV2Dto,
    ) = catchSimuleringsfeil {
        val fagsystem = konsumentConfig.finnFagsystem(TokenContext.hentKlientnavn())
        val respons = simuleringService.hentSimuleringsresultatMedOppsummering(requestDto.tilInterntFormat(fagsystem))
        respons?.let { ResponseEntity.ok(it) } ?: ResponseEntity.noContent().build()
    }

    private fun <T> catchSimuleringsfeil(block: () -> T) =
        try {
            block.invoke()
        } catch (e: HttpClientErrorException) {
            when (e.statusCode) {
                HttpStatus.NOT_FOUND,
                HttpStatus.BAD_REQUEST,
                HttpStatus.CONFLICT,
                -> ResponseEntity.status(e.statusCode).body(e.message)

                else -> ResponseEntity.internalServerError().build()
            }
        } catch (e: HttpServerErrorException) {
            when (e.statusCode) {
                HttpStatus.SERVICE_UNAVAILABLE -> ResponseEntity.status(e.statusCode).body(e.message)
                else -> ResponseEntity.internalServerError().build()
            }
        }
}
