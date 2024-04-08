package no.nav.utsjekk.felles.oppdrag.konfig.interceptor

import no.nav.utsjekk.felles.http.MDCConstants
import no.nav.utsjekk.felles.http.NavHttpHeaders
import no.nav.familie.prosessering.util.IdUtils
import org.slf4j.MDC
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component

@Component
class MdcValuesPropagatingClientInterceptor : ClientHttpRequestInterceptor {

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        val callId = MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()
        val requestId = MDC.get(MDCConstants.MDC_REQUEST_ID) ?: callId
        request.headers.add(NavHttpHeaders.NAV_CALL_ID.asString(), callId)
        request.headers.add(NavHttpHeaders.NGNINX_REQUEST_ID.asString(), requestId)

        return execution.execute(request, body)
    }
}
