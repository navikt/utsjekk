package no.nav.utsjekk.iverksetting.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.prosessering.internal.TaskService
import no.nav.utsjekk.Integrasjonstest
import no.nav.utsjekk.iverksetting.featuretoggle.IverksettingErSkruddAvException
import no.nav.utsjekk.iverksetting.task.IverksettMotOppdragTask
import no.nav.utsjekk.iverksetting.util.enIverksettV2Dto
import no.nav.utsjekk.konfig.FeatureToggleMock
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.felles.objectMapper
import no.nav.utsjekk.kontrakter.iverksett.IverksettStatus
import no.nav.utsjekk.kontrakter.iverksett.IverksettV2Dto
import no.nav.utsjekk.kontrakter.iverksett.VedtaksdetaljerV2Dto
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime

class IverksettingControllerTest : Integrasjonstest() {
    private val behandlingId = "1"
    private val sakId = "1234"

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var iverksettMotOppdragTask: IverksettMotOppdragTask

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken())
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }

    @AfterEach
    fun resetMocks() {
        FeatureToggleMock.resetMock()
    }

    @Test
    fun `iverksetter ikke når kill switch for ytelsen er skrudd på`() {
        FeatureToggleMock.skruAvFagsystem(Fagsystem.TILTAKSPENGER)

        val iverksettJson = enIverksettV2Dto(behandlingId = behandlingId, sakId = sakId)

        val respons: ResponseEntity<Any> =
            restTemplate.exchange(
                localhostUrl("/api/iverksetting/v2"),
                HttpMethod.POST,
                HttpEntity(iverksettJson, headers),
            )

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, respons.statusCode)
        assertEquals(IverksettingErSkruddAvException(Fagsystem.TILTAKSPENGER).message, respons.body)
    }

    @Test
    fun `start iverksetting`() {
        val iverksettJson = enIverksettV2Dto(behandlingId = behandlingId, sakId = sakId)

        val respons: ResponseEntity<Any> =
            restTemplate.exchange(
                localhostUrl("/api/iverksetting/v2"),
                HttpMethod.POST,
                HttpEntity(iverksettJson, headers),
            )

        assertEquals(HttpStatus.ACCEPTED, respons.statusCode)
    }

    @Test
    fun `returnerer beskrivende feilmelding når jackson ikke greier å deserialisere request`() {
        @Language("JSON")
        val payload =
            """
            {
              "sakId": "1234",
              "behandlingId": "1",
              "personident": {
                "verdi": "15507600333"
              },
              "vedtak": {
                "vedtakstidspunkt": "2021-05-12T00:00:00",
                "saksbehandlerId": "A12345",
                "utbetalinger": [
                  {
                    "beløp": 500,
                    "satstype": "DAGLIG",
                    "fraOgMedDato": "2021-01-01",
                    "tilOgMedDato": "2021-12-31",
                    "stønadsdata": {
                      "stønadstype": "DAGPENGER_ARBEIDSSØKER_ORDINÆR",
                      "ferietillegg": null,
                      "meldekortId": "UKE_40_41_2024"
                    }
                  }
                ]
              },
              "forrigeIverksetting": null
            }
            """.trimIndent()

        val respons: ResponseEntity<Any> =
            restTemplate.exchange(
                localhostUrl("/api/iverksetting/v2"),
                HttpMethod.POST,
                HttpEntity(objectMapper.readValue<JsonNode>(payload), headers),
            )

        assertEquals(HttpStatus.BAD_REQUEST, respons.statusCode)
        assertEquals("Mangler påkrevd felt: vedtak.beslutterId", respons.body)
    }

    @Test
    fun `start iverksetting v2 av tilleggsstønader`() {
        headers.setBearerAuth(lokalTestToken(klientapp = "dev-gcp:tilleggsstonader:tilleggsstonader-sak"))

        val dto =
            enIverksettV2Dto(
                behandlingId = behandlingId,
                sakId = sakId,
                iverksettingId = "en-iverksetting",
            )

        restTemplate
            .exchange<Unit>(
                localhostUrl("/api/iverksetting/v2"),
                HttpMethod.POST,
                HttpEntity(dto, headers),
            ).also {
                assertEquals(HttpStatus.ACCEPTED, it.statusCode)
            }

        kjørTasks()

        restTemplate
            .exchange<IverksettStatus>(
                localhostUrl("/api/iverksetting/${dto.sakId}/${dto.behandlingId}/${dto.iverksettingId}/status"),
                HttpMethod.GET,
                HttpEntity(null, headers),
            ).also {
                assertEquals(HttpStatus.OK, it.statusCode)
                assertEquals(IverksettStatus.SENDT_TIL_OPPDRAG, it.body)
            }
    }

    @Test
    fun `start iverksetting av vedtak uten utbetaling`() {
        val dto =
            IverksettV2Dto(
                behandlingId = behandlingId,
                sakId = sakId,
                personident = Personident("15507600333"),
                vedtak =
                    VedtaksdetaljerV2Dto(
                        vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
                        saksbehandlerId = "A12345",
                        beslutterId = "B23456",
                        utbetalinger = emptyList(),
                    ),
            )

        restTemplate
            .exchange<Unit>(
                localhostUrl("/api/iverksetting/v2"),
                HttpMethod.POST,
                HttpEntity(dto, headers),
            ).also {
                assertEquals(HttpStatus.ACCEPTED, it.statusCode)
            }

        kjørTasks()

        restTemplate
            .exchange<IverksettStatus>(
                localhostUrl("/api/iverksetting/$sakId/$behandlingId/status"),
                HttpMethod.GET,
                HttpEntity(null, headers),
            ).also {
                assertEquals(HttpStatus.OK, it.statusCode)
                assertEquals(IverksettStatus.OK_UTEN_UTBETALING, it.body)
            }
    }

    private fun kjørTasks() {
        taskService.findAll().let { tasks ->
            iverksettMotOppdragTask.doTask(tasks.first())
        }
    }
}
