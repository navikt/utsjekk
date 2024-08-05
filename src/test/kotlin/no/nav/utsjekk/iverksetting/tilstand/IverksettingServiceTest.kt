package no.nav.utsjekk.iverksetting.tilstand

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import no.nav.familie.prosessering.error.TaskExceptionUtenStackTrace
import no.nav.familie.prosessering.internal.TaskService
import no.nav.utsjekk.felles.oppdrag.OppdragClient
import no.nav.utsjekk.iverksetting.domene.IverksettingEntitet
import no.nav.utsjekk.iverksetting.domene.OppdragResultat
import no.nav.utsjekk.iverksetting.domene.StønadsdataTiltakspenger
import no.nav.utsjekk.iverksetting.domene.behandlingId
import no.nav.utsjekk.iverksetting.domene.transformer.RandomOSURId
import no.nav.utsjekk.iverksetting.featuretoggle.IverksettingErSkruddAvException
import no.nav.utsjekk.iverksetting.util.enAndelTilkjentYtelse
import no.nav.utsjekk.iverksetting.util.enIverksetting
import no.nav.utsjekk.iverksetting.util.enTilkjentYtelse
import no.nav.utsjekk.iverksetting.util.etIverksettingsresultat
import no.nav.utsjekk.iverksetting.util.etTomtUtbetalingsoppdrag
import no.nav.utsjekk.iverksetting.util.lagIverksettingEntitet
import no.nav.utsjekk.iverksetting.util.mockFeatureToggleService
import no.nav.utsjekk.konfig.FeatureToggleMock
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.utsjekk.kontrakter.iverksett.IverksettStatus
import no.nav.utsjekk.kontrakter.oppdrag.OppdragStatus
import no.nav.utsjekk.kontrakter.oppdrag.OppdragStatusDto
import no.nav.utsjekk.status.StatusEndretProdusent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

internal class IverksettingServiceTest {
    private val iverksettingsresultatService = mockk<IverksettingsresultatService>()
    private val taskService = mockk<TaskService>()
    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val oppdragClient = mockk<OppdragClient>()
    private val featureToggleService = mockFeatureToggleService()
    private val statusEndretProdusent = mockk<StatusEndretProdusent>(relaxed = true)

    private val iverksettingService: IverksettingService =
        IverksettingService(
            taskService = taskService,
            iverksettingsresultatService = iverksettingsresultatService,
            iverksettingRepository = iverksettingRepository,
            oppdragClient = oppdragClient,
            featureToggleService = featureToggleService,
            statusEndretProdusent = statusEndretProdusent,
        )

    @AfterEach
    fun reset() {
        FeatureToggleMock.resetMock()
    }

    @Test
    fun `iverksetter ikke utbetaling for dagpenger når iverksetting er skrudd av for dagpenger`() {
        every { featureToggleService.iverksettingErSkruddAvForFagsystem(Fagsystem.DAGPENGER) } returns true

        assertThrows<IverksettingErSkruddAvException> {
            iverksettingService.startIverksetting(
                enIverksetting(fagsystem = Fagsystem.DAGPENGER),
            )
        }
    }

    @Test
    fun `iverksetter ikke utbetaling for tiltakspenger når iverksetting er skrudd av for tiltakspenger`() {
        every { featureToggleService.iverksettingErSkruddAvForFagsystem(Fagsystem.TILTAKSPENGER) } returns true

        assertThrows<IverksettingErSkruddAvException> {
            iverksettingService.startIverksetting(
                enIverksetting(fagsystem = Fagsystem.TILTAKSPENGER),
            )
        }
    }

    @Test
    fun `iverksetter ikke utbetaling for tilleggsstønader når iverksetting er skrudd av for tilleggsstønader`() {
        every { featureToggleService.iverksettingErSkruddAvForFagsystem(Fagsystem.TILLEGGSSTØNADER) } returns true

        assertThrows<IverksettingErSkruddAvException> {
            iverksettingService.startIverksetting(enIverksetting(fagsystem = Fagsystem.TILLEGGSSTØNADER))
        }
    }

    @Test
    fun `iverksetter utbetaling for dagpenger når iverksetting for andre ytelser er skrudd av`() {
        every { iverksettingRepository.insert(any()) } returns 1
        every { iverksettingsresultatService.opprettTomtResultat(any(), any(), any(), any()) } just runs
        every { taskService.save(any()) } returns mockk()

        every { featureToggleService.iverksettingErSkruddAvForFagsystem(Fagsystem.DAGPENGER) } returns false
        every { featureToggleService.iverksettingErSkruddAvForFagsystem(Fagsystem.TILTAKSPENGER) } returns true
        every { featureToggleService.iverksettingErSkruddAvForFagsystem(Fagsystem.TILLEGGSSTØNADER) } returns true

        assertDoesNotThrow {
            iverksettingService.startIverksetting(enIverksetting(fagsystem = Fagsystem.DAGPENGER))
        }
    }

    @Test
    fun `forventer status SENDT_TIL_OPPDRAG for resultat med tilkjent ytelse`() {
        val behandlingId = RandomOSURId.generate()
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
                sakId = resultat.sakId,
                behandlingId = resultat.behandlingId,
                iverksettingId = resultat.iverksettingId,
            )

        assertEquals(IverksettStatus.SENDT_TIL_OPPDRAG, status)
    }

    @Test
    fun `forventer status FEILET_MOT_OPPDRAG for resultat med oppdragsresultat med status KVITTERT_FUNKSJONELL_FEIL`() {
        val behandlingId = RandomOSURId.generate()
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
                sakId = resultat.sakId,
                behandlingId = resultat.behandlingId,
                iverksettingId = resultat.iverksettingId,
            )

        assertEquals(IverksettStatus.FEILET_MOT_OPPDRAG, status)
    }

    @Test
    fun `forventer status OK for resultat med tilkjent ytelse og oppdrag som er kvittert ok`() {
        val behandlingId = RandomOSURId.generate()
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
                sakId = resultat.sakId,
                behandlingId = resultat.behandlingId,
                iverksettingId = resultat.iverksettingId,
            )

        assertEquals(IverksettStatus.OK, status)
    }

    @Test
    fun `forventer status OK_UTEN_UTBETALING for resultat med tom tilkjent ytelse uten kvittering fra oppdrag`() {
        val behandlingId = RandomOSURId.generate()
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
                sakId = resultat.sakId,
                behandlingId = resultat.behandlingId,
                iverksettingId = resultat.iverksettingId,
            )

        assertEquals(IverksettStatus.OK_UTEN_UTBETALING, status)
    }

    @Test
    fun `hentForrigeIverksett skal hente korrekt iverksetting når flere iverksettinger for samme behandling`() {
        val sakId = RandomOSURId.generate()
        val behandlingId = RandomOSURId.generate()
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
                fagsakId = sakId,
                behandlingId = behandlingId,
                iverksettingId = iverksettingId1,
            )
        } returns listOf(IverksettingEntitet(behandlingId, iverksetting1, LocalDateTime.now()))
        every {
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = sakId,
                behandlingId = behandlingId,
                iverksettingId = iverksettingId2,
            )
        } returns listOf(IverksettingEntitet(behandlingId, iverksetting2, LocalDateTime.now()))

        val forrigeIverksetting1 = iverksettingService.hentForrigeIverksetting(iverksetting1)
        val forrigeIverksetting2 = iverksettingService.hentForrigeIverksetting(iverksetting2)

        assertNull(forrigeIverksetting1)
        assertNotNull(forrigeIverksetting2)
        assertEquals(iverksetting1, forrigeIverksetting2)
    }

    @Test
    fun `hentForrigeIverksett skal hente korrekt iverksetting for iverksettingId på annen behandling`() {
        val sakId = RandomOSURId.generate()
        val behandlingId1 = RandomOSURId.generate()
        val behandlingId2 = RandomOSURId.generate()
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
                fagsakId = sakId,
                behandlingId = behandlingId1,
                iverksettingId = iverksettingId1,
            )
        } returns listOf(IverksettingEntitet(behandlingId1, iverksetting1, LocalDateTime.now()))
        every {
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = sakId,
                behandlingId = behandlingId2,
                iverksettingId = iverksettingId2,
            )
        } returns listOf(IverksettingEntitet(behandlingId2, iverksetting2, LocalDateTime.now()))

        val forrigeIverksetting1 = iverksettingService.hentForrigeIverksetting(iverksetting1)
        val forrigeIverksetting2 = iverksettingService.hentForrigeIverksetting(iverksetting2)

        assertNull(forrigeIverksetting1)
        assertNotNull(forrigeIverksetting2)
        assertEquals(iverksetting1, forrigeIverksetting2)
    }

    @Test
    fun `hentIverksett skal hente korrekt iverksetting når flere fagsystem har samme behandlingId`() {
        val behandlingId = RandomOSURId.generate()
        val sakId = RandomOSURId.generate()
        val iverksettingDagpenger =
            enIverksetting(
                behandlingId = behandlingId,
                andeler = listOf(enAndelTilkjentYtelse()),
            )
        val iverksettingTiltakspenger =
            enIverksetting(
                behandlingId = behandlingId,
                andeler =
                    listOf(
                        enAndelTilkjentYtelse(stønadsdata = StønadsdataTiltakspenger(stønadstype = StønadTypeTiltakspenger.JOBBKLUBB)),
                    ),
            )

        every {
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = sakId,
                behandlingId = behandlingId,
                iverksettingId = null,
            )
        } returns
            listOf(
                IverksettingEntitet(behandlingId, iverksettingDagpenger, LocalDateTime.now()),
                IverksettingEntitet(behandlingId, iverksettingTiltakspenger, LocalDateTime.now()),
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
        val behandlingId = RandomOSURId.generate()
        val sakId1 = RandomOSURId.generate()
        val sakId2 = RandomOSURId.generate()
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
                fagsakId = sakId1,
                behandlingId = behandlingId,
                iverksettingId = null,
            )
        } returns
            listOf(
                IverksettingEntitet(behandlingId, iverksettingSak1, LocalDateTime.now()),
            )
        every {
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = sakId2,
                behandlingId = behandlingId,
                iverksettingId = null,
            )
        } returns
            listOf(
                IverksettingEntitet(behandlingId, iverksettingSak2, LocalDateTime.now()),
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

    @Test
    fun `hent siste mottatte iverksetting skal hente nyeste iverksetting ved flere iverksettinger på sak`() {
        val sakId = RandomOSURId.generate()
        val fagsystem = Fagsystem.DAGPENGER
        val eldsteIverksetting =
            lagIverksettingEntitet(
                iverksettingData = enIverksetting(sakId = sakId, fagsystem = fagsystem),
                mottattTidspunkt = LocalDateTime.now().minusDays(14),
            )
        val mellomsteIverksetting =
            lagIverksettingEntitet(
                iverksettingData = enIverksetting(sakId = sakId, fagsystem = fagsystem),
                mottattTidspunkt = LocalDateTime.now().minusDays(7),
            )
        val nyesteIverksetting =
            lagIverksettingEntitet(
                iverksettingData = enIverksetting(sakId = sakId, fagsystem = fagsystem),
                mottattTidspunkt = LocalDateTime.now().minusDays(1),
            )
        every { iverksettingRepository.findByFagsakIdAndFagsystem(sakId, fagsystem) } returns
            listOf(
                nyesteIverksetting,
                eldsteIverksetting,
                mellomsteIverksetting,
            )

        val sisteMottatteIverksetting =
            iverksettingService.hentSisteMottatteIverksetting(enIverksetting(sakId = sakId, fagsystem = fagsystem))

        assertNotNull(sisteMottatteIverksetting)
        assertEquals(nyesteIverksetting.behandlingId, sisteMottatteIverksetting?.behandlingId)
    }

    @Test
    fun `sjekkStatusOgOppdraterTilstand skal ikke kaste exception ved feilkvittering`() {
        every { oppdragClient.hentStatus(any()) } returns
            OppdragStatusDto(
                status = OppdragStatus.KVITTERT_FUNKSJONELL_FEIL,
                feilmelding = "Funksjonell feil",
            )
        every { iverksettingsresultatService.oppdaterOppdragResultat(any(), any(), any(), any(), any()) } just runs
        every {
            iverksettingsresultatService.hentIverksettingsresultat(
                any(),
                any(),
                any(),
                any(),
            )
        } returns etIverksettingsresultat(oppdragResultat = OppdragResultat(oppdragStatus = OppdragStatus.KVITTERT_FUNKSJONELL_FEIL))
        every { statusEndretProdusent.sendStatusEndretEvent(any(), any()) } just runs

        assertDoesNotThrow { iverksettingService.sjekkStatusPåIverksettOgOppdaterTilstand(enIverksetting()) }
    }

    @Test
    fun `sjekkStatusOgOppdraterTilstand skal kaste exception ved manglende kvittering`() {
        every { oppdragClient.hentStatus(any()) } returns
            OppdragStatusDto(
                status = OppdragStatus.LAGT_PÅ_KØ,
                feilmelding = null,
            )

        assertThrows<TaskExceptionUtenStackTrace> {
            iverksettingService.sjekkStatusPåIverksettOgOppdaterTilstand(
                enIverksetting(),
            )
        }
    }
}
