package no.nav.dagpenger.iverksett.api

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.IverksettResultat
import no.nav.dagpenger.iverksett.api.domene.OppdragResultat
import no.nav.dagpenger.iverksett.api.domene.behandlingId
import no.nav.dagpenger.iverksett.api.domene.personIdent
import no.nav.dagpenger.iverksett.api.domene.sakId
import no.nav.dagpenger.iverksett.api.domene.tilAndelData
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.Utbetalingsgenerator
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.Behandlingsinformasjon
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.BeregnetUtbetalingsoppdrag
import no.nav.dagpenger.iverksett.lagIverksettData
import no.nav.dagpenger.iverksett.mai
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class IverksettingValidatorServiceTest {

    private val iverksettResultatServiceMock = mockk<IverksettResultatService>()
    private val iverksettingServiceMock = mockk<IverksettingService>()
    private lateinit var iverksettingValidatorService: IverksettingValidatorService

    @BeforeEach
    fun setup() {
        iverksettingValidatorService = IverksettingValidatorService(
            iverksettResultatServiceMock,
            iverksettingServiceMock,
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

    @Test
    fun `skal få CONFLICT når forrige iverksetting ikke er ferdig og OK mot oppdrag`() {
        val forrigeIverksetting = lagIverksettData(
            andelsdatoer = listOf(1.mai(2023), 2.mai(2023)),
            beløp = 300,
        )
        val nåværendeIverksetting = lagIverksettData(
            forrigeIverksetting = forrigeIverksetting,
        )

        val beregnetUtbetalingsoppdrag = beregnUtbetalingsoppdrag(forrigeIverksetting)
        val forrigeIverksettResultat = IverksettResultat(
            behandlingId = forrigeIverksetting.behandlingId,
            tilkjentYtelseForUtbetaling = forrigeIverksetting.vedtak.tilkjentYtelse
                ?.copy(utbetalingsoppdrag = beregnetUtbetalingsoppdrag.utbetalingsoppdrag),
            oppdragResultat = OppdragResultat(OppdragStatus.LAGT_PAA_KOE),
        )

        every { iverksettResultatServiceMock.hentIverksettResultat(forrigeIverksettResultat.behandlingId) } returns forrigeIverksettResultat

        assertApiFeil(HttpStatus.CONFLICT) {
            iverksettingValidatorService.validerAtForrigeBehandlingErFerdigIverksattMotOppdrag(nåværendeIverksetting)
        }
    }

    private fun beregnUtbetalingsoppdrag(iverksettData: IverksettDagpenger): BeregnetUtbetalingsoppdrag {
        val behandlingsinformasjon = Behandlingsinformasjon(
            saksbehandlerId = iverksettData.vedtak.saksbehandlerId,
            fagsakId = iverksettData.sakId,
            saksreferanse = iverksettData.fagsak.saksreferanse,
            behandlingId = iverksettData.behandlingId.toString(),
            personIdent = iverksettData.personIdent,
            vedtaksdato = iverksettData.vedtak.vedtakstidspunkt.toLocalDate(),
        )

        return Utbetalingsgenerator.lagUtbetalingsoppdrag(
            behandlingsinformasjon = behandlingsinformasjon,
            nyeAndeler = iverksettData.vedtak.tilkjentYtelse?.andelerTilkjentYtelse?.map { it.tilAndelData() }
                ?: emptyList(),
            forrigeAndeler = emptyList(),
            sisteAndelPerKjede = iverksettData.vedtak.tilkjentYtelse?.sisteAndelPerKjede?.mapValues { it.value.tilAndelData() }
                ?: emptyMap(),
        )
    }
}
