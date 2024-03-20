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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IverksettingRepositoryTest : Integrasjonstest() {
    private lateinit var iverksettingRepository: IverksettingRepository

    @BeforeAll
    fun setup() {
        iverksettingRepository = IverksettingRepository(namedParameterJdbcTemplate)
    }

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

        val iverksetting = lagIverksettingEntitet(iverksettingData)
        iverksettingRepository.insert(iverksetting)

        iverksettingRepository.findByFagsakId(iverksettingData.fagsak.fagsakId).also {
            assertEquals(1, it.size)
            assertSammeIverksetting(it.first(), iverksetting)
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

        iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
            fagsakId = iverksettingData.sakId,
            behandlingId = iverksettingData.behandlingId,
            iverksettingId = iverksettingData.behandling.iverksettingId,
        ).also {
            assertTrue(it.isEmpty())
        }

        val iverksetting = lagIverksettingEntitet(iverksettingData)
        iverksettingRepository.insert(iverksetting)

        iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
            fagsakId = iverksettingData.sakId,
            behandlingId = iverksettingData.behandlingId,
            iverksettingId = iverksettingData.behandling.iverksettingId,
        ).also {
            assertTrue(it.isNotEmpty())
            assertSammeIverksetting(iverksetting, it.first())
        }
    }

    @Test
    fun `lagre og hent iverksett på behandlingId og tom iverksettingId, forvent likhet`() {
        val iverksettingData: Iverksetting =
            lagIverksettingsdata(
                sakId = RandomOSURId.generate(),
                behandlingId = RandomOSURId.generate(),
                andelsdatoer = listOf(LocalDate.now(), LocalDate.now().minusDays(15)),
            )

        iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
            fagsakId = iverksettingData.sakId,
            behandlingId = iverksettingData.behandlingId,
            iverksettingId = iverksettingData.behandling.iverksettingId,
        ).also {
            assertTrue(it.isEmpty())
        }

        val iverksetting = lagIverksettingEntitet(iverksettingData)
        iverksettingRepository.insert(iverksetting)

        iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
            fagsakId = iverksettingData.sakId,
            behandlingId = iverksettingData.behandlingId,
            iverksettingId = iverksettingData.behandling.iverksettingId,
        ).also {
            assertTrue(it.isNotEmpty())
            assertSammeIverksetting(iverksetting, it.first())
        }
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

        val forventet1 = lagIverksettingEntitet(iverksettingData1)
        val forventet2 = lagIverksettingEntitet(iverksettingData2)
        iverksettingRepository.insert(forventet1)
        iverksettingRepository.insert(forventet2)

        val faktisk1 =
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = iverksettingData1.sakId,
                behandlingId = iverksettingData1.behandlingId,
                iverksettingId = iverksettingData1.behandling.iverksettingId,
            )
        val faktisk2 =
            iverksettingRepository.findByFagsakAndBehandlingAndIverksetting(
                fagsakId = iverksettingData2.sakId,
                behandlingId = iverksettingData2.behandlingId,
                iverksettingId = iverksettingData2.behandling.iverksettingId,
            )

        assertEquals(1, faktisk1.size)
        assertSammeIverksetting(forventet1, faktisk1.first())
        assertEquals(1, faktisk2.size)
        assertSammeIverksetting(forventet2, faktisk2.first())
    }

    private fun assertSammeIverksetting(
        forventet: IverksettingEntitet,
        faktisk: IverksettingEntitet,
    ) {
        assertEquals(forventet.behandlingId, faktisk.behandlingId)
        assertEquals(forventet.data, faktisk.data)
        assertEquals(forventet.mottattTidspunkt.truncatedTo(ChronoUnit.SECONDS), faktisk.mottattTidspunkt.truncatedTo(ChronoUnit.SECONDS))
    }
}
