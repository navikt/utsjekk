package no.nav.dagpenger.iverksett.utbetaling.api.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.dagpenger.iverksett.utbetaling.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.utbetaling.api.TokenContext
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingsresultatService
import no.nav.dagpenger.iverksett.utbetaling.util.opprettIverksettDto
import no.nav.dagpenger.iverksett.felles.oppdrag.OppdragClient
import no.nav.dagpenger.iverksett.utbetaling.lagIverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.lagIverksettingsdata
import no.nav.dagpenger.iverksett.utbetaling.api.IverksettingTilgangskontrollService
import no.nav.dagpenger.iverksett.utbetaling.api.assertApiFeil
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingRepository
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.iverksett.util.mockFeatureToggleService
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.http.HttpStatus

class IverksettingTilgangskontrollServiceTest {

    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val featureToggleServiceMock = mockk<FeatureToggleService>()
    private val iverksettingServiceMock = IverksettingService(
        taskService = mockk<TaskService>(),
        iverksettingsresultatService = mockk<IverksettingsresultatService>(),
        iverksettingRepository = iverksettingRepository,
        oppdragClient = mockk<OppdragClient>(),
        featureToggleService = mockFeatureToggleService(),
    )
    private lateinit var iverksettingTilgangskontrollService: IverksettingTilgangskontrollService

    @BeforeEach
    fun setup() {
        iverksettingTilgangskontrollService = IverksettingTilgangskontrollService(
            iverksettingServiceMock,
            featureToggleServiceMock,
            BESLUTTERGRUPPE,
            APP_MED_SYSTEMTILGANG
        )

        every { featureToggleServiceMock.isEnabled(any(), any()) } returns true
        mockkObject(TokenContext)
    }

    @AfterEach
    fun cleanup() {
        unmockkObject(TokenContext)
    }

    @Test
    fun `skal få OK når rammevedtak sendes av beslutter`() {
        val nåværendeIverksetting = iverksetting()

        every { iverksettingRepository.findByFagsakId(any()) } returns emptyList()
        every { TokenContext.hentGrupper() } returns listOf(BESLUTTERGRUPPE)
        every { TokenContext.erSystemtoken() } returns false

        assertDoesNotThrow {
            iverksettingTilgangskontrollService.valider(nåværendeIverksetting)
        }
    }

    @Test
    fun `skal få FORBIDDEN når første vedtak på sak sendes uten beslutter-token`() {
        val nåværendeIverksetting = opprettIverksettDto()

        every { iverksettingRepository.findByFagsakId(any()) } returns emptyList()
        every { TokenContext.erSystemtoken() } returns true

        assertApiFeil(HttpStatus.FORBIDDEN) {
            iverksettingTilgangskontrollService.valider(nåværendeIverksetting)
        }
    }

    @Test
    fun `skal få OK når utbetalingsvedtak sendes etter autorisert vedtak`() {
        val forrigeIverksetting = iverksettData()
        val nåværendeIverksetting = iverksetting()
        val iverksettListe = listOf(lagIverksettingEntitet(forrigeIverksetting))

        every { iverksettingRepository.findByFagsakId(any()) } returns iverksettListe
        every { TokenContext.erSystemtoken() } returns true
        every { TokenContext.hentKlientnavn() } returns APP_MED_SYSTEMTILGANG

        assertDoesNotThrow {
            iverksettingTilgangskontrollService.valider(nåværendeIverksetting)
        }
    }

    @Test
    fun `skal få FORBIDDEN når utbetalingsvedtak sendes av ukjent system`() {
        val forrigeIverksetting = iverksettData()
        val nåværendeIverksetting = iverksetting()
        val iverksettListe = listOf(lagIverksettingEntitet(forrigeIverksetting))

        every { iverksettingRepository.findByFagsakId(any()) } returns iverksettListe
        every { TokenContext.erSystemtoken() } returns true
        every { TokenContext.hentKlientnavn() } returns "ukjent app"

        assertApiFeil(HttpStatus.FORBIDDEN) {
            iverksettingTilgangskontrollService.valider(nåværendeIverksetting)
        }
    }

    private fun iverksettData() = lagIverksettingsdata()

    private fun iverksetting() = opprettIverksettDto()

    companion object {
        private const val BESLUTTERGRUPPE = "0000-GA-Beslutter"
        private const val APP_MED_SYSTEMTILGANG = "dev-gcp:teamdagpenger:dp-vedtak-iverksett"
    }
}
