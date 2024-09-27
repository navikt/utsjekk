package no.nav.utsjekk.simulering.api

import no.nav.familie.prosessering.internal.TaskService
import no.nav.utsjekk.Integrasjonstest
import no.nav.utsjekk.iverksetting.task.IverksettMotOppdragTask
import no.nav.utsjekk.iverksetting.util.enIverksettV2Dto
import no.nav.utsjekk.kontrakter.felles.StønadTypeTilleggsstønader
import no.nav.utsjekk.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.utsjekk.kontrakter.iverksett.ForrigeIverksettingV2Dto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTilleggsstønaderDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTiltakspengerV2Dto
import no.nav.utsjekk.simulering.enSimuleringRequestV2Dto
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
import java.util.UUID

class SimuleringControllerTest : Integrasjonstest() {
    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var iverksettMotOppdragTask: IverksettMotOppdragTask

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken())
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }

    @Test
    fun `409 ved endring på ikke-eksisterende utbetaling`() {
        val body =
            enSimuleringRequestV2Dto(stønadsdataDto = StønadsdataTilleggsstønaderDto(StønadTypeTilleggsstønader.TILSYN_BARN_AAP))
                .copy(
                    forrigeIverksetting =
                        ForrigeIverksettingV2Dto(
                            behandlingId = "noe-tull",
                            iverksettingId = "noe-tull",
                        ),
                )

        val respons: ResponseEntity<Any> =
            restTemplate.exchange(
                localhostUrl("/api/simulering/v2"),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )

        assertEquals(HttpStatus.CONFLICT, respons.statusCode)
    }

    @Test
    fun `204 ved simulering av tom utbetaling som ikke er opphør`() {
        val body =
            enSimuleringRequestV2Dto(
                stønadsdataDto = StønadsdataTilleggsstønaderDto(stønadstype = StønadTypeTilleggsstønader.TILSYN_BARN_AAP),
            ).copy(
                utbetalinger = emptyList(),
            )

        val respons: ResponseEntity<Any> =
            restTemplate.exchange(
                localhostUrl("/api/simulering/v2"),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )

        assertEquals(HttpStatus.NO_CONTENT, respons.statusCode)
    }

    @Test
    fun `Simulerer for tiltakspenger`() {
        val body =
            enSimuleringRequestV2Dto(
                stønadsdataDto =
                    StønadsdataTiltakspengerV2Dto(
                        stønadstype = StønadTypeTiltakspenger.ARBEIDSTRENING,
                        barnetillegg = false,
                        brukersNavKontor = "4400",
                        meldekortId = "M1",
                    ),
            )

        val respons: ResponseEntity<Any> =
            restTemplate.exchange(
                localhostUrl("/api/simulering/v2"),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )

        assertEquals(HttpStatus.OK, respons.statusCode)
    }

    @Test
    fun `Simulerer for tilleggsstønader med eksisterende iverksetting`() {
        val forrigeIverksettingId = UUID.randomUUID().toString()
        val forrigeBehandlingId = "forrige-behandling"
        val sakId = "en-sakid"
        val forrigeIverksettingTmp =
            enIverksettV2Dto(
                behandlingId = forrigeBehandlingId,
                sakId = sakId,
                iverksettingId = forrigeIverksettingId,
            )
        val forrigeIverksetting = forrigeIverksettingTmp.copy(vedtak = forrigeIverksettingTmp.vedtak.copy(utbetalinger = emptyList()))
        restTemplate.exchange<Any>(
            localhostUrl("/api/iverksetting/v2"),
            HttpMethod.POST,
            HttpEntity(forrigeIverksetting, headers),
        )
        taskService.findAll().let { tasks ->
            iverksettMotOppdragTask.doTask(tasks.first())
        }

        val body =
            enSimuleringRequestV2Dto(
                stønadsdataDto =
                    StønadsdataTilleggsstønaderDto(
                        stønadstype = StønadTypeTilleggsstønader.TILSYN_BARN_ETTERLATTE,
                    ),
            ).copy(
                sakId = sakId,
                forrigeIverksetting =
                    ForrigeIverksettingV2Dto(
                        behandlingId = forrigeBehandlingId,
                        iverksettingId = forrigeIverksettingId,
                    ),
            )

        val respons: ResponseEntity<Any> =
            restTemplate.exchange(
                localhostUrl("/api/simulering/v2"),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )

        assertEquals(HttpStatus.OK, respons.statusCode)
    }
}
