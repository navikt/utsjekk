package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.infrastruktur.util.opprettIverksettDto
import no.nav.dagpenger.kontrakter.datadeling.DatadelingRequest
import no.nav.dagpenger.kontrakter.datadeling.DatadelingResponse
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeType
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import no.nav.dagpenger.kontrakter.iverksett.VedtaksstatusDto
import no.nav.familie.http.client.MultipartBuilder
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.time.LocalDate

class IverksattStatusControllerTest : ServerTest() {

    @Test
    fun `skal svare med 401 uten token`() {
        val response: ResponseEntity<Any> = restTemplate.exchange(
            localhostUrl("/api/vedtakstatus/12345678910"),
            HttpMethod.GET,
            HttpEntity(null, headers),
        )

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun `skal svare med 404 når person ikke har noen vedtak`() {
        headers.setBearerAuth(lokalTestToken())

        val response: ResponseEntity<VedtaksstatusDto> = restTemplate.exchange(
            localhostUrl("/api/vedtakstatus/12345678910"),
            HttpMethod.GET,
            HttpEntity(null, headers),
        )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `skal svare med 200 når person har iverksatt vedtak`() {
        // Opprett testdata
        val beslutterGruppe = "0000-GA-Beslutter"
        System.setProperty("BESLUTTER_GRUPPE", beslutterGruppe)
        headers.setBearerAuth(lokalTestToken(grupper = listOf(beslutterGruppe)))
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)

        val iverksettJson = opprettIverksettDto(
            vedtaksperioder = listOf(
                VedtaksperiodeDto(
                    fraOgMedDato = LocalDate.now(),
                    tilOgMedDato = LocalDate.now().plusDays(14),
                    periodeType = VedtaksperiodeType.HOVEDPERIODE,
                ),
            ),
        )

        val iverksettRequest = MultipartBuilder()
            .withJson("data", iverksettJson)
            .withByteArray("fil", "1", byteArrayOf(12))
            .build()

        val iverksettResponse: ResponseEntity<Any> = restTemplate.exchange(
            localhostUrl("/api/iverksetting"),
            HttpMethod.POST,
            HttpEntity(iverksettRequest, headers),
        )
        Assertions.assertThat(iverksettResponse.statusCode.value()).isEqualTo(202)

        // Sjekk
        headers.clear()
        headers.setBearerAuth(lokalTestToken())

        val statusResponse: ResponseEntity<VedtaksstatusDto> = restTemplate.exchange(
            localhostUrl("/api/vedtakstatus/12345678910"),
            HttpMethod.GET,
            HttpEntity(null, headers),
        )

        assertEquals(HttpStatus.OK, statusResponse.statusCode)
        val vedtaksstatusDto: VedtaksstatusDto = statusResponse.body!!
        assertEquals(vedtaksstatusDto.vedtakstype, iverksettJson.vedtak.vedtakstype)
        assertEquals(vedtaksstatusDto.vedtakstidspunkt, iverksettJson.vedtak.vedtakstidspunkt)
        assertEquals(vedtaksstatusDto.resultat, Vedtaksresultat.INNVILGET)
        assertEquals(vedtaksstatusDto.vedtaksperioder.size, iverksettJson.vedtak.vedtaksperioder.size)
        assertEquals(vedtaksstatusDto.vedtaksperioder[0], iverksettJson.vedtak.vedtaksperioder[0])
    }

    @Test
    fun `skal hente iverksettinger for person of periode`() {
        val personId = "12345678910"

        // Opprett testdata
        val beslutterGruppe = "0000-GA-Beslutter"
        System.setProperty("BESLUTTER_GRUPPE", beslutterGruppe)
        headers.setBearerAuth(lokalTestToken(grupper = listOf(beslutterGruppe)))
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)

        val iverksettJson = opprettIverksettDto(
            vedtaksperioder = listOf(
                VedtaksperiodeDto(
                    fraOgMedDato = LocalDate.now(),
                    tilOgMedDato = LocalDate.now().plusDays(14),
                    periodeType = VedtaksperiodeType.HOVEDPERIODE,
                ),
            ),
        )

        val iverksettRequest = MultipartBuilder()
            .withJson("data", iverksettJson)
            .withByteArray("fil", "1", byteArrayOf(12))
            .build()

        val iverksettResponse: ResponseEntity<Any> = restTemplate.exchange(
            localhostUrl("/api/iverksetting"),
            HttpMethod.POST,
            HttpEntity(iverksettRequest, headers),
        )
        Assertions.assertThat(iverksettResponse.statusCode.value()).isEqualTo(202)

        // Sjekk
        headers.clear()
        headers.setBearerAuth(lokalTestToken())

        // Iverksetting:  |-------|
        // Request:     |----------
        val response1: ResponseEntity<DatadelingResponse> = restTemplate.exchange(
            localhostUrl("/api/vedtakstatus"),
            HttpMethod.POST,
            HttpEntity(DatadelingRequest(personId, LocalDate.now().minusDays(5), null), headers),
        )
        assertEquals(HttpStatus.OK, response1.statusCode)
        val datadelingResponse1 = response1.body!!
        assertEquals(1, datadelingResponse1.perioder.size)

        // Iverksetting:  |-------|
        // Request:         |---|
        val response2: ResponseEntity<DatadelingResponse> = restTemplate.exchange(
            localhostUrl("/api/vedtakstatus"),
            HttpMethod.POST,
            HttpEntity(
                DatadelingRequest(
                    personId,
                    LocalDate.now().plusDays(3),
                    LocalDate.now().plusDays(10)
                ), headers
            ),
        )
        assertEquals(HttpStatus.OK, response2.statusCode)
        val datadelingResponse2 = response2.body!!
        assertEquals(1, datadelingResponse2.perioder.size)

        // En annen ID
        val response3: ResponseEntity<DatadelingResponse> = restTemplate.exchange(
            localhostUrl("/api/vedtakstatus"),
            HttpMethod.POST,
            HttpEntity(DatadelingRequest("01020312345", LocalDate.now().minusDays(5), null), headers),
        )
        assertEquals(HttpStatus.OK, response3.statusCode)
        val datadelingResponse = response3.body!!
        assertEquals(0, datadelingResponse.perioder.size)
    }
}
