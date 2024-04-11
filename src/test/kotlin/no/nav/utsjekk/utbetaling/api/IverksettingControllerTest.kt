package no.nav.utsjekk.utbetaling.api

import no.nav.familie.prosessering.internal.TaskService
import no.nav.utsjekk.Integrasjonstest
import no.nav.utsjekk.konfig.FeatureToggleMock
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.iverksett.IverksettDto
import no.nav.utsjekk.kontrakter.iverksett.IverksettStatus
import no.nav.utsjekk.kontrakter.iverksett.IverksettTilleggsstønaderDto
import no.nav.utsjekk.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.utsjekk.kontrakter.iverksett.VedtaksdetaljerTilleggsstønaderDto
import no.nav.utsjekk.utbetaling.domene.transformer.RandomOSURId
import no.nav.utsjekk.utbetaling.featuretoggle.IverksettingErSkruddAvException
import no.nav.utsjekk.utbetaling.task.IverksettMotOppdragTask
import no.nav.utsjekk.utbetaling.util.enIverksettDto
import no.nav.utsjekk.utbetaling.util.enIverksettTilleggsstønaderDto
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
        FeatureToggleMock.skruAvFagsystem(Fagsystem.DAGPENGER)

        val iverksettJson = enIverksettDto(behandlingId = behandlingId, sakId = sakId)

        val respons: ResponseEntity<Any> =
            restTemplate.exchange(
                localhostUrl("/api/iverksetting/v1"),
                HttpMethod.POST,
                HttpEntity(iverksettJson, headers),
            )

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, respons.statusCode)
        assertEquals(IverksettingErSkruddAvException().message, respons.body)
    }

    @Test
    fun `start iverksetting`() {
        val iverksettJson = enIverksettDto(behandlingId = behandlingId, sakId = sakId)

        val respons: ResponseEntity<Any> =
            restTemplate.exchange(
                localhostUrl("/api/iverksetting/v1"),
                HttpMethod.POST,
                HttpEntity(iverksettJson, headers),
            )

        assertEquals(HttpStatus.ACCEPTED, respons.statusCode)
    }

    @Test
    fun `start iverksetting av tilleggsstønader`() {
        val dto =
            enIverksettTilleggsstønaderDto(
                behandlingId = behandlingId,
                sakId = sakId,
                iverksettingId = "en-iverksetting",
            )

        restTemplate.exchange<Unit>(
            localhostUrl("/api/iverksetting/tilleggsstonader/v1"),
            HttpMethod.POST,
            HttpEntity(dto, headers),
        ).also {
            assertEquals(HttpStatus.ACCEPTED, it.statusCode)
        }

        kjørTasks()

        restTemplate.exchange<IverksettStatus>(
            localhostUrl("/api/iverksetting/${dto.sakId}/${dto.behandlingId}/${dto.iverksettingId}/status/v1"),
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
            IverksettDto(
                behandlingId = behandlingId,
                sakId = sakId,
                personident = Personident("15507600333"),
                vedtak =
                    VedtaksdetaljerDto(
                        vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
                        saksbehandlerId = "A12345",
                        beslutterId = "B23456",
                        brukersNavKontor = null,
                        utbetalinger = emptyList(),
                    ),
            )

        restTemplate.exchange<Unit>(
            localhostUrl("/api/iverksetting/v1"),
            HttpMethod.POST,
            HttpEntity(dto, headers),
        ).also {
            assertEquals(HttpStatus.ACCEPTED, it.statusCode)
        }

        kjørTasks()

        restTemplate.exchange<IverksettStatus>(
            localhostUrl("/api/iverksetting/$sakId/$behandlingId/status/v1"),
            HttpMethod.GET,
            HttpEntity(null, headers),
        ).also {
            assertEquals(HttpStatus.OK, it.statusCode)
            assertEquals(IverksettStatus.OK_UTEN_UTBETALING, it.body)
        }
    }

    @Test
    fun `start iverksetting av vedtak for tilleggsstønader uten utbetaling`() {
        val behandlingId = RandomOSURId.generate()
        val sakId = RandomOSURId.generate()
        val iverksettingId = "c2502e75-6e78-40fa-9124-6549341855b4"

        val dto =
            IverksettTilleggsstønaderDto(
                behandlingId = behandlingId,
                sakId = sakId,
                personident = Personident("18498636957"),
                vedtak =
                    VedtaksdetaljerTilleggsstønaderDto(
                        beslutterId = "Z994230",
                        saksbehandlerId = "Z994230",
                        utbetalinger = emptyList(),
                        vedtakstidspunkt = LocalDateTime.of(2024, 2, 19, 13, 10),
                    ),
                iverksettingId = iverksettingId,
            )

        restTemplate.exchange<Unit>(
            localhostUrl("/api/iverksetting/tilleggsstonader/v1"),
            HttpMethod.POST,
            HttpEntity(dto, headers),
        ).also {
            assertEquals(HttpStatus.ACCEPTED, it.statusCode)
        }

        kjørTasks()

        restTemplate.exchange<IverksettStatus>(
            localhostUrl("/api/iverksetting/$sakId/$behandlingId/$iverksettingId/status/v1"),
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
