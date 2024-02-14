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
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomString
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.felles.somString
import no.nav.dagpenger.kontrakter.felles.somUUID
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
        val behandlingId = UUID.randomUUID()
        val sakId = GeneriskIdSomUUID(UUID.randomUUID())
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingId)
        every { iverksettingsresultatService.hentIverksettResultat(any(), sakId, behandlingId, any()) } returns
            IverksettResultatMockBuilder.Builder()
                .build(Fagsystem.DAGPENGER, sakId, behandlingId, tilkjentYtelse)

        val status =
            iverksettingService.utledStatus(
                fagsystem = Fagsystem.DAGPENGER,
                sakId = sakId.somString,
                behandlingId = behandlingId,
            )
        assertEquals(IverksettStatus.SENDT_TIL_OPPDRAG, status)
    }

    @Test
    fun `la IverksettResultat ha tilkjent ytelse, oppdrag, og oppdragsresultat satt, forvent status FEILET_MOT_OPPDRAG`() {
        val behandlingsId = UUID.randomUUID()
        val sakId = GeneriskIdSomUUID(UUID.randomUUID())
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { iverksettingsresultatService.hentIverksettResultat(any(), sakId, behandlingsId, any()) } returns
            IverksettResultatMockBuilder.Builder()
                .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_FUNKSJONELL_FEIL))
                .build(Fagsystem.DAGPENGER, sakId, behandlingsId, tilkjentYtelse)

        val status =
            iverksettingService.utledStatus(
                fagsystem = Fagsystem.DAGPENGER,
                sakId = sakId.somString,
                behandlingId = behandlingsId,
            )
        assertEquals(IverksettStatus.FEILET_MOT_OPPDRAG, status)
    }

    @Test
    fun `la IverksettResultat ha felt satt for tilkjent ytelse, oppdrag med kvittert_ok, forvent status OK`() {
        val behandlingsId = UUID.randomUUID()
        val sakId = GeneriskIdSomUUID(UUID.randomUUID())
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { iverksettingsresultatService.hentIverksettResultat(any(), sakId, behandlingsId, any()) } returns
            IverksettResultatMockBuilder.Builder()
                .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_OK))
                .build(Fagsystem.DAGPENGER, sakId, behandlingsId, tilkjentYtelse)

        val status =
            iverksettingService.utledStatus(
                fagsystem = Fagsystem.DAGPENGER,
                sakId = sakId.somString,
                behandlingId = behandlingsId,
            )
        assertEquals(IverksettStatus.OK, status)
    }

    @Test
    fun `hentForrigeIverksett skal hente korrekt iverksetting når flere iverksettinger for samme behandling`() {
        val sakId = GeneriskIdSomUUID(UUID.randomUUID())
        val behandlingId = GeneriskIdSomUUID(UUID.randomUUID())
        val iverksettingId1 = "IVERK-1"
        val iverksettingId2 = "IVERK-2"
        val iverksetting1 =
            opprettIverksett(behandlingId = behandlingId, sakId = sakId, iverksettingId = iverksettingId1)
        val iverksetting2 =
            opprettIverksett(
                behandlingId = behandlingId,
                sakId = sakId,
                iverksettingId = iverksettingId2,
                forrigeBehandlingId = behandlingId,
                forrigeIverksettingId = iverksettingId1,
            )

        every {
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = sakId.somString,
                behandlingId = behandlingId.somUUID,
                iverksettingId = iverksettingId1,
            )
        } returns listOf(IverksettingEntitet(behandlingId.somUUID, iverksetting1))
        every {
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = sakId.somString,
                behandlingId = behandlingId.somUUID,
                iverksettingId = iverksettingId2,
            )
        } returns listOf(IverksettingEntitet(behandlingId.somUUID, iverksetting2))

        val forrigeIverksetting1 = iverksettingService.hentForrigeIverksett(iverksetting1)
        val forrigeIverksetting2 = iverksettingService.hentForrigeIverksett(iverksetting2)

        assertNull(forrigeIverksetting1)
        assertNotNull(forrigeIverksetting2)
        assertEquals(iverksetting1, forrigeIverksetting2)
    }

    @Test
    fun `hentForrigeIverksett skal hente korrekt iverksetting for iverksettingId på annen behandling`() {
        val sakId = GeneriskIdSomUUID(UUID.randomUUID())
        val behandlingId1 = GeneriskIdSomUUID(UUID.randomUUID())
        val behandlingId2 = GeneriskIdSomUUID(UUID.randomUUID())
        val iverksettingId1 = "1"
        val iverksettingId2 = "1"
        val iverksetting1 =
            opprettIverksett(behandlingId = behandlingId1, sakId = sakId, iverksettingId = iverksettingId1)
        val iverksetting2 =
            opprettIverksett(
                behandlingId = behandlingId2,
                sakId = sakId,
                iverksettingId = iverksettingId2,
                forrigeBehandlingId = behandlingId1,
                forrigeIverksettingId = iverksettingId1,
            )

        every {
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = sakId.somString,
                behandlingId = behandlingId1.somUUID,
                iverksettingId = iverksettingId1,
            )
        } returns listOf(IverksettingEntitet(behandlingId1.somUUID, iverksetting1))
        every {
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = sakId.somString,
                behandlingId = behandlingId2.somUUID,
                iverksettingId = iverksettingId2,
            )
        } returns listOf(IverksettingEntitet(behandlingId2.somUUID, iverksetting2))

        val forrigeIverksetting1 = iverksettingService.hentForrigeIverksett(iverksetting1)
        val forrigeIverksetting2 = iverksettingService.hentForrigeIverksett(iverksetting2)

        assertNull(forrigeIverksetting1)
        assertNotNull(forrigeIverksetting2)
        assertEquals(iverksetting1, forrigeIverksetting2)
    }

    @Test
    fun `hentIverksett skal hente korrekt iverksetting når flere fagsystem har samme behandlingId`() {
        val behandlingId = GeneriskIdSomUUID(UUID.randomUUID())
        val sakId = GeneriskIdSomUUID(UUID.randomUUID())
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
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = sakId.somString,
                behandlingId = behandlingId.somUUID,
                iverksettingId = null,
            )
        } returns
            listOf(
                IverksettingEntitet(behandlingId.somUUID, iverksettingDagpenger),
                IverksettingEntitet(behandlingId.somUUID, iverksettingTiltakspenger),
            )

        val hentetIverksettingTiltakspenger =
            iverksettingService.hentIverksetting(
                fagsystem = Fagsystem.TILTAKSPENGER,
                sakId = sakId,
                behandlingId = behandlingId,
            )

        assertNotNull(hentetIverksettingTiltakspenger)
        assertEquals(iverksettingTiltakspenger, hentetIverksettingTiltakspenger)
    }

    @Test
    fun `hentIverksett skal hente korrekt iverksetting når flere fagsaker har samme behandlingId`() {
        val behandlingId = GeneriskIdSomString("1")
        val sakId1 = GeneriskIdSomString("1")
        val sakId2 = GeneriskIdSomString("2")
        val iverksettingSak1 =
            opprettIverksett(
                sakId = sakId1,
                behandlingId = behandlingId,
            )
        val iverksettingSak2 =
            opprettIverksett(
                sakId = sakId2,
                behandlingId = behandlingId,
            )

        every {
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = sakId1.somString,
                behandlingId = behandlingId.somUUID,
                iverksettingId = null,
            )
        } returns
            listOf(
                IverksettingEntitet(behandlingId.somUUID, iverksettingSak1),
            )
        every {
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = sakId2.somString,
                behandlingId = behandlingId.somUUID,
                iverksettingId = null,
            )
        } returns
            listOf(
                IverksettingEntitet(behandlingId.somUUID, iverksettingSak2),
            )

        val hentetIverksettingSak1 =
            iverksettingService.hentIverksetting(
                fagsystem = iverksettingSak1.fagsak.fagsystem,
                sakId = sakId1,
                behandlingId = behandlingId,
            )

        assertNotNull(hentetIverksettingSak1)
        assertEquals(iverksettingSak1, hentetIverksettingSak1)
    }
}
