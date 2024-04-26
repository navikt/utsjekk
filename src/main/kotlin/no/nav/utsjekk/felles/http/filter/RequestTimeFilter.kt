package no.nav.utsjekk.felles.http.filter

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.util.StopWatch
import java.io.IOException

open class RequestTimeFilter : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        servletRequest: ServletRequest,
        servletResponse: ServletResponse,
        filterChain: FilterChain,
    ) {
        val response = servletResponse as HttpServletResponse
        val request = servletRequest as HttpServletRequest
        val timer = StopWatch()
        try {
            timer.start()
            filterChain.doFilter(servletRequest, servletResponse)
        } finally {
            timer.stop()
            log(request, response.status, timer)
        }
    }

    private fun log(
        request: HttpServletRequest,
        code: Int,
        timer: StopWatch,
    ) {
        val loggForRequest = "${request.method} - ${request.requestURI} - ($code). Dette tok ${timer.totalTimeMillis} ms"
        if (HttpStatus.valueOf(code).isError) {
            logger.warn(loggForRequest)
        } else {
            if (!erInterntEndepunkt(request.requestURI)) {
                logger.info(loggForRequest)
            }
        }
    }

    companion object {
        @Suppress("MemberVisibilityCanBePrivate") // kan overrides hvis det ønskes
        fun erInterntEndepunkt(uri: String): Boolean {
            return uri.contains("/internal")
        }

        private val logger = LoggerFactory.getLogger(RequestTimeFilter::class.java)
    }
}
