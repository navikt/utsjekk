package no.nav.dagpenger.iverksett.api

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.iverksett.util.vedtaksstatusDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class IverksattStatusControllerTest {

    private val vedtakStatusServiceMock: VedtakStatusService = mockk()
    private val iverksattStatusController = IverksattStatusController(vedtakStatusServiceMock)

    @Test
    fun `skal svare med 404 når person ikke har noen vedtak`() {
        every { vedtakStatusServiceMock.getVedtakStatus(any()) } returns null

        val response = iverksattStatusController.hentStatusForPerson("12345678910")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `skal svare med 200 når person har iverksatt vedtak`() {
        val vedtaksdetaljerDto = vedtaksstatusDto()
        every { vedtakStatusServiceMock.getVedtakStatus(any()) } returns vedtaksdetaljerDto

        val response = iverksattStatusController.hentStatusForPerson("12345678910")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(vedtaksdetaljerDto, response.body)
    }
}
