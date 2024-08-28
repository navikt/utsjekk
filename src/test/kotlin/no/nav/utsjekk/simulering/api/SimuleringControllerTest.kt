package no.nav.utsjekk.simulering.api

import no.nav.utsjekk.Integrasjonstest
import no.nav.utsjekk.iverksetting.util.enIverksettV2Dto
import no.nav.utsjekk.iverksetting.util.enUtbetalingV2Dto
import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.felles.StønadTypeTilleggsstønader
import no.nav.utsjekk.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.utsjekk.kontrakter.iverksett.ForrigeIverksettingV2Dto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTilleggsstønaderDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTiltakspengerV2Dto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.time.LocalDate
import java.util.UUID

class SimuleringControllerTest : Integrasjonstest() {
    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken())
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }

    @Test
    fun `400 ved endring på ikke-eksisterende utbetaling`() {
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

        assertEquals(HttpStatus.BAD_REQUEST, respons.statusCode)
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
                        StønadTypeTiltakspenger.ARBEIDSTRENING,
                        false,
                        "4400",
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
        val forrigeIverksetting =
            enIverksettV2Dto(
                behandlingId = forrigeBehandlingId,
                sakId = "en-sakid",
                iverksettingId = forrigeIverksettingId,
            )
        restTemplate.exchange<Any>(
            localhostUrl("/api/iverksetting/v2"),
            HttpMethod.POST,
            HttpEntity(forrigeIverksetting, headers),
        )

        val body =
            enSimuleringRequestV2Dto(
                stønadsdataDto =
                    StønadsdataTilleggsstønaderDto(
                        stønadstype = StønadTypeTilleggsstønader.TILSYN_BARN_ETTERLATTE,
                    ),
            ).copy(
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

    private fun enSimuleringRequestV2Dto(stønadsdataDto: StønadsdataDto) =
        SimuleringRequestV2Dto(
            sakId = "en-sakid",
            behandlingId = "en-behandlingId",
            personident = Personident("15507600333"),
            saksbehandlerId = "A123456",
            utbetalinger =
                listOf(
                    enUtbetalingV2Dto(
                        beløp = 100u,
                        fraOgMed = LocalDate.of(2020, 1, 1),
                        tilOgMed = LocalDate.of(2020, 1, 31),
                        stønadsdata = stønadsdataDto,
                    ),
                ),
        )
}
