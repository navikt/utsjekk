package no.nav.dagpenger.iverksett.felles.http.advice

import no.nav.dagpenger.iverksett.utbetaling.featuretoggle.IverksettingErSkruddAvException
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
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
    private val log = LoggerFactory.getLogger(javaClass)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    private fun rootCause(throwable: Throwable) = NestedExceptionUtils.getMostSpecificCause(throwable).javaClass.simpleName

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
    fun handleKillSwitchException(exception: IverksettingErSkruddAvException) =
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(objectMapper.writeValueAsString(exception.message))

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(throwable: Throwable): ResponseEntity<Nothing> {
        val responseStatus = throwable::class.annotations.find { it is ResponseStatus }?.let { it as ResponseStatus }
        if (responseStatus != null) {
            return håndtertResponseStatusFeil(throwable, responseStatus)
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build<Nothing?>().also {
            loggFeil(throwable, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    private fun loggFeil(
        throwable: Throwable,
        status: HttpStatusCode,
    ) {
        logger.error("En feil har oppstått", throwable)
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

    @ExceptionHandler(ApiFeil::class)
    fun handleApiFeil(feil: ApiFeil) = ResponseEntity.status(feil.httpStatus).body(feil.feil)
}
