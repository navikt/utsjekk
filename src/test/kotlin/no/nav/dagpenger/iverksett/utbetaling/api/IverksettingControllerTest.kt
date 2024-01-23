package no.nav.dagpenger.iverksett.utbetaling.api

import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.felles.http.advice.Ressurs
import no.nav.dagpenger.iverksett.utbetaling.util.opprettIverksettDto
import no.nav.dagpenger.iverksett.utbetaling.util.opprettIverksettTilleggsstønaderDto
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.util.UUID

class IverksettingControllerTest : ServerTest() {
    private val behandlingId = GeneriskIdSomUUID(UUID.randomUUID())
    private val sakId = GeneriskIdSomUUID(UUID.randomUUID())

    @Value("\${BESLUTTER_GRUPPE}")
    private lateinit var beslutterGruppe: String

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken(grupper = listOf(beslutterGruppe)))
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }

    @Test
    internal fun `starte iverksetting gir 202 Accepted`() {
        val iverksettJson = opprettIverksettDto(behandlingId = behandlingId, sakId = sakId)

        val respons: ResponseEntity<Any> =
            restTemplate.exchange(
                localhostUrl("/api/iverksetting"),
                HttpMethod.POST,
                HttpEntity(iverksettJson, headers),
            )
        assertThat(respons.statusCode.value()).isEqualTo(202)
    }

    @Test
    internal fun `starte iverksetting for tilleggsstønader gir 202 Accepted`() {
        val iverksettJson = opprettIverksettTilleggsstønaderDto(behandlingId = behandlingId, sakId = sakId)

        val respons: ResponseEntity<Any> =
            restTemplate.exchange(
                localhostUrl("/api/iverksetting/tilleggsstønader"),
                HttpMethod.POST,
                HttpEntity(iverksettJson, headers),
            )
        assertThat(respons.statusCode.value()).isEqualTo(202)
    }

    @Test
    internal fun `Innvilget rammevedtak uten tilkjent ytelse gir 202 Accepted`() {
        val iverksettJson = opprettIverksettDto(behandlingId = behandlingId, sakId = sakId)
        val rammevedtak =
            iverksettJson.copy(
                vedtak =
                    iverksettJson.vedtak.copy(
                        utbetalinger = emptyList(),
                    ),
            )

        val responsRammevedtak: ResponseEntity<Ressurs<Nothing>> =
            restTemplate.exchange(
                localhostUrl("/api/iverksetting"),
                HttpMethod.POST,
                HttpEntity(rammevedtak, headers),
            )
        assertThat(responsRammevedtak.statusCode.value()).isEqualTo(202)
    }
}
