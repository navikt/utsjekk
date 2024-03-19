package no.nav.dagpenger.iverksett.utbetaling.api

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksettingsresultat
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.domene.behandlingId
import no.nav.dagpenger.iverksett.utbetaling.domene.personident
import no.nav.dagpenger.iverksett.utbetaling.domene.sakId
import no.nav.dagpenger.iverksett.utbetaling.domene.tilAndelData
import no.nav.dagpenger.iverksett.utbetaling.lagIverksettingsdata
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingService
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingsresultatService
import no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.Utbetalingsgenerator
import no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.domene.Behandlingsinformasjon
import no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.domene.BeregnetUtbetalingsoppdrag
import no.nav.dagpenger.iverksett.utbetaling.util.mai
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
        iverksettingValidatorService =
            IverksettingValidatorService(
                iverksettingsresultatServiceMock,
                iverksettingServiceMock,
            )
    }

    @Test
    fun `skal få BAD_REQUEST når forrige iverksetting er knyttet til en annen sak`() {
        val forrigeIverksetting = lagIverksettingsdata()
        val nåværendeIverksetting =
            lagIverksettingsdata(
                forrigeBehandlingId = forrigeIverksetting.behandlingId,
            )
        every { iverksettingServiceMock.hentForrigeIverksetting(nåværendeIverksetting) } returns forrigeIverksetting

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            iverksettingValidatorService.validerAtIverksettingErForSammeSakOgPersonSomForrige(nåværendeIverksetting)
        }
    }

    @Test
    fun `skal få BAD_REQUEST når forrige iverksetting er knyttet til en annen person`() {
        val forrigeIverksetting = lagIverksettingsdata()
        val iverksettingTmp = lagIverksettingsdata()
        val nåværendeIverksetting =
            iverksettingTmp.copy(
                fagsak = forrigeIverksetting.fagsak,
                søker = iverksettingTmp.søker.copy(personident = "12345678911"),
            )
        every { iverksettingServiceMock.hentForrigeIverksetting(nåværendeIverksetting) } returns forrigeIverksetting

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            iverksettingValidatorService.validerAtIverksettingErForSammeSakOgPersonSomForrige(nåværendeIverksetting)
        }
    }

    @Test
    fun `skal få CONFLICT når iverksetting allerede er mottatt`() {
        val iverksetting = lagIverksettingsdata()

        // Burde ikke få samme
        every {
            iverksettingServiceMock.hentIverksetting(
                fagsystem = iverksetting.fagsak.fagsystem,
                sakId = iverksetting.sakId,
                behandlingId = iverksetting.behandlingId,
            )
        } returns iverksetting

        assertApiFeil(HttpStatus.CONFLICT) {
            iverksettingValidatorService.validerAtIverksettingIkkeAlleredeErMottatt(iverksetting)
        }
    }

    @Test
    fun `skal få CONFLICT når forrige iverksetting ikke er ferdig og OK mot oppdrag`() {
        val forrigeIverksetting =
            lagIverksettingsdata(
                andelsdatoer = listOf(1.mai, 2.mai),
                beløp = 300,
            )
        val nåværendeIverksetting =
            lagIverksettingsdata(
                sakId = forrigeIverksetting.sakId,
                forrigeBehandlingId = forrigeIverksetting.behandlingId,
            )

        val beregnetUtbetalingsoppdrag = beregnUtbetalingsoppdrag(forrigeIverksetting)
        val forrigeIverksettingsresultat =
            Iverksettingsresultat(
                fagsystem = forrigeIverksetting.fagsak.fagsystem,
                sakId = forrigeIverksetting.sakId,
                behandlingId = forrigeIverksetting.behandlingId,
                iverksettingId = forrigeIverksetting.behandling.iverksettingId,
                tilkjentYtelseForUtbetaling =
                    forrigeIverksetting.vedtak.tilkjentYtelse.copy(
                        utbetalingsoppdrag = beregnetUtbetalingsoppdrag.utbetalingsoppdrag,
                    ),
                oppdragResultat = OppdragResultat(OppdragStatus.LAGT_PÅ_KØ),
            )

        every {
            iverksettingsresultatServiceMock.hentIverksettingsresultat(
                forrigeIverksettingsresultat.fagsystem,
                forrigeIverksettingsresultat.sakId,
                forrigeIverksettingsresultat.behandlingId,
                forrigeIverksettingsresultat.iverksettingId,
            )
        } returns forrigeIverksettingsresultat

        assertApiFeil(HttpStatus.CONFLICT) {
            iverksettingValidatorService.validerAtForrigeBehandlingErFerdigIverksattMotOppdrag(nåværendeIverksetting)
        }
    }

    private fun beregnUtbetalingsoppdrag(iverksettingData: Iverksetting): BeregnetUtbetalingsoppdrag {
        val behandlingsinformasjon =
            Behandlingsinformasjon(
                saksbehandlerId = iverksettingData.vedtak.saksbehandlerId,
                beslutterId = iverksettingData.vedtak.beslutterId,
                fagsystem = iverksettingData.fagsak.fagsystem,
                fagsakId = iverksettingData.sakId,
                behandlingId = iverksettingData.behandlingId,
                personident = iverksettingData.personident,
                vedtaksdato = iverksettingData.vedtak.vedtakstidspunkt.toLocalDate(),
                iverksettingId = iverksettingData.behandling.iverksettingId,
            )

        return Utbetalingsgenerator.lagUtbetalingsoppdrag(
            behandlingsinformasjon = behandlingsinformasjon,
            nyeAndeler = iverksettingData.vedtak.tilkjentYtelse.andelerTilkjentYtelse.map { it.tilAndelData() },
            forrigeAndeler = emptyList(),
            sisteAndelPerKjede = iverksettingData.vedtak.tilkjentYtelse.sisteAndelPerKjede.mapValues { it.value.tilAndelData() },
        )
    }
}
