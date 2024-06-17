package no.nav.utsjekk.simulering.api

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.utsjekk.felles.http.advice.ApiFeil
import no.nav.utsjekk.kontrakter.felles.objectMapper
import no.nav.utsjekk.simulering.SimuleringService
import no.nav.utsjekk.simulering.api.Mapper.tilInterntFormat
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
class SimuleringController(private val simuleringService: SimuleringService) {
    @PostMapping
    fun hentSimulering(
        @RequestBody requestDto: SimuleringRequestDto,
    ) = try {
        requestDto.valider()
        val respons = simuleringService.hentSimuleringsresultatMedOppsummering(requestDto.tilInterntFormat())
        ResponseEntity.ok(respons)
    } catch (e: HttpClientErrorException) {
        when (e.statusCode) {
            HttpStatus.NOT_FOUND,
            HttpStatus.BAD_REQUEST,
            HttpStatus.SERVICE_UNAVAILABLE,
            HttpStatus.CONFLICT -> ResponseEntity.status(e.statusCode).body(e.message)

            else -> ResponseEntity.internalServerError()
        }
    } catch (e: ApiFeil) {
        when (e.httpStatus) {
            HttpStatus.NOT_FOUND,
            HttpStatus.BAD_REQUEST,
            HttpStatus.SERVICE_UNAVAILABLE,
            HttpStatus.CONFLICT -> ResponseEntity.status(e.httpStatus).body(objectMapper.writeValueAsString(e.feil))

            else -> ResponseEntity.internalServerError()
        }
    }
}

fun SimuleringRequestDto.valider() {
    if (utbetalinger.isEmpty()) {
        throw ApiFeil("Ingen utbetalinger å simulere", HttpStatus.BAD_REQUEST)
    }
}