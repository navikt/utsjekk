package no.nav.dagpenger.iverksett.utbetaling.tilstand

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.iverksett.felles.oppdrag.OppdragClient
import no.nav.dagpenger.iverksett.utbetaling.domene.IverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.util.IverksettResultatMockBuilder
import no.nav.dagpenger.iverksett.utbetaling.util.opprettAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.util.opprettIverksett
import no.nav.dagpenger.iverksett.utbetaling.util.opprettTilkjentYtelse
import no.nav.dagpenger.iverksett.util.mockFeatureToggleService
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.iverksett.IverksettStatus
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

internal class IverksettingServiceTest {
    val iverksettingsresultatService = mockk<IverksettingsresultatService>()
    val taskService = mockk<TaskService>()
    val iverksettingRepository = mockk<IverksettingRepository>()
    private val oppdragClient = mockk<OppdragClient>()

    private var iverksettingService: IverksettingService =
        IverksettingService(
            taskService = taskService,
            iverksettingsresultatService = iverksettingsresultatService,
            iverksettingRepository = iverksettingRepository,
            oppdragClient = oppdragClient,
            featureToggleService = mockFeatureToggleService(),
        )

    @Test
    fun `la IverksettResultat ha felt kun satt for tilkjent ytelse, forvent status SENDT_TIL_OPPDRAG`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { iverksettingsresultatService.hentIverksettResultat(any(), behandlingsId, any()) } returns
            IverksettResultatMockBuilder.Builder()
                .build(Fagsystem.DAGPENGER, behandlingsId, tilkjentYtelse)

        val status = iverksettingService.utledStatus(Fagsystem.DAGPENGER, behandlingsId)
        assertEquals(IverksettStatus.SENDT_TIL_OPPDRAG, status)
    }

    @Test
    fun `la IverksettResultat ha tilkjent ytelse, oppdrag, og oppdragsresultat satt, forvent status FEILET_MOT_OPPDRAG`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { iverksettingsresultatService.hentIverksettResultat(any(), behandlingsId, any()) } returns
            IverksettResultatMockBuilder.Builder()
                .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_FUNKSJONELL_FEIL))
                .build(Fagsystem.DAGPENGER, behandlingsId, tilkjentYtelse)

        val status = iverksettingService.utledStatus(Fagsystem.DAGPENGER, behandlingsId)
        assertEquals(IverksettStatus.FEILET_MOT_OPPDRAG, status)
    }

    @Test
    fun `la IverksettResultat ha felt satt for tilkjent ytelse, oppdrag med kvittert_ok, forvent status OK`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { iverksettingsresultatService.hentIverksettResultat(any(), behandlingsId, any()) } returns
            IverksettResultatMockBuilder.Builder()
                .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_OK))
                .build(Fagsystem.DAGPENGER, behandlingsId, tilkjentYtelse)

        val status = iverksettingService.utledStatus(Fagsystem.DAGPENGER, behandlingsId)
        assertEquals(IverksettStatus.OK, status)
    }

    @Test
    fun `hentForrigeIverksett skal hente korrekt iverksetting når flere iverksettinger for samme behandling`() {
        val sakId = UUID.randomUUID()
        val behandlingId = UUID.randomUUID()
        val iverksettingId1 = "IVERK-1"
        val iverksettingId2 = "IVERK-2"
        val iverksetting1 = opprettIverksett(behandlingId = behandlingId, fagsakId = sakId, iverksettingId = iverksettingId1)
        val iverksetting2 =
            opprettIverksett(
                behandlingId = behandlingId,
                fagsakId = sakId,
                iverksettingId = iverksettingId2,
                forrigeBehandlingId = behandlingId,
                forrigeIverksettingId = iverksettingId1,
            )

        every {
            iverksettingRepository.findByBehandlingAndIverksetting(behandlingId, iverksettingId1)
        } returns listOf(IverksettingEntitet(behandlingId, iverksetting1))
        every {
            iverksettingRepository.findByBehandlingAndIverksetting(behandlingId, iverksettingId2)
        } returns listOf(IverksettingEntitet(behandlingId, iverksetting2))

        val forrigeIverksetting1 = iverksettingService.hentForrigeIverksett(iverksetting1)
        val forrigeIverksetting2 = iverksettingService.hentForrigeIverksett(iverksetting2)

        assertNull(forrigeIverksetting1)
        assertNotNull(forrigeIverksetting2)
        assertEquals(iverksetting1, forrigeIverksetting2)
    }

    @Test
    fun `hentForrigeIverksett skal hente korrekt iverksetting for iverksettingId på annen behandling`() {
        val sakId = UUID.randomUUID()
        val behandlingId1 = UUID.randomUUID()
        val behandlingId2 = UUID.randomUUID()
        val iverksettingId1 = "1"
        val iverksettingId2 = "1"
        val iverksetting1 = opprettIverksett(behandlingId = behandlingId1, fagsakId = sakId, iverksettingId = iverksettingId1)
        val iverksetting2 =
            opprettIverksett(
                behandlingId = behandlingId2,
                fagsakId = sakId,
                iverksettingId = iverksettingId2,
                forrigeBehandlingId = behandlingId1,
                forrigeIverksettingId = iverksettingId1,
            )

        every {
            iverksettingRepository.findByBehandlingAndIverksetting(behandlingId1, iverksettingId1)
        } returns listOf(IverksettingEntitet(behandlingId1, iverksetting1))
        every {
            iverksettingRepository.findByBehandlingAndIverksetting(behandlingId2, iverksettingId2)
        } returns listOf(IverksettingEntitet(behandlingId2, iverksetting2))

        val forrigeIverksetting1 = iverksettingService.hentForrigeIverksett(iverksetting1)
        val forrigeIverksetting2 = iverksettingService.hentForrigeIverksett(iverksetting2)

        assertNull(forrigeIverksetting1)
        assertNotNull(forrigeIverksetting2)
        assertEquals(iverksetting1, forrigeIverksetting2)
    }

    @Test
    fun `hentIverksett skal hente korrekt iverksetting når flere fagsystem har samme behandlingId`() {
        val behandlingId = UUID.randomUUID()
        val iverksettingDagpenger =
            opprettIverksett(
                behandlingId = behandlingId,
                andeler = listOf(opprettAndelTilkjentYtelse(stønadstype = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR)),
            )
        val iverksettingTiltakspenger =
            opprettIverksett(
                behandlingId = behandlingId,
                andeler = listOf(opprettAndelTilkjentYtelse(stønadstype = StønadTypeTiltakspenger.JOBBKLUBB)),
            )

        every {
            iverksettingRepository.findByBehandlingAndIverksetting(behandlingId, null)
        } returns listOf(IverksettingEntitet(behandlingId, iverksettingDagpenger), IverksettingEntitet(behandlingId, iverksettingTiltakspenger))

        val hentetIverksettingTiltakspenger = iverksettingService.hentIverksetting(Fagsystem.TILTAKSPENGER, behandlingId)

        assertNotNull(hentetIverksettingTiltakspenger)
        assertEquals(iverksettingTiltakspenger, hentetIverksettingTiltakspenger)
    }
}
