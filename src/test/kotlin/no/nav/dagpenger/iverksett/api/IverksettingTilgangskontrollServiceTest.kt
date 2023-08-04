package no.nav.dagpenger.iverksett.api

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.PlainJWT
import no.nav.dagpenger.iverksett.lagIverksettData
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.http.HttpStatus

class IverksettingTilgangskontrollServiceTest {

    private lateinit var iverksettingTilgangskontrollService: IverksettingTilgangskontrollService

    @BeforeEach
    fun setup() {
        iverksettingTilgangskontrollService = IverksettingTilgangskontrollService()
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

        assertApiFeil(HttpStatus.CONFLICT) {
            iverksettingTilgangskontrollService.validerAtDetFinnesIverksattRammevedtak(nåværendeIverksetting)
        }
    }

    @Test
    fun `skal få OK når utbetaingsvedtak sendes med rammevedtak`() {
        val forrigeIverksetting = lagIverksettData()
        val iverksettingTmp = lagIverksettData()
        val nåværendeIverksetting = iverksettingTmp.copy(
            vedtak = iverksettingTmp.vedtak.copy(vedtakstype = VedtakType.UTBETALINGSVEDTAK),
            fagsak = forrigeIverksetting.fagsak,
            forrigeIverksetting = forrigeIverksetting.copy(
                vedtak = forrigeIverksetting.vedtak.copy(
                    vedtakstype = VedtakType.RAMMEVEDTAK
                ),
            ),
        )

        assertDoesNotThrow {
            iverksettingTilgangskontrollService.validerAtDetFinnesIverksattRammevedtak(nåværendeIverksetting)
        }
    }
}
