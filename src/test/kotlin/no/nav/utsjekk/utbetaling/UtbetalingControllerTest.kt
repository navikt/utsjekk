package no.nav.utsjekk.utbetaling

import no.nav.utsjekk.Integrasjonstest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.*
import java.time.LocalDate
import java.time.LocalDateTime

class UtbetalingControllerTest : Integrasjonstest() {

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalClientCredentialsTestToken(accessAsApplication = true))
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }

    @Test
    fun `kan opprette ny utbetaling`() {
        val body = UtbetalingDto(
            sakId = "123",
            behandlingId = "abc",
            personident = "",
            vedtak = UtbetalingDto.VedtakDto(
                vedtakstidspunkt = LocalDateTime.now(),
                saksbehandlerId = "",
                beslutterId = "",
                utbetalinger = listOf(
                    UtbetalingDto.VedtakDto.DagsatsDto(
                        beløp = 500u,
                        stønadstype = Stønadstype.Tilleggsstønader.TILSYN_BARN_AAP.navn(),
                        brukersNavKontor = null,
                        dato = LocalDate.now(),
                    )
                ),
            ),
        )

        val respons: ResponseEntity<Any> = restTemplate.exchange(
            localhostUrl("/api/utbetalinger"),
            HttpMethod.POST,
            HttpEntity(body, headers),
        )

        assertEquals(HttpStatus.CREATED, respons.statusCode)
    }

    private fun dagsats(dato: LocalDate) = UtbetalingDto.VedtakDto.DagsatsDto(
        beløp = 500u,
        stønadstype = Stønadstype.Tilleggsstønader.TILSYN_BARN_AAP.navn(),
        brukersNavKontor = "",
        dato = dato,
    )

    private fun vedtak(sats: UtbetalingDto.VedtakDto.SatsDto) = UtbetalingDto.VedtakDto(
        vedtakstidspunkt = LocalDateTime.now(),
        saksbehandlerId = "",
        beslutterId = "",
        utbetalinger = listOf(sats),
    )

    private fun utbetaling(vedtak: UtbetalingDto.VedtakDto) = UtbetalingDto(
        sakId = "123",
        behandlingId = "abc",
        personident = "",
        vedtak = vedtak,
    )

    // Samme sakid,
    // samme behandlingsid
    // samme utbetalingsid
    @Test
    fun `gjenbruk behandlingsId`() {
        val dagsats = dagsats(LocalDate.now())
        val vedtak = vedtak(dagsats)
        val utbetaling = utbetaling(vedtak)

        val location = restTemplate.exchange<Any>(
            localhostUrl("/api/utbetalinger"),
            HttpMethod.POST,
            HttpEntity(utbetaling, headers),
        ).let {
            assertEquals(HttpStatus.CREATED, it.statusCode)
            requireNotNull(it.headers.location) { "location header is missing" }
        }

        val nesteUtbetaling = utbetaling.copy(
            vedtak = utbetaling.vedtak.copy(
                utbetalinger = listOf(
                    dagsats.copy(
                        dato = LocalDate.now().plusDays(1)
                    )
                )
            )
        )

        restTemplate.exchange<Any>(
            location.toString(),
            HttpMethod.PUT,
            HttpEntity(nesteUtbetaling, headers),
        ).also {
            assertEquals(HttpStatus.OK, it.statusCode)
        }
    }

    // Samme sakid,
    // ny behandlingsId
    // samme utbetalingsid
    @Test
    fun `legg til på utbetalingslinje`() {

    }

    // Samme sakid TODO: eller ny?
    // ny behandlingsid
    // ny utbetalingsid
    @Test
    fun `ny utbetalingslinje`() {

    }
}












