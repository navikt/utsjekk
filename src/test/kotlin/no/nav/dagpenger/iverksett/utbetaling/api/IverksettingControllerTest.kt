package no.nav.dagpenger.iverksett.utbetaling.api

import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.utbetaling.task.IverksettMotOppdragTask
import no.nav.dagpenger.iverksett.utbetaling.util.enIverksettDto
import no.nav.dagpenger.iverksett.utbetaling.util.enIverksettTilleggsstønaderDto
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.felles.Personident
import no.nav.dagpenger.kontrakter.felles.somUUID
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.IverksettStatus
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
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
import java.util.UUID

class IverksettingControllerTest : ServerTest() {
    private val behandlingId = GeneriskIdSomUUID(UUID.randomUUID())
    private val sakId = GeneriskIdSomUUID(UUID.randomUUID())

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var iverksettMotOppdragTask: IverksettMotOppdragTask

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken(grupper = listOf(konsumentConfig.konsumenter["dagpenger"]!!.grupper.beslutter)))
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }

    @Test
    fun `start iverksetting`() {
        val iverksettJson = enIverksettDto(behandlingId = behandlingId, sakId = sakId)

        val respons: ResponseEntity<Any> =
            restTemplate.exchange(
                localhostUrl("/api/iverksetting"),
                HttpMethod.POST,
                HttpEntity(iverksettJson, headers),
            )
        assertThat(respons.statusCode.value()).isEqualTo(202)
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
            localhostUrl("/api/iverksetting/tilleggsstønader"),
            HttpMethod.POST,
            HttpEntity(dto, headers),
        ).also {
            assertEquals(HttpStatus.ACCEPTED, it.statusCode)
        }

        kjørTasks()

        restTemplate.exchange<IverksettStatus>(
            localhostUrl("/api/iverksetting/${dto.sakId.somUUID}/${dto.behandlingId.somUUID}/${dto.iverksettingId}/status"),
            HttpMethod.GET,
            HttpEntity(null, headers),
        ).also {
            assertEquals(HttpStatus.OK, it.statusCode)
            assertEquals(IverksettStatus.SENDT_TIL_OPPDRAG, it.body)
        }
    }

    @Test
    fun `start iverksetting av rammevedtak uten utbetaling`() {
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

        val rammevedtak = dto.copy(vedtak = dto.vedtak.copy(utbetalinger = emptyList()))

        restTemplate.exchange<Unit>(
            localhostUrl("/api/iverksetting"),
            HttpMethod.POST,
            HttpEntity(rammevedtak, headers),
        ).also {
            assertEquals(HttpStatus.ACCEPTED, it.statusCode)
        }

        kjørTasks()

        restTemplate.exchange<IverksettStatus>(
            localhostUrl("/api/iverksetting/${sakId.somUUID}/${behandlingId.somUUID}/status"),
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
