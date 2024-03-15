package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.Integrasjonstest
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.IverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.domene.behandlingId
import no.nav.dagpenger.iverksett.utbetaling.domene.sakId
import no.nav.dagpenger.iverksett.utbetaling.domene.transformer.RandomOSURId
import no.nav.dagpenger.iverksett.utbetaling.lagIverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.lagIverksettingsdata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class IverksettingRepositoryTest : Integrasjonstest() {
    @Autowired
    private lateinit var iverksettingRepository: IverksettingRepository

    @Test
    fun `lagre og hent iverksett på fagsakId, forvent likhet`() {
        val iverksettingData: Iverksetting =
            lagIverksettingsdata(
                sakId = RandomOSURId.generate(),
                behandlingId = RandomOSURId.generate(),
                andelsdatoer = listOf(LocalDate.now(), LocalDate.now().minusDays(15)),
            )

        iverksettingRepository.findByFagsakId(iverksettingData.fagsak.fagsakId).also {
            assertEquals(0, it.size)
        }

        val iverksett = iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData))

        iverksettingRepository.findByFagsakId(iverksettingData.fagsak.fagsakId).also {
            assertEquals(1, it.size)
            assertSammeIverksetting(it[0], iverksett)
        }
    }

    @Test
    fun `lagre og hent iverksett på behandlingId og iverksettingId, forvent likhet`() {
        val tmp: Iverksetting =
            lagIverksettingsdata(
                sakId = RandomOSURId.generate(),
                behandlingId = RandomOSURId.generate(),
                andelsdatoer = listOf(LocalDate.now(), LocalDate.now().minusDays(15)),
            )
        val iverksettingData = tmp.copy(behandling = tmp.behandling.copy(iverksettingId = "TEST123"))

        val iverksetting =
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = iverksettingData.sakId,
                behandlingId = iverksettingData.behandlingId,
                iverksettingId = iverksettingData.behandling.iverksettingId,
            )
        assertTrue(iverksetting.isEmpty())

        val iverksett = iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData))

        val iverksetting2 =
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = iverksettingData.sakId,
                behandlingId = iverksettingData.behandlingId,
                iverksettingId = iverksettingData.behandling.iverksettingId,
            )
        assertTrue(iverksetting2.isNotEmpty())
        assertSammeIverksetting(iverksett, iverksetting2.first())
    }

    @Test
    fun `lagre og hent iverksett på behandlingId og tom iverksettingId, forvent likhet`() {
        val iverksettingData: Iverksetting =
            lagIverksettingsdata(
                sakId = RandomOSURId.generate(),
                behandlingId = RandomOSURId.generate(),
                andelsdatoer = listOf(LocalDate.now(), LocalDate.now().minusDays(15)),
            )

        val iverksetting =
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = iverksettingData.sakId,
                behandlingId = iverksettingData.behandlingId,
                iverksettingId = iverksettingData.behandling.iverksettingId,
            )
        assertTrue(iverksetting.isEmpty())

        val iverksett = iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData))

        val iverksetting2 =
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = iverksettingData.sakId,
                behandlingId = iverksettingData.behandlingId,
                iverksettingId = iverksettingData.behandling.iverksettingId,
            )
        assertTrue(iverksetting2.isNotEmpty())
        assertSammeIverksetting(iverksett, iverksetting2.first())
    }

    @Test
    fun `lagre og hent iverksett på fagsakId, behandlingId og tom iverksettingId, forvent 1 iverksetting per fagsak`() {
        val sakId1 = RandomOSURId.generate()
        val sakId2 = RandomOSURId.generate()
        val behandlingId = RandomOSURId.generate()
        val iverksettingData1 =
            lagIverksettingsdata(
                sakId = sakId1,
                behandlingId = behandlingId,
                andelsdatoer = listOf(LocalDate.now(), LocalDate.now().minusDays(15)),
            )

        val iverksettingData2 =
            lagIverksettingsdata(
                sakId = sakId2,
                behandlingId = behandlingId,
                andelsdatoer = listOf(LocalDate.now(), LocalDate.now().minusDays(15)),
            )

        val iverksett1 = iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData1))
        val iverksett2 = iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData2))

        val iverksetting1 =
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = iverksettingData1.sakId,
                behandlingId = iverksettingData1.behandlingId,
                iverksettingId = iverksettingData1.behandling.iverksettingId,
            )
        val iverksetting2 =
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = iverksettingData2.sakId,
                behandlingId = iverksettingData2.behandlingId,
                iverksettingId = iverksettingData2.behandling.iverksettingId,
            )

        assertEquals(1, iverksetting1.size)
        assertSammeIverksetting(iverksett1, iverksetting1.first())
        assertEquals(1, iverksetting2.size)
        assertSammeIverksetting(iverksett2, iverksetting2.first())
    }

    private fun assertSammeIverksetting(
        forventet: IverksettingEntitet,
        faktisk: IverksettingEntitet,
    ) {
        assertEquals(forventet.behandlingId, faktisk.behandlingId)
        assertEquals(forventet.data, faktisk.data)
        assertEquals(forventet.mottattTidspunkt?.truncatedTo(ChronoUnit.SECONDS), faktisk.mottattTidspunkt?.truncatedTo(ChronoUnit.SECONDS))
    }
}
