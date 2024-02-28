package no.nav.dagpenger.iverksett.utbetaling.tilstand

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.iverksett.felles.oppdrag.OppdragClient
import no.nav.dagpenger.iverksett.felles.util.mockFeatureToggleService
import no.nav.dagpenger.iverksett.utbetaling.domene.IverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.util.enAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.util.enIverksetting
import no.nav.dagpenger.iverksett.utbetaling.util.enTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.util.etIverksettingsresultat
import no.nav.dagpenger.iverksett.utbetaling.util.etTomtUtbetalingsoppdrag
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
    private val iverksettingsresultatService = mockk<IverksettingsresultatService>()
    private val taskService = mockk<TaskService>()
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val oppdragClient = mockk<OppdragClient>()

    private val iverksettingService: IverksettingService =
        IverksettingService(
            taskService = taskService,
            iverksettingsresultatService = iverksettingsresultatService,
            iverksettingRepository = iverksettingRepository,
            oppdragClient = oppdragClient,
            featureToggleService = mockFeatureToggleService(),
        )

    @Test
    fun `forventer status SENDT_TIL_OPPDRAG for resultat med tilkjent ytelse`() {
        val behandlingId = UUID.randomUUID()
        val resultat =
            etIverksettingsresultat(
                behandlingId = behandlingId,
                tilkjentYtelse = enTilkjentYtelse(behandlingId),
            )

        every {
            iverksettingsresultatService.hentIverksettingsresultat(
                any(),
                resultat.sakId,
                resultat.behandlingId,
                any(),
            )
        } returns resultat

        val status =
            iverksettingService.utledStatus(
                fagsystem = resultat.fagsystem,
                sakId = resultat.sakId.somString,
                behandlingId = resultat.behandlingId,
                iverksettingId = resultat.iverksettingId,
            )

        assertEquals(IverksettStatus.SENDT_TIL_OPPDRAG, status)
    }

    @Test
    fun `forventer status FEILET_MOT_OPPDRAG for resultat med oppdragsresultat med status KVITTERT_FUNKSJONELL_FEIL`() {
        val behandlingId = UUID.randomUUID()
        val resultat =
            etIverksettingsresultat(
                behandlingId = behandlingId,
                tilkjentYtelse = enTilkjentYtelse(behandlingId),
                oppdragResultat = OppdragResultat(OppdragStatus.KVITTERT_FUNKSJONELL_FEIL),
            )

        every {
            iverksettingsresultatService.hentIverksettingsresultat(
                any(),
                resultat.sakId,
                resultat.behandlingId,
                any(),
            )
        } returns resultat

        val status =
            iverksettingService.utledStatus(
                fagsystem = resultat.fagsystem,
                sakId = resultat.sakId.somString,
                behandlingId = resultat.behandlingId,
                iverksettingId = resultat.iverksettingId,
            )

        assertEquals(IverksettStatus.FEILET_MOT_OPPDRAG, status)
    }

    @Test
    fun `forventer status OK for resultat med tilkjent ytelse og oppdrag som er kvittert ok`() {
        val behandlingId = UUID.randomUUID()
        val resultat =
            etIverksettingsresultat(
                behandlingId = behandlingId,
                tilkjentYtelse = enTilkjentYtelse(behandlingId),
                oppdragResultat = OppdragResultat(OppdragStatus.KVITTERT_OK),
            )

        every {
            iverksettingsresultatService.hentIverksettingsresultat(
                any(),
                resultat.sakId,
                resultat.behandlingId,
                any(),
            )
        } returns
            resultat

        val status =
            iverksettingService.utledStatus(
                fagsystem = resultat.fagsystem,
                sakId = resultat.sakId.somString,
                behandlingId = resultat.behandlingId,
                iverksettingId = resultat.iverksettingId,
            )

        assertEquals(IverksettStatus.OK, status)
    }

    @Test
    fun `forventer status OK_UTEN_UTBETALING for resultat med tom tilkjent ytelse uten kvittering fra oppdrag`() {
        val behandlingId = UUID.randomUUID()
        val resultat =
            etIverksettingsresultat(
                behandlingId = behandlingId,
                tilkjentYtelse =
                    enTilkjentYtelse(
                        behandlingId = behandlingId,
                        andeler = emptyList(),
                        utbetalingsoppdrag = etTomtUtbetalingsoppdrag(),
                    ),
                oppdragResultat = OppdragResultat(OppdragStatus.OK_UTEN_UTBETALING),
            )

        every {
            iverksettingsresultatService.hentIverksettingsresultat(
                any(),
                resultat.sakId,
                resultat.behandlingId,
                any(),
            )
        } returns
            resultat

        val status =
            iverksettingService.utledStatus(
                fagsystem = resultat.fagsystem,
                sakId = resultat.sakId.somString,
                behandlingId = resultat.behandlingId,
                iverksettingId = resultat.iverksettingId,
            )

        assertEquals(IverksettStatus.OK_UTEN_UTBETALING, status)
    }

    @Test
    fun `hentForrigeIverksett skal hente korrekt iverksetting når flere iverksettinger for samme behandling`() {
        val sakId = GeneriskIdSomUUID(UUID.randomUUID())
        val behandlingId = GeneriskIdSomUUID(UUID.randomUUID())
        val iverksettingId1 = "IVERK-1"
        val iverksettingId2 = "IVERK-2"
        val iverksetting1 =
            enIverksetting(behandlingId = behandlingId, sakId = sakId, iverksettingId = iverksettingId1)
        val iverksetting2 =
            enIverksetting(
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

        val forrigeIverksetting1 = iverksettingService.hentForrigeIverksetting(iverksetting1)
        val forrigeIverksetting2 = iverksettingService.hentForrigeIverksetting(iverksetting2)

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
            enIverksetting(behandlingId = behandlingId1, sakId = sakId, iverksettingId = iverksettingId1)
        val iverksetting2 =
            enIverksetting(
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

        val forrigeIverksetting1 = iverksettingService.hentForrigeIverksetting(iverksetting1)
        val forrigeIverksetting2 = iverksettingService.hentForrigeIverksetting(iverksetting2)

        assertNull(forrigeIverksetting1)
        assertNotNull(forrigeIverksetting2)
        assertEquals(iverksetting1, forrigeIverksetting2)
    }

    @Test
    fun `hentIverksett skal hente korrekt iverksetting når flere fagsystem har samme behandlingId`() {
        val behandlingId = GeneriskIdSomUUID(UUID.randomUUID())
        val sakId = GeneriskIdSomUUID(UUID.randomUUID())
        val iverksettingDagpenger =
            enIverksetting(
                behandlingId = behandlingId,
                andeler = listOf(enAndelTilkjentYtelse(stønadstype = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR)),
            )
        val iverksettingTiltakspenger =
            enIverksetting(
                behandlingId = behandlingId,
                andeler = listOf(enAndelTilkjentYtelse(stønadstype = StønadTypeTiltakspenger.JOBBKLUBB)),
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
            enIverksetting(
                sakId = sakId1,
                behandlingId = behandlingId,
            )
        val iverksettingSak2 =
            enIverksetting(
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
