package no.nav.dagpenger.iverksett.utbetaling.api.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.dagpenger.iverksett.felles.oppdrag.OppdragClient
import no.nav.dagpenger.iverksett.utbetaling.api.IverksettingTilgangskontrollService
import no.nav.dagpenger.iverksett.utbetaling.api.TokenContext
import no.nav.dagpenger.iverksett.utbetaling.api.assertApiFeil
import no.nav.dagpenger.iverksett.utbetaling.domene.Grupper
import no.nav.dagpenger.iverksett.utbetaling.domene.IverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.domene.Konsument
import no.nav.dagpenger.iverksett.utbetaling.domene.KonsumentConfig
import no.nav.dagpenger.iverksett.utbetaling.domene.transformer.IverksettDtoMapper
import no.nav.dagpenger.iverksett.utbetaling.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.utbetaling.lagIverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.lagIverksettingsdata
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingRepository
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingsresultatService
import no.nav.dagpenger.iverksett.utbetaling.util.enIverksettDto
import no.nav.dagpenger.iverksett.util.mockFeatureToggleService
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.somUUID
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.http.HttpStatus

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IverksettingTilgangskontrollServiceTest {
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val featureToggleServiceMock = mockk<FeatureToggleService>()
    private val iverksettingServiceMock =
        IverksettingService(
            taskService = mockk<TaskService>(),
            iverksettingsresultatService = mockk<IverksettingsresultatService>(),
            iverksettingRepository = iverksettingRepository,
            oppdragClient = mockk<OppdragClient>(),
            featureToggleService = mockFeatureToggleService(),
        )
    private val konsumentConfig = mockk<KonsumentConfig>()
    private val iverksettDtoMapper = IverksettDtoMapper(konsumentConfig)

    private lateinit var iverksettingTilgangskontrollService: IverksettingTilgangskontrollService

    @BeforeAll
    fun initialize() {
        every { konsumentConfig.finnFagsystem(any()) } returns Fagsystem.DAGPENGER
        every { konsumentConfig.configForFagsystem(any()) } returns
            Konsument(
                fagsystem = Fagsystem.DAGPENGER,
                klientapp = APP_MED_SYSTEMTILGANG,
                grupper = Grupper(beslutter = BESLUTTERGRUPPE),
            )
    }

    @BeforeEach
    fun setup() {
        iverksettingTilgangskontrollService =
            IverksettingTilgangskontrollService(
                iverksettingServiceMock,
                featureToggleServiceMock,
                konsumentConfig,
            )

        every { featureToggleServiceMock.isEnabled(any(), any()) } returns true
        mockkObject(TokenContext)
        every { TokenContext.hentKlientnavn() } returns "dp-vedtak-iverksett"
    }

    @AfterEach
    fun cleanup() {
        unmockkObject(TokenContext)
    }

    @Test
    fun `skal få OK når rammevedtak sendes av beslutter`() {
        val nåværendeIverksetting = enIverksettDto()

        every { iverksettingRepository.findByFagsakId(any()) } returns emptyList()
        every { TokenContext.hentGrupper() } returns listOf(BESLUTTERGRUPPE)
        every { TokenContext.erSystemtoken() } returns false

        assertDoesNotThrow {
            iverksettingTilgangskontrollService.valider(iverksettDtoMapper.tilDomene(nåværendeIverksetting).fagsak)
        }
    }

    @Test
    fun `skal få FORBIDDEN når første vedtak på sak sendes uten beslutter-token`() {
        val nåværendeIverksetting = enIverksettDto()

        every { iverksettingRepository.findByFagsakId(any()) } returns emptyList()
        every { TokenContext.erSystemtoken() } returns true

        assertApiFeil(HttpStatus.FORBIDDEN) {
            iverksettingTilgangskontrollService.valider(iverksettDtoMapper.tilDomene(nåværendeIverksetting).fagsak)
        }
    }

    @Test
    fun `skal få FORBIDDEN når første vedtak på sak sendes uten beslutter-token og samme sakId finnes for annet fagsystem`() {
        val nåværendeIverksetting = enIverksettDto()
        val eksisterendeIverksettingTiltakspenger =
            lagIverksettingsdata(fagsystem = Fagsystem.TILTAKSPENGER, sakId = nåværendeIverksetting.sakId.somUUID)

        every { iverksettingRepository.findByFagsakId(any()) } returns
            listOf(
                IverksettingEntitet(
                    behandlingId = eksisterendeIverksettingTiltakspenger.behandling.behandlingId.somUUID,
                    data = eksisterendeIverksettingTiltakspenger,
                ),
            )
        every { TokenContext.erSystemtoken() } returns true

        assertApiFeil(HttpStatus.FORBIDDEN) {
            iverksettingTilgangskontrollService.valider(iverksettDtoMapper.tilDomene(nåværendeIverksetting).fagsak)
        }
    }

    @Test
    fun `skal få OK når utbetalingsvedtak sendes etter autorisert vedtak`() {
        val forrigeIverksetting = lagIverksettingsdata()
        val nåværendeIverksetting = enIverksettDto()
        val iverksettListe = listOf(lagIverksettingEntitet(forrigeIverksetting))

        every { iverksettingRepository.findByFagsakId(any()) } returns iverksettListe
        every { TokenContext.erSystemtoken() } returns true
        every { TokenContext.hentKlientnavn() } returns APP_MED_SYSTEMTILGANG

        assertDoesNotThrow {
            iverksettingTilgangskontrollService.valider(iverksettDtoMapper.tilDomene(nåværendeIverksetting).fagsak)
        }
    }

    @Test
    fun `skal få FORBIDDEN når utbetalingsvedtak sendes av ukjent system`() {
        val forrigeIverksetting = lagIverksettingsdata()
        val iverksettListe = listOf(lagIverksettingEntitet(forrigeIverksetting))

        every { iverksettingRepository.findByFagsakId(any()) } returns iverksettListe
        every { TokenContext.erSystemtoken() } returns true
        every { TokenContext.hentKlientnavn() } returns "ukjent app"

        assertApiFeil(HttpStatus.FORBIDDEN) {
            iverksettingTilgangskontrollService.valider(lagIverksettingsdata().fagsak)
        }
    }

    companion object {
        private const val BESLUTTERGRUPPE = "0000-GA-Test-Beslutter"
        private const val APP_MED_SYSTEMTILGANG = "dp-vedtak-iverksett"
    }
}
