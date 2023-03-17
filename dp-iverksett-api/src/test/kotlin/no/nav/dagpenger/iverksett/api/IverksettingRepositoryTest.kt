package no.nav.dagpenger.iverksett.api

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.dagpenger.iverksett.ResourceLoaderTestUtil
import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.api.domene.IverksettOvergangsstønad
import no.nav.dagpenger.iverksett.infrastruktur.repository.findByIdOrThrow
import no.nav.dagpenger.iverksett.infrastruktur.transformer.toDomain
import no.nav.dagpenger.iverksett.infrastruktur.util.ObjectMapperProvider.objectMapper
import no.nav.dagpenger.iverksett.kontrakter.iverksett.IverksettOvergangsstønadDto
import no.nav.dagpenger.iverksett.lagIverksett
import no.nav.dagpenger.iverksett.util.opprettBrev
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class IverksettingRepositoryTest : ServerTest() {

    @Autowired
    private lateinit var iverksettingRepository: IverksettingRepository

    @Test
    fun `lagre og hent iverksett overgangsstønad, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettData: IverksettOvergangsstønad = objectMapper.readValue<IverksettOvergangsstønadDto>(json).toDomain()
        val iverksett = iverksettingRepository.insert(lagIverksett(iverksettData, opprettBrev()))

        val iverksettResultat = iverksettingRepository.findByIdOrThrow(iverksett.behandlingId)
        assertThat(iverksett).usingRecursiveComparison().isEqualTo(iverksettResultat)
    }

    @Test
    fun `lagre og hent iverksett av eksternId, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettData: IverksettOvergangsstønad = objectMapper.readValue<IverksettOvergangsstønadDto>(json).toDomain()
        val iverksett = iverksettingRepository.insert(lagIverksett(iverksettData, opprettBrev()))

        val iverksettResultat = iverksettingRepository.findByEksternId(iverksett.eksternId)

        assertThat(iverksett).usingRecursiveComparison().isEqualTo(iverksettResultat)
    }
}
