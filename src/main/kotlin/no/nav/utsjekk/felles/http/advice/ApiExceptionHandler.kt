package no.nav.utsjekk.felles.http.advice

import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import no.nav.utsjekk.kontrakter.felles.objectMapper
import no.nav.utsjekk.utbetaling.featuretoggle.IverksettingErSkruddAvException
import org.slf4j.LoggerFactory
import org.springframework.core.NestedExceptionUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {
    companion object {
        private val log = LoggerFactory.getLogger(ApiExceptionHandler::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }

    override fun handleExceptionInternal(
        ex: java.lang.Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ) = super.handleExceptionInternal(ex, body, headers, status, request).also {
        loggFeil(ex, status)
    }

    @ExceptionHandler(IverksettingErSkruddAvException::class)
    fun håndterKillSwitchException(exception: IverksettingErSkruddAvException) =
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(objectMapper.writeValueAsString(exception.message))

    @ExceptionHandler(JwtTokenMissingException::class, JwtTokenUnauthorizedException::class)
    fun håndterManglendeToken() = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build<Nothing>()

    @ExceptionHandler(Throwable::class)
    fun håndterFeil(throwable: Throwable): ResponseEntity<Nothing> {
        val responseStatus = throwable::class.annotations.find { it is ResponseStatus }?.let { it as ResponseStatus }
        if (responseStatus != null) {
            return håndtertResponseStatusFeil(throwable, responseStatus)
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build<Nothing?>().also {
            loggFeil(throwable, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @ExceptionHandler(ApiFeil::class)
    fun håndterApiFeil(feil: ApiFeil) = ResponseEntity.status(feil.httpStatus).body(feil.feil)

    private fun loggFeil(
        throwable: Throwable,
        status: HttpStatusCode,
    ) {
        secureLogger.error("En feil har oppstått", throwable)
        log.error("En feil har oppstått - throwable=${rootCause(throwable)} status=${status.value()}")
    }

    private fun håndtertResponseStatusFeil(
        throwable: Throwable,
        responseStatus: ResponseStatus,
    ): ResponseEntity<Nothing> {
        val status =
            when (responseStatus.value) {
                HttpStatus.INTERNAL_SERVER_ERROR -> responseStatus.value
                else -> responseStatus.code
            }

        val loggMelding =
            "En håndtert feil har oppstått" +
                " throwable=${rootCause(throwable)}" +
                " reason=${responseStatus.reason}" +
                " status=$status"

        when (throwable) {
            is JwtTokenUnauthorizedException -> logger.debug(loggMelding)
            else -> logger.error(loggMelding)
        }

        return ResponseEntity.status(status).build()
    }

    private fun rootCause(throwable: Throwable) = NestedExceptionUtils.getMostSpecificCause(throwable).javaClass.simpleName
}
