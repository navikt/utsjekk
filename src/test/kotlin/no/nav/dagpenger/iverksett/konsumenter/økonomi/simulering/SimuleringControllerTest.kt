package no.nav.dagpenger.iverksett.konsumenter.økonomi.simulering

import io.mockk.clearMocks
import io.mockk.verify
import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.api.domene.tilAndelData
import no.nav.dagpenger.iverksett.api.domene.tilBehandlingsinformasjon
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.beriketSimuleringsresultat
import no.nav.dagpenger.iverksett.infrastruktur.util.opprettTilkjentYtelse
import no.nav.dagpenger.iverksett.infrastruktur.util.opprettTilkjentYtelseMedMetadata
import no.nav.dagpenger.iverksett.konsumenter.økonomi.OppdragClient
import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagUtbetalingDto
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.Utbetalingsgenerator
import no.nav.dagpenger.iverksett.simuleringDto
import no.nav.dagpenger.kontrakter.oppdrag.simulering.BeriketSimuleringsresultat
import no.nav.dagpenger.kontrakter.oppdrag.simulering.DetaljertSimuleringResultat
import no.nav.familie.kontrakter.felles.Ressurs
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import java.time.LocalDate
import java.util.UUID

class SimuleringControllerTest : ServerTest() {

    @Autowired
    private lateinit var iverksettResultatService: IverksettResultatService

    @Autowired
    private lateinit var oppdragClient: OppdragClient

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @AfterEach
    internal fun tearDown() {
        clearMocks(oppdragClient, answers = false)
    }

    @Test
    internal fun `Hent simulering skal gi 200 OK`() {
        val respons = restTemplate
            .exchange<Ressurs<BeriketSimuleringsresultat>>(
                localhostUrl("/api/simulering"),
                HttpMethod.POST,
                HttpEntity(simuleringDto(), headers),
            )
        assertThat(respons.statusCode.value()).isEqualTo(200)
        assertThat(respons.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(respons.body?.data).isEqualTo(beriketSimuleringsresultat())
        verify(exactly = 1) { oppdragClient.hentSimuleringsresultat(any()) }
    }

    @Test
    internal fun `simulering av førstegangsbehandling skal gi tomt svar`() {
        val request = simuleringDto(andeler = emptyList(), forrigeBehandlingId = null)
        val respons = restTemplate
            .exchange<Ressurs<BeriketSimuleringsresultat>>(
                localhostUrl("/api/simulering"),
                HttpMethod.POST,
                HttpEntity(request, headers),
            )

        assertThat(respons.statusCode.value()).isEqualTo(200)
        assertThat(respons.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(respons.body?.data).isEqualTo(lagSimuleringsresultatMedTomListe())
        verify(exactly = 0) { oppdragClient.hentSimuleringsresultat(any()) }
    }

    @Test
    @Disabled("Vi tillater ikke 0-beløp per nå. Besøker denne på nytt når vi kommer til simulering")
    internal fun `simulering av førstegangsbehandling med kun 0 beløp skal gi tomt svar`() {
        val request = simuleringDto(andeler = listOf(lagUtbetalingDto(beløp = 0)), forrigeBehandlingId = null)
        val respons = restTemplate
            .exchange<Ressurs<BeriketSimuleringsresultat>>(
                localhostUrl("/api/simulering"),
                HttpMethod.POST,
                HttpEntity(request, headers),
            )

        assertThat(respons.statusCode.value()).isEqualTo(200)
        assertThat(respons.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(respons.body?.data).isEqualTo(lagSimuleringsresultatMedTomListe())
        verify(exactly = 0) { oppdragClient.hentSimuleringsresultat(any()) }
    }

    @Test
    @Disabled("Vi tillater ikke 0-beløp per nå. Besøker denne på nytt når vi kommer til simulering")
    internal fun `simulering av revurdering hvor førstegangsbehandling hadde kun 0 beløp skal gi svar`() {
        val behandlingId = UUID.randomUUID()
        lagFørstegangsbehandlingUtenBeløp(behandlingId)

        val revurdering =
            simuleringDto(andeler = listOf(lagUtbetalingDto(beløp = 1000)), forrigeBehandlingId = behandlingId)

        val response = restTemplate.exchange<Ressurs<BeriketSimuleringsresultat>>(
            localhostUrl("/api/simulering"),
            HttpMethod.POST,
            HttpEntity(revurdering, headers),
        )

        assertThat(response.body?.data?.detaljer?.simuleringMottaker).isNotEmpty
        verify(exactly = 1) { oppdragClient.hentSimuleringsresultat(any()) }
    }

    @Test
    @Disabled("Vi tillater ikke 0-beløp per nå. Besøker denne på nytt når vi kommer til simulering")
    internal fun `simulering av revurdering med 0 i beløp hvor førstegangsbehandling også hadde kun 0 beløp skal gi svar`() {
        val behandlingId = UUID.randomUUID()
        lagFørstegangsbehandlingUtenBeløp(behandlingId)

        val revurdering =
            simuleringDto(andeler = listOf(lagUtbetalingDto(beløp = 0)), forrigeBehandlingId = behandlingId)

        val respons = restTemplate.exchange<Ressurs<BeriketSimuleringsresultat>>(
            localhostUrl("/api/simulering"),
            HttpMethod.POST,
            HttpEntity(revurdering, headers),
        )

        assertThat(respons.body?.data).isEqualTo(lagSimuleringsresultatMedTomListe())
        verify(exactly = 0) { oppdragClient.hentSimuleringsresultat(any()) }
    }

    private fun lagFørstegangsbehandlingUtenBeløp(behandlingId: UUID) {
        val andelTilkjentYtelse =
            lagAndelTilkjentYtelse(0, fraOgMed = LocalDate.of(2021, 1, 1), tilOgMed = LocalDate.of(2021, 1, 31))
        val tilkjentYtelseMedMetadata = opprettTilkjentYtelseMedMetadata(behandlingId, opprettTilkjentYtelse(behandlingId, andeler = listOf(andelTilkjentYtelse)))
        val beregnetUtbetalingsoppdrag = Utbetalingsgenerator.lagUtbetalingsoppdrag(
            behandlingsinformasjon = tilkjentYtelseMedMetadata.tilBehandlingsinformasjon(),
            nyeAndeler = tilkjentYtelseMedMetadata.tilkjentYtelse.andelerTilkjentYtelse.map { it.tilAndelData() },
            forrigeAndeler = emptyList(),
            sisteAndelPerKjede = emptyMap(),
        )
        val tilkjentYtelseMedUtbetalingsoppdrag = tilkjentYtelseMedMetadata.tilkjentYtelse.copy(
            utbetalingsoppdrag = beregnetUtbetalingsoppdrag.utbetalingsoppdrag,
        )

        iverksettResultatService.opprettTomtResultat(behandlingId)
        iverksettResultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelseMedUtbetalingsoppdrag)
    }

    private fun lagSimuleringsresultatMedTomListe(): BeriketSimuleringsresultat {
        val defaultSimuleringsresultat = DetaljertSimuleringResultat(emptyList())
        val oppsummering = lagSimuleringsoppsummering(defaultSimuleringsresultat, LocalDate.now())
        return BeriketSimuleringsresultat(defaultSimuleringsresultat, oppsummering)
    }
}
