package no.nav.dagpenger.iverksett.api

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import no.nav.dagpenger.iverksett.api.tilgangskontroll.IverksettingTilgangskontrollService
import no.nav.dagpenger.iverksett.api.tilgangskontroll.TokenContext
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.infrastruktur.util.opprettIverksettDto
import no.nav.dagpenger.iverksett.konsumenter.økonomi.OppdragClient
import no.nav.dagpenger.iverksett.lagIverksett
import no.nav.dagpenger.iverksett.lagIverksettData
import no.nav.dagpenger.iverksett.util.mockFeatureToggleService
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.http.HttpStatus

class IverksettingTilgangskontrollServiceTest {

    private val taskService = mockk<TaskService>()
    private val iverksettResultatService = mockk<IverksettResultatService>()
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val oppdragClient = mockk<OppdragClient>()
    private val featureToggleServiceMock = mockk<FeatureToggleService>()
    private val iverksettingServiceMock = IverksettingService(
        taskService = taskService,
        iverksettResultatService = iverksettResultatService,
        iverksettingRepository = iverksettingRepository,
        oppdragClient = oppdragClient,
        featureToggleService = mockFeatureToggleService(),
    )
    private lateinit var iverksettingTilgangskontrollService: IverksettingTilgangskontrollService

    @BeforeEach
    fun setup() {
        iverksettingTilgangskontrollService = IverksettingTilgangskontrollService(
            iverksettingServiceMock,
            featureToggleServiceMock,
            beslutterGruppe,
        )

        every { featureToggleServiceMock.isEnabled(any(),any()) } returns true

    }

    @Test
    fun `skal få OK når rammevedtak sendes av beslutter`() {
        val nåværendeIverksetting = opprettIverksettDto().copy(
            vedtak = opprettIverksettDto().vedtak.copy(vedtakstype = VedtakType.RAMMEVEDTAK),
        )

        every {
            iverksettingRepository.findByFagsakId(nåværendeIverksetting.sakId!!)
        } returns emptyList()

        mockkObject(TokenContext)
        every { TokenContext.hentGrupper() } returns listOf(beslutterGruppe)

        assertDoesNotThrow {
            iverksettingTilgangskontrollService.valider(
                nåværendeIverksetting
            )
        }

        unmockkObject(TokenContext)
    }

    @Test
    fun `skal få FORBIDDEN når første vedtak på sak sendes uten beslutter-token`() {
        val nåværendeIverksetting = opprettIverksettDto()

        every {
            iverksettingRepository.findByFagsakId(
                nåværendeIverksetting.sakId!!
            )
        } returns emptyList()

        assertApiFeil(HttpStatus.FORBIDDEN) {
            iverksettingTilgangskontrollService.valider(nåværendeIverksetting)
        }
    }

    @Test
    fun `skal få OK når utbetalingsvedtak sendes etter autorisert vedtak`() {
        val forrigeIverksetting = lagIverksettData().copy(
            vedtak = lagIverksettData().vedtak.copy(
                vedtakstype = VedtakType.RAMMEVEDTAK,
            ),
        )

        val nåværendeIverksetting = opprettIverksettDto().copy(
            vedtak = opprettIverksettDto().vedtak.copy(vedtakstype = VedtakType.UTBETALINGSVEDTAK),
        )

        val iverksettListe = listOf(lagIverksett(forrigeIverksetting))
        every {
            iverksettingRepository.findByFagsakId(
                nåværendeIverksetting.sakId!!
            )
        } returns iverksettListe

        assertDoesNotThrow {
            iverksettingTilgangskontrollService.valider(nåværendeIverksetting)
        }
    }

    companion object {
        private val beslutterGruppe = "0000-GA-Beslutter"
    }
}
