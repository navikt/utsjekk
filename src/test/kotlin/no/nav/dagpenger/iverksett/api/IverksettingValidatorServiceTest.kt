package no.nav.dagpenger.iverksett.api

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.iverksett.api.domene.Iverksetting
import no.nav.dagpenger.iverksett.api.domene.Iverksettingsresultat
import no.nav.dagpenger.iverksett.api.domene.OppdragResultat
import no.nav.dagpenger.iverksett.api.domene.behandlingId
import no.nav.dagpenger.iverksett.api.domene.personIdent
import no.nav.dagpenger.iverksett.api.domene.sakId
import no.nav.dagpenger.iverksett.api.domene.tilAndelData
import no.nav.dagpenger.iverksett.api.tilstand.IverksettingsresultatService
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.Utbetalingsgenerator
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.Behandlingsinformasjon
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.BeregnetUtbetalingsoppdrag
import no.nav.dagpenger.iverksett.lagIverksettingsdata
import no.nav.dagpenger.iverksett.mai
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class IverksettingValidatorServiceTest {

    private val iverksettingsresultatServiceMock = mockk<IverksettingsresultatService>()
    private val iverksettingServiceMock = mockk<IverksettingService>()
    private lateinit var iverksettingValidatorService: IverksettingValidatorService

    @BeforeEach
    fun setup() {
        iverksettingValidatorService = IverksettingValidatorService(
            iverksettingsresultatServiceMock,
            iverksettingServiceMock,
        )
    }

    @Test
    fun `skal få BAD_REQUEST når forrige iverksetting er knyttet til en annen sak`() {
        val forrigeIverksetting = lagIverksettingsdata()
        val nåværendeIverksetting = lagIverksettingsdata(
            forrigeBehandlingId = forrigeIverksetting.behandlingId,
        )
        every { iverksettingServiceMock.hentForrigeIverksett(nåværendeIverksetting) } returns forrigeIverksetting

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            iverksettingValidatorService.validerAtIverksettingErForSammeSakOgPersonSomForrige(nåværendeIverksetting)
        }
    }

    @Test
    fun `skal få BAD_REQUEST når forrige iverksetting er knyttet til en annen person`() {
        val forrigeIverksetting = lagIverksettingsdata()
        val iverksettingTmp = lagIverksettingsdata()
        val nåværendeIverksetting = iverksettingTmp.copy(
            fagsak = forrigeIverksetting.fagsak,
            forrigeIverksettingBehandlingId = forrigeIverksetting.behandlingId,
            søker = iverksettingTmp.søker.copy(personIdent = "12345678911"),
        )
        every { iverksettingServiceMock.hentForrigeIverksett(nåværendeIverksetting) } returns forrigeIverksetting

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            iverksettingValidatorService.validerAtIverksettingErForSammeSakOgPersonSomForrige(nåværendeIverksetting)
        }
    }

    @Test
    fun `skal få CONFLICT når iverksetting allerede er mottatt`() {
        val iverksetting = lagIverksettingsdata()

        // Burde ikke få samme
        every { iverksettingServiceMock.hentIverksetting(iverksetting.behandlingId) } returns iverksetting

        assertApiFeil(HttpStatus.CONFLICT) {
            iverksettingValidatorService.validerAtBehandlingIkkeAlleredeErMottatt(iverksetting)
        }
    }

    @Test
    fun `skal få CONFLICT når forrige iverksetting ikke er ferdig og OK mot oppdrag`() {
        val forrigeIverksetting = lagIverksettingsdata(
            andelsdatoer = listOf(1.mai(2023), 2.mai(2023)),
            beløp = 300,
        )
        val nåværendeIverksetting = lagIverksettingsdata(
            forrigeBehandlingId = forrigeIverksetting.behandlingId,
        )

        val beregnetUtbetalingsoppdrag = beregnUtbetalingsoppdrag(forrigeIverksetting)
        val forrigeIverksettingsresultat = Iverksettingsresultat(
            behandlingId = forrigeIverksetting.behandlingId,
            tilkjentYtelseForUtbetaling = forrigeIverksetting.vedtak.tilkjentYtelse.copy(
                utbetalingsoppdrag = beregnetUtbetalingsoppdrag.utbetalingsoppdrag
            ),
            oppdragResultat = OppdragResultat(OppdragStatus.LAGT_PAA_KOE),
        )

        every { iverksettingsresultatServiceMock.hentIverksettResultat(forrigeIverksettingsresultat.behandlingId) } returns forrigeIverksettingsresultat

        assertApiFeil(HttpStatus.CONFLICT) {
            iverksettingValidatorService.validerAtForrigeBehandlingErFerdigIverksattMotOppdrag(nåværendeIverksetting)
        }
    }

    private fun beregnUtbetalingsoppdrag(iverksettingData: Iverksetting): BeregnetUtbetalingsoppdrag {
        val behandlingsinformasjon = Behandlingsinformasjon(
            saksbehandlerId = iverksettingData.vedtak.saksbehandlerId,
            fagsakId = iverksettingData.sakId,
            saksreferanse = iverksettingData.fagsak.saksreferanse,
            behandlingId = iverksettingData.behandlingId.toString(),
            personIdent = iverksettingData.personIdent,
            vedtaksdato = iverksettingData.vedtak.vedtakstidspunkt.toLocalDate(),
        )

        return Utbetalingsgenerator.lagUtbetalingsoppdrag(
            behandlingsinformasjon = behandlingsinformasjon,
            nyeAndeler = iverksettingData.vedtak.tilkjentYtelse.andelerTilkjentYtelse.map { it.tilAndelData() },
            forrigeAndeler = emptyList(),
            sisteAndelPerKjede = iverksettingData.vedtak.tilkjentYtelse.sisteAndelPerKjede.mapValues { it.value.tilAndelData() },
        )
    }
}
