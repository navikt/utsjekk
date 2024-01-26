package no.nav.dagpenger.iverksett.utbetaling.tilstand

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.dagpenger.iverksett.ResourceLoaderTestUtil
import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.felles.http.ObjectMapperProvider.objectMapper
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.behandlingId
import no.nav.dagpenger.iverksett.utbetaling.domene.transformer.toDomain
import no.nav.dagpenger.iverksett.utbetaling.lagIverksettingEntitet
import no.nav.dagpenger.kontrakter.felles.somString
import no.nav.dagpenger.kontrakter.felles.somUUID
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class IverksettingRepositoryTest : ServerTest() {
    @Autowired
    private lateinit var iverksettingRepository: IverksettingRepository

    @Test
    fun `lagre og hent iverksett dagpenger, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettingData: Iverksetting = objectMapper.readValue<IverksettDto>(json).toDomain()

        assertThrows<NoSuchElementException> { iverksettingRepository.findByIdOrThrow(iverksettingData.behandlingId.somUUID) }

        val iverksett = iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData))

        val iverksettResultat = iverksettingRepository.findByIdOrThrow(iverksett.behandlingId)
        assertThat(iverksett).usingRecursiveComparison().isEqualTo(iverksettResultat)
    }

    @Test
    fun `lagre og hent iverksett på fagsakId, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettingData: Iverksetting = objectMapper.readValue<IverksettDto>(json).toDomain()

        val fagsakId = iverksettingData.fagsak.fagsakId
        val iverksettListe1 = iverksettingRepository.findByFagsakId(fagsakId.somString)
        assertEquals(0, iverksettListe1.size)

        val iverksett = iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData))

        val iverksettListe2 = iverksettingRepository.findByFagsakId(fagsakId.somString)
        assertEquals(1, iverksettListe2.size)
        assertThat(iverksett).usingRecursiveComparison().isEqualTo(iverksettListe2[0])
    }

    @Test
    fun `lagre og hent iverksett på behandlingId og iverksettingId, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val tmp: Iverksetting = objectMapper.readValue<IverksettDto>(json).toDomain()
        val iverksettingData = tmp.copy(behandling = tmp.behandling.copy(iverksettingId = "TEST123"))

        val iverksetting =
            iverksettingRepository.findByBehandlingAndIverksetting(
                iverksettingData.behandlingId.somUUID,
                iverksettingData.behandling.iverksettingId,
            )
        assertNull(iverksetting)

        val iverksett = iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData))

        val iverksetting2 =
            iverksettingRepository.findByBehandlingAndIverksetting(
                iverksettingData.behandlingId.somUUID,
                iverksettingData.behandling.iverksettingId,
            )
        assertNotNull(iverksetting2)
        assertEquals(iverksett, iverksetting2)
    }

    @Test
    fun `lagre og hent iverksett på behandlingId og tom iverksettingId, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettingData: Iverksetting = objectMapper.readValue<IverksettDto>(json).toDomain()

        val iverksetting =
            iverksettingRepository.findByBehandlingAndIverksetting(
                iverksettingData.behandlingId.somUUID,
                iverksettingData.behandling.iverksettingId,
            )
        assertNull(iverksetting)

        val iverksett = iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData))

        val iverksetting2 =
            iverksettingRepository.findByBehandlingAndIverksetting(
                iverksettingData.behandlingId.somUUID,
                iverksettingData.behandling.iverksettingId,
            )
        assertNotNull(iverksetting2)
        assertEquals(iverksett, iverksetting2)
    }
}
