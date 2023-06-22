package no.nav.dagpenger.iverksett.api

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.iverksett.api.domene.behandlingId
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.lagIverksettData
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class IverksettingValidatorServiceTest {

    private val iverksettResultatServiceMock = mockk<IverksettResultatService>()
    private val iverksettingServiceMock = mockk<IverksettingService>()
    private val featureToggleServiceMock = mockk<FeatureToggleService>()
    private lateinit var iverksettingValidatorService: IverksettingValidatorService

    @BeforeEach
    fun setup() {
        iverksettingValidatorService = IverksettingValidatorService(
            iverksettResultatServiceMock,
            iverksettingServiceMock,
            featureToggleServiceMock,
        )
    }

    @Test
    fun `skal få BAD_REQUEST når forrige iverksetting er knyttet til en annen sak`() {
        val forrigeIverksetting = lagIverksettData()
        val nåværendeIverksetting = lagIverksettData(
            forrigeBehandlingId = forrigeIverksetting.behandlingId,
        )
        every { iverksettingServiceMock.hentForrigeIverksett(nåværendeIverksetting) } returns forrigeIverksetting

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            iverksettingValidatorService.validerAtIverksettingErForSammeSakOgPersonSomForrige(nåværendeIverksetting)
        }
    }

    @Test
    fun `skal få BAD_REQUEST når forrige iverksetting er knyttet til en annen person`() {
        val forrigeIverksetting = lagIverksettData()
        val iverksettingTmp = lagIverksettData()
        val nåværendeIverksetting = iverksettingTmp.copy(
            fagsak = forrigeIverksetting.fagsak,
            forrigeIverksetting = forrigeIverksetting,
            søker = iverksettingTmp.søker.copy(personIdent = "12345678911"),
        )
        every { iverksettingServiceMock.hentForrigeIverksett(nåværendeIverksetting) } returns forrigeIverksetting

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            iverksettingValidatorService.validerAtIverksettingErForSammeSakOgPersonSomForrige(nåværendeIverksetting)
        }
    }

    @Test
    fun `skal få CONFLICT når iverksetting allerede er mottatt`() {
        val iverksetting = lagIverksettData()

        // Burde ikke få samme
        every { iverksettingServiceMock.hentIverksetting(iverksetting.behandlingId) } returns iverksetting

        assertApiFeil(HttpStatus.CONFLICT) {
            iverksettingValidatorService.validerAtBehandlingIkkeAlleredeErMottatt(iverksetting)
        }
    }
}
