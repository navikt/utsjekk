package no.nav.dagpenger.iverksett.api

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.dagpenger.iverksett.ResourceLoaderTestUtil
import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.api.domene.Iverksetting
import no.nav.dagpenger.iverksett.api.domene.behandlingId
import no.nav.dagpenger.iverksett.infrastruktur.repository.findByIdOrThrow
import no.nav.dagpenger.iverksett.infrastruktur.transformer.toDomain
import no.nav.dagpenger.iverksett.infrastruktur.util.ObjectMapperProvider.objectMapper
import no.nav.dagpenger.iverksett.lagIverksettingEntitet
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class IverksettingRepositoryTest : ServerTest() {

    @Autowired
    private lateinit var iverksettingRepository: IverksettingRepository

    @Test
    fun `lagre og hent iverksett dagpenger, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettingData: Iverksetting = objectMapper.readValue<IverksettDto>(json).toDomain()

        assertThrows<IllegalStateException> { iverksettingRepository.findByIdOrThrow(iverksettingData.behandlingId) }

        val iverksett = iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData))

        val iverksettResultat = iverksettingRepository.findByIdOrThrow(iverksett.behandlingId)
        assertThat(iverksett).usingRecursiveComparison().isEqualTo(iverksettResultat)
    }

    @Test
    fun `lagre og hent iverksett på personId, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettingData: Iverksetting = objectMapper.readValue<IverksettDto>(json).toDomain()

        val iverksettListe1 = iverksettingRepository.findByPersonId(iverksettingData.søker.personident)
        assertEquals(0, iverksettListe1.size)

        val iverksett = iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData))

        val iverksettListe2 = iverksettingRepository.findByPersonId(iverksettingData.søker.personident)
        assertEquals(1, iverksettListe2.size)
        assertThat(iverksett).usingRecursiveComparison().isEqualTo(iverksettListe2[0])
    }

    @Test
    fun `lagre to iverksettinger på samme person, forvent å få begge`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettingData: Iverksetting = objectMapper.readValue<IverksettDto>(json).toDomain()
        val iverksettData2 = iverksettingData.copy(
            behandling = iverksettingData.behandling.copy(
                behandlingId = UUID.randomUUID(),
            ),
        )
        val iverksettDataAnnenPerson = iverksettingData.copy(
            behandling = iverksettingData.behandling.copy(
                behandlingId = UUID.randomUUID(),
            ),
            søker = iverksettingData.søker.copy(
                personident = "12345678911",
            ),
        )
        iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData))
        iverksettingRepository.insert(lagIverksettingEntitet(iverksettData2))
        val iverksettAnnenPerson = iverksettingRepository.insert(lagIverksettingEntitet(iverksettDataAnnenPerson))

        val iverksettResultat = iverksettingRepository.findByPersonId(iverksettingData.søker.personident)

        assertEquals(2, iverksettResultat.size)
        assertFalse(iverksettResultat.contains(iverksettAnnenPerson))
    }

    @Test
    fun `lagre og hent iverksett på fagsakId, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettingData: Iverksetting = objectMapper.readValue<IverksettDto>(json).toDomain()

        val fagsakId = iverksettingData.fagsak.fagsakId!!
        val iverksettListe1 = iverksettingRepository.findByFagsakId(fagsakId)
        assertEquals(0, iverksettListe1.size)

        val iverksett = iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData))

        val iverksettListe2 = iverksettingRepository.findByFagsakId(fagsakId)
        assertEquals(1, iverksettListe2.size)
        assertThat(iverksett).usingRecursiveComparison().isEqualTo(iverksettListe2[0])
    }

    @Test
    fun `lagre og hent iverksett på saksreferanse, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettingData: Iverksetting = objectMapper.readValue<IverksettDto>(json).toDomain()

        val saksreferanse = iverksettingData.fagsak.saksreferanse!!
        val iverksettListe1 = iverksettingRepository.findBySaksreferanse(saksreferanse)
        assertEquals(0, iverksettListe1.size)

        val iverksett = iverksettingRepository.insert(lagIverksettingEntitet(iverksettingData))

        val iverksettListe2 = iverksettingRepository.findBySaksreferanse(saksreferanse)
        assertEquals(1, iverksettListe2.size)
        assertThat(iverksett).usingRecursiveComparison().isEqualTo(iverksettListe2[0])
    }
}
