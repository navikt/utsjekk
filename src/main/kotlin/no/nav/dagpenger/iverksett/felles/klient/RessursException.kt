package no.nav.dagpenger.iverksett.felles.klient

import no.nav.dagpenger.iverksett.felles.http.advice.Ressurs
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClientResponseException

class RessursException(
        val ressurs: Ressurs<Any>,
        cause: RestClientResponseException,
        val httpStatus: HttpStatus = HttpStatus.valueOf(cause.statusCode.value())
) : RuntimeException(cause)
