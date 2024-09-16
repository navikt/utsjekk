package no.nav.utsjekk.simulering.api

import io.mockk.every
import io.mockk.mockk
import no.nav.utsjekk.iverksetting.api.assertApiFeil
import no.nav.utsjekk.iverksetting.domene.Iverksettingsresultat
import no.nav.utsjekk.iverksetting.domene.OppdragResultat
import no.nav.utsjekk.iverksetting.domene.behandlingId
import no.nav.utsjekk.iverksetting.domene.sakId
import no.nav.utsjekk.iverksetting.domene.transformer.RandomOSURId
import no.nav.utsjekk.iverksetting.tilstand.IverksettingService
import no.nav.utsjekk.iverksetting.tilstand.IverksettingsresultatService
import no.nav.utsjekk.iverksetting.util.lagIverksettingEntitet
import no.nav.utsjekk.iverksetting.util.lagIverksettingsdata
import no.nav.utsjekk.iverksetting.util.mai
import no.nav.utsjekk.kontrakter.felles.StønadTypeTilleggsstønader
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTilleggsstønaderDto
import no.nav.utsjekk.kontrakter.oppdrag.OppdragStatus
import no.nav.utsjekk.simulering.domene.ForrigeIverksetting
import no.nav.utsjekk.simulering.enSimuleringRequestV2Dto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class SimuleringValidatorTest {
    private val iverksettingsresultatServiceMock = mockk<IverksettingsresultatService>()
    private val iverksettingServiceMock = mockk<IverksettingService>()
    private lateinit var simuleringValidator: SimuleringValidator

    @BeforeEach
    fun setup() {
        simuleringValidator =
            SimuleringValidator(
                iverksettingsresultatService = iverksettingsresultatServiceMock,
                iverksettingService = iverksettingServiceMock,
            )
    }

    @Test
    fun `skal få BAD_REQUEST når forrige iverksetting har annen behandlingId enn siste mottatte iverksetting`() {
        val nyeste = LocalDateTime.now().minusDays(2)
        val sakId = RandomOSURId.generate()
        val sisteMottatteIverksetting =
            lagIverksettingEntitet(iverksettingData = lagIverksettingsdata(sakId = sakId), mottattTidspunkt = nyeste)
        val forrigeIverksetting = lagIverksettingsdata(sakId = sakId)
        val tmpSimulering =
            enSimuleringRequestV2Dto(
                stønadsdataDto = StønadsdataTilleggsstønaderDto(stønadstype = StønadTypeTilleggsstønader.TILSYN_BARN_AAP),
            ).tilInterntFormat(fagsystem = forrigeIverksetting.fagsak.fagsystem)
        val simulering =
            tmpSimulering.copy(
                behandlingsinformasjon = tmpSimulering.behandlingsinformasjon.copy(fagsakId = sakId),
                forrigeIverksetting =
                    ForrigeIverksetting(
                        behandlingId = forrigeIverksetting.behandlingId,
                        iverksettingId = forrigeIverksetting.behandling.iverksettingId,
                    ),
            )
        every {
            iverksettingServiceMock.hentSisteMottatteIverksetting(
                fagsystem = simulering.behandlingsinformasjon.fagsystem,
                sakId = simulering.behandlingsinformasjon.fagsakId,
            )
        } returns sisteMottatteIverksetting.data

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            simuleringValidator.forrigeIverksettingErLikSisteMottatteIverksetting(simulering)
        }
    }

    @Test
    fun `skal få BAD_REQUEST når forrige iverksetting har annen iverksettingId enn siste mottatte iverksetting`() {
        val nyeste = LocalDateTime.now().minusDays(2)
        val sakId = RandomOSURId.generate()
        val sisteMottatteIverksetting =
            lagIverksettingEntitet(iverksettingData = lagIverksettingsdata(sakId = sakId), mottattTidspunkt = nyeste)
        val forrigeIverksetting =
            lagIverksettingsdata(
                sakId = sakId,
                behandlingId = sisteMottatteIverksetting.behandlingId,
                iverksettingId = RandomOSURId.generate(),
            )
        val tmpSimulering =
            enSimuleringRequestV2Dto(
                stønadsdataDto = StønadsdataTilleggsstønaderDto(stønadstype = StønadTypeTilleggsstønader.TILSYN_BARN_AAP),
            ).tilInterntFormat(fagsystem = forrigeIverksetting.fagsak.fagsystem)
        val simulering =
            tmpSimulering.copy(
                behandlingsinformasjon = tmpSimulering.behandlingsinformasjon.copy(fagsakId = sakId),
                forrigeIverksetting =
                    ForrigeIverksetting(
                        behandlingId = forrigeIverksetting.behandlingId,
                        iverksettingId = forrigeIverksetting.behandling.iverksettingId,
                    ),
            )
        every {
            iverksettingServiceMock.hentSisteMottatteIverksetting(
                fagsystem = simulering.behandlingsinformasjon.fagsystem,
                sakId = simulering.behandlingsinformasjon.fagsakId,
            )
        } returns sisteMottatteIverksetting.data

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            simuleringValidator.forrigeIverksettingErLikSisteMottatteIverksetting(simulering)
        }
    }

    @Test
    fun `skal få BAD_REQUEST når forrige iverksetting ikke er satt og vi har mottatt iverksetting på saken tidligere`() {
        val nyeste = LocalDateTime.now().minusDays(2)
        val sakId = RandomOSURId.generate()
        val sisteMottatteIverksetting =
            lagIverksettingEntitet(iverksettingData = lagIverksettingsdata(sakId = sakId), mottattTidspunkt = nyeste)
        val tmpSimulering =
            enSimuleringRequestV2Dto(
                stønadsdataDto = StønadsdataTilleggsstønaderDto(stønadstype = StønadTypeTilleggsstønader.TILSYN_BARN_AAP),
            ).tilInterntFormat(fagsystem = sisteMottatteIverksetting.data.fagsak.fagsystem)
        val simulering =
            tmpSimulering.copy(
                behandlingsinformasjon = tmpSimulering.behandlingsinformasjon.copy(fagsakId = sakId),
                forrigeIverksetting = null,
            )
        every {
            iverksettingServiceMock.hentSisteMottatteIverksetting(
                fagsystem = simulering.behandlingsinformasjon.fagsystem,
                sakId = simulering.behandlingsinformasjon.fagsakId,
            )
        } returns sisteMottatteIverksetting.data

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            simuleringValidator.forrigeIverksettingErLikSisteMottatteIverksetting(simulering)
        }
    }

    @Test
    fun `skal få CONFLICT når forrige iverksetting ikke er ferdig og OK mot oppdrag`() {
        val forrigeIverksetting =
            lagIverksettingsdata(
                andelsdatoer = listOf(1.mai, 2.mai),
                beløp = 300,
            )
        val tmpSimulering =
            enSimuleringRequestV2Dto(
                stønadsdataDto = StønadsdataTilleggsstønaderDto(stønadstype = StønadTypeTilleggsstønader.TILSYN_BARN_AAP),
            ).tilInterntFormat(fagsystem = forrigeIverksetting.fagsak.fagsystem)
        val simulering =
            tmpSimulering.copy(
                behandlingsinformasjon = tmpSimulering.behandlingsinformasjon.copy(fagsakId = forrigeIverksetting.sakId),
                forrigeIverksetting =
                    ForrigeIverksetting(
                        behandlingId = forrigeIverksetting.behandlingId,
                        iverksettingId = forrigeIverksetting.behandling.iverksettingId,
                    ),
            )

        val forrigeIverksettingsresultat =
            Iverksettingsresultat(
                fagsystem = forrigeIverksetting.fagsak.fagsystem,
                sakId = forrigeIverksetting.sakId,
                behandlingId = forrigeIverksetting.behandlingId,
                iverksettingId = forrigeIverksetting.behandling.iverksettingId,
                tilkjentYtelseForUtbetaling = forrigeIverksetting.vedtak.tilkjentYtelse,
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
            simuleringValidator.forrigeIverksettingSkalVæreFerdigstilt(simulering)
        }
    }
}
