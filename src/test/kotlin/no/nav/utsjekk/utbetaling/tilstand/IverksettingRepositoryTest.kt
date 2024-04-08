package no.nav.utsjekk.utbetaling.tilstand

import no.nav.utsjekk.Integrasjonstest
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.utbetaling.domene.Iverksetting
import no.nav.utsjekk.utbetaling.domene.IverksettingEntitet
import no.nav.utsjekk.utbetaling.domene.behandlingId
import no.nav.utsjekk.utbetaling.domene.sakId
import no.nav.utsjekk.utbetaling.domene.transformer.RandomOSURId
import no.nav.utsjekk.utbetaling.util.enIverksetting
import no.nav.utsjekk.utbetaling.util.lagIverksettingEntitet
import no.nav.utsjekk.utbetaling.util.lagIverksettingsdata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import java.time.LocalDateTime
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

    @Test
    fun `skal hente alle mottatte iverksettinger på sak`() {
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
        val annenSakIverksetting =
            lagIverksettingEntitet(
                iverksettingData = enIverksetting(sakId = RandomOSURId.generate(), fagsystem = fagsystem),
                mottattTidspunkt = LocalDateTime.now().minusDays(2),
            )
        val annetFagsystemIverksetting =
            lagIverksettingEntitet(
                iverksettingData = enIverksetting(sakId = sakId, fagsystem = Fagsystem.TILLEGGSSTØNADER),
                mottattTidspunkt = LocalDateTime.now(),
            )
        iverksettingRepository.insert(eldsteIverksetting)
        iverksettingRepository.insert(mellomsteIverksetting)
        iverksettingRepository.insert(nyesteIverksetting)
        iverksettingRepository.insert(annenSakIverksetting)
        iverksettingRepository.insert(annetFagsystemIverksetting)

        val alleIverksettingerPåSak =
            iverksettingRepository.findByFagsakIdAndFagsystem(fagsakId = sakId, fagsystem = fagsystem)

        assertEquals(3, alleIverksettingerPåSak.size)
    }

    @Test
    fun `findByFagsakIdAndFagsystem skal gi tom liste når det ikke finnes iverksettinger på sak`() {
        val alleIverksettingerPåSak =
            iverksettingRepository.findByFagsakIdAndFagsystem(fagsakId = RandomOSURId.generate(), fagsystem = Fagsystem.DAGPENGER)

        assertEquals(0, alleIverksettingerPåSak.size)
    }

    private fun assertSammeIverksetting(
        forventet: IverksettingEntitet,
        faktisk: IverksettingEntitet,
    ) {
        assertEquals(forventet.behandlingId, faktisk.behandlingId)
        assertEquals(forventet.data, faktisk.data)
        assertEquals(
            forventet.mottattTidspunkt.truncatedTo(ChronoUnit.SECONDS),
            faktisk.mottattTidspunkt.truncatedTo(ChronoUnit.SECONDS),
        )
    }
}
