package no.nav.utsjekk.iverksetting.api

import io.mockk.every
import io.mockk.mockk
import no.nav.utsjekk.iverksetting.domene.Iverksetting
import no.nav.utsjekk.iverksetting.domene.Iverksettingsresultat
import no.nav.utsjekk.iverksetting.domene.OppdragResultat
import no.nav.utsjekk.iverksetting.domene.behandlingId
import no.nav.utsjekk.iverksetting.domene.personident
import no.nav.utsjekk.iverksetting.domene.sakId
import no.nav.utsjekk.iverksetting.domene.tilAndelData
import no.nav.utsjekk.iverksetting.domene.transformer.RandomOSURId
import no.nav.utsjekk.iverksetting.tilstand.IverksettingService
import no.nav.utsjekk.iverksetting.tilstand.IverksettingsresultatService
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.Utbetalingsgenerator
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.domene.Behandlingsinformasjon
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.domene.BeregnetUtbetalingsoppdrag
import no.nav.utsjekk.iverksetting.util.lagIverksettingEntitet
import no.nav.utsjekk.iverksetting.util.lagIverksettingsdata
import no.nav.utsjekk.iverksetting.util.mai
import no.nav.utsjekk.kontrakter.oppdrag.OppdragStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

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
            iverksettingValidatorService.validerAtIverksettingGjelderSammeSakSomForrigeIverksetting(
                nåværendeIverksetting,
            )
        }
    }

    @Test
    fun `skal få BAD_REQUEST når forrige iverksetting har annen behandlingId enn siste mottatte iverksetting`() {
        val nyeste = LocalDateTime.now().minusDays(2)
        val sakId = RandomOSURId.generate()
        val sisteMottatteIverksetting =
            lagIverksettingEntitet(iverksettingData = lagIverksettingsdata(sakId = sakId), mottattTidspunkt = nyeste)
        val forrigeIverksetting = lagIverksettingsdata(sakId = sakId)
        val nåværendeIverksetting =
            lagIverksettingsdata(
                sakId = sakId,
                forrigeBehandlingId = forrigeIverksetting.behandlingId,
            )
        every { iverksettingServiceMock.hentSisteMottatteIverksetting(nåværendeIverksetting) } returns sisteMottatteIverksetting.data

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            iverksettingValidatorService.validerAtForrigeIverksettingErLikSisteMottatteIverksetting(
                nåværendeIverksetting,
            )
        }
    }

    @Test
    fun `skal få BAD_REQUEST når forrige iverksetting har annen iverksettingId enn siste mottatte iverksetting`() {
        val nyeste = LocalDateTime.now().minusDays(2)
        val sakId = RandomOSURId.generate()
        val behandlingId = RandomOSURId.generate()
        val sisteMottatteIverksetting =
            lagIverksettingEntitet(
                iverksettingData =
                    lagIverksettingsdata(
                        sakId = sakId,
                        behandlingId = behandlingId,
                        iverksettingId = RandomOSURId.generate(),
                    ),
                mottattTidspunkt = nyeste,
            )
        val forrigeIverksetting =
            lagIverksettingsdata(sakId = sakId, behandlingId = behandlingId, iverksettingId = RandomOSURId.generate())
        val nåværendeIverksetting =
            lagIverksettingsdata(
                sakId = sakId,
                behandlingId = behandlingId,
                forrigeBehandlingId = forrigeIverksetting.behandlingId,
                forrigeIverksettingId = forrigeIverksetting.behandling.iverksettingId,
            )
        every { iverksettingServiceMock.hentSisteMottatteIverksetting(nåværendeIverksetting) } returns sisteMottatteIverksetting.data

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            iverksettingValidatorService.validerAtForrigeIverksettingErLikSisteMottatteIverksetting(
                nåværendeIverksetting,
            )
        }
    }

    @Test
    fun `skal få BAD_REQUEST når forrige iverksetting ikke er satt og vi har mottatt iverksetting på saken før`() {
        val nyeste = LocalDateTime.now().minusDays(2)
        val sakId = RandomOSURId.generate()
        val sisteMottatteIverksetting =
            lagIverksettingEntitet(
                iverksettingData =
                    lagIverksettingsdata(
                        sakId = sakId,
                    ),
                mottattTidspunkt = nyeste,
            )
        val nåværendeIverksetting =
            lagIverksettingsdata(
                sakId = sakId,
            )
        every { iverksettingServiceMock.hentSisteMottatteIverksetting(nåværendeIverksetting) } returns sisteMottatteIverksetting.data

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            iverksettingValidatorService.validerAtForrigeIverksettingErLikSisteMottatteIverksetting(
                nåværendeIverksetting,
            )
        }
    }

    @Test
    fun `skal få BAD_REQUEST når forrige iverksetting er satt og vi ikke har mottatt iverksetting på saken før`() {
        val nåværendeIverksetting =
            lagIverksettingsdata(
                forrigeBehandlingId = RandomOSURId.generate(),
            )
        every { iverksettingServiceMock.hentSisteMottatteIverksetting(nåværendeIverksetting) } returns null

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            iverksettingValidatorService.validerAtForrigeIverksettingErLikSisteMottatteIverksetting(
                nåværendeIverksetting,
            )
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
            iverksettingValidatorService.validerAtForrigeIverksettingErFerdigIverksattMotOppdrag(nåværendeIverksetting)
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
