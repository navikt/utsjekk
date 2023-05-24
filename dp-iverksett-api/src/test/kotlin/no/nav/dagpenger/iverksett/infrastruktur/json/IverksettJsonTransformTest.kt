package no.nav.dagpenger.iverksett.infrastruktur.json

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.dagpenger.iverksett.ResourceLoaderTestUtil
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.infrastruktur.transformer.toDomain
import no.nav.dagpenger.iverksett.infrastruktur.util.ObjectMapperProvider.objectMapper
import no.nav.dagpenger.iverksett.kontrakter.iverksett.IverksettDagpengerdDto
import no.nav.dagpenger.iverksett.util.opprettIverksettDagpenger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Dersom testene i denne filen feiler i maven-bygg, men ikke når det kjøres i IntelliJ,
 * så hjelper det sannsynligvis å reloade maven dependencies.
 */
class IverksettJsonTransformTest {

    @Test
    fun `deserialiser dagpenger JSON til IverksettDtoJson, kall toDomain, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettDto = objectMapper.readValue<IverksettDagpengerdDto>(json)
        val iverksett = iverksettDto.toDomain()

        assertThat(iverksettDto).isInstanceOf(IverksettDagpengerdDto::class.java)
        assertThat(iverksett).isInstanceOf(IverksettDagpenger::class.java)

        assertThat(iverksett).isNotNull
        assertThat(objectMapper.readTree(json))
            .isEqualTo(objectMapper.readTree(objectMapper.writeValueAsString(iverksettDto)))
    }

    @Test
    fun `deserialiser JSON til Iverksett, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettEksempel.json")
        val iverksett = objectMapper.readValue<IverksettDagpenger>(json)
        assertThat(iverksett).isNotNull
    }

    @Test
    internal fun `deserialiser iverksettDagpenger til json og serialiser tilbake til object, forvent likhet`() {
        val behandlingId = UUID.randomUUID()
        val iverksett = opprettIverksettDagpenger(behandlingId)
        val parsetIverksett = objectMapper.readValue<IverksettDagpenger>(objectMapper.writeValueAsString(iverksett))
        assertThat(iverksett).isEqualTo(parsetIverksett)
    }
}
