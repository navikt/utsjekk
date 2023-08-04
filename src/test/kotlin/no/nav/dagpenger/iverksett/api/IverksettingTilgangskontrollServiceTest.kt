package no.nav.dagpenger.iverksett.api

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.PlainJWT
import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
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

    private val iverksettResultatService = mockk<IverksettResultatService>()
    private val taskService = mockk<TaskService>()
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val oppdragClient = mockk<OppdragClient>()
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
        iverksettingTilgangskontrollService = IverksettingTilgangskontrollService(iverksettingServiceMock)
    }

    @Test
    fun `skal få BAD_REQUEST når rammevedtak sendes av ikke beslutter`() {
        val iverksettingTmp = lagIverksettData()
        val nåværendeIverksetting = iverksettingTmp.copy(
            vedtak = iverksettingTmp.vedtak.copy(vedtakstype = VedtakType.RAMMEVEDTAK),
            forrigeIverksetting = null,
        )

        assertApiFeil(HttpStatus.FORBIDDEN) {
            iverksettingTilgangskontrollService.validerAtRammevedtakSendesAvBeslutter(nåværendeIverksetting, "")
        }
    }

    @Test
    fun `skal få OK når rammevedtak sendes av beslutter`() {
        val iverksettingTmp = lagIverksettData()
        val nåværendeIverksetting = iverksettingTmp.copy(
            vedtak = iverksettingTmp.vedtak.copy(vedtakstype = VedtakType.RAMMEVEDTAK),
            forrigeIverksetting = null,
        )

        val beslutterGruppe = "0000-GA-Beslutter"
        System.setProperty("BESLUTTER_GRUPPE", beslutterGruppe)
        val token = PlainJWT(JWTClaimsSet.Builder().claim("groups", arrayOf(beslutterGruppe)).build())
        assertDoesNotThrow {
            iverksettingTilgangskontrollService.validerAtRammevedtakSendesAvBeslutter(
                nåværendeIverksetting,
                token.serialize()
            )
        }
    }

    @Test
    fun `skal få BAD_REQUEST når utbetaingsvedtak sendes uten rammevedtak`() {
        val forrigeIverksetting = lagIverksettData()
        val iverksettingTmp = lagIverksettData()
        val nåværendeIverksetting = iverksettingTmp.copy(
            vedtak = iverksettingTmp.vedtak.copy(vedtakstype = VedtakType.UTBETALINGSVEDTAK),
            fagsak = forrigeIverksetting.fagsak,
            forrigeIverksetting = forrigeIverksetting.copy(
                vedtak = forrigeIverksetting.vedtak.copy(
                    vedtakstype = VedtakType.UTBETALINGSVEDTAK
                ),
            ),
        )

        every { iverksettingRepository.findByFagsakId(nåværendeIverksetting.fagsak.fagsakId) } returns emptyList()

        assertApiFeil(HttpStatus.CONFLICT) {
            iverksettingTilgangskontrollService.validerAtDetFinnesIverksattRammevedtak(nåværendeIverksetting)
        }
    }

    @Test
    fun `skal få OK når utbetaingsvedtak sendes med rammevedtak`() {
        val iverksettingTmp = lagIverksettData()
        val forrigeIverksetting = iverksettingTmp.copy(
            vedtak = iverksettingTmp.vedtak.copy(
                vedtakstype = VedtakType.RAMMEVEDTAK
            ),
        )
        val nåværendeIverksetting = iverksettingTmp.copy(
            vedtak = iverksettingTmp.vedtak.copy(vedtakstype = VedtakType.UTBETALINGSVEDTAK),
            fagsak = forrigeIverksetting.fagsak,
            forrigeIverksetting = forrigeIverksetting,
        )

        val iverksettListe = listOf(lagIverksett(forrigeIverksetting))
        every { iverksettingRepository.findByFagsakId(nåværendeIverksetting.fagsak.fagsakId) } returns iverksettListe

        assertDoesNotThrow {
            iverksettingTilgangskontrollService.validerAtDetFinnesIverksattRammevedtak(nåværendeIverksetting)
        }
    }
}
