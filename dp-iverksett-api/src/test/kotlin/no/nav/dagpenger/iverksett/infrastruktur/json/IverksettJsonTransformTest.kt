package no.nav.dagpenger.iverksett.infrastruktur.json

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.dagpenger.iverksett.ResourceLoaderTestUtil
import no.nav.dagpenger.iverksett.api.domene.IverksettOvergangsstønad
import no.nav.dagpenger.iverksett.infrastruktur.transformer.toDomain
import no.nav.dagpenger.iverksett.infrastruktur.util.ObjectMapperProvider.objectMapper
import no.nav.dagpenger.iverksett.kontrakter.iverksett.IverksettOvergangsstønadDto
import no.nav.dagpenger.iverksett.util.opprettIverksettOvergangsstønad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Dersom testene i denne filen feiler i maven-bygg, men ikke når det kjøres i IntelliJ,
 * så hjelper det sannsynligvis å reloade maven dependencies.
 */
class IverksettJsonTransformTest {

    @Test
    fun `deserialiser overgangsstønad JSON til IverksettDtoJson, kall toDomain, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettJson = objectMapper.readValue<IverksettOvergangsstønadDto>(json)
        val iverksett = iverksettJson.toDomain()

        assertThat(iverksettJson).isInstanceOf(IverksettOvergangsstønadDto::class.java)
        assertThat(iverksett).isInstanceOf(IverksettOvergangsstønad::class.java)

        assertThat(iverksett).isNotNull
        assertThat(objectMapper.readTree(json))
            .isEqualTo(objectMapper.readTree(objectMapper.writeValueAsString(iverksettJson)))
    }

    @Test
    fun `deserialiser JSON til Iverksett, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettEksempel.json")
        val iverksett = objectMapper.readValue<IverksettOvergangsstønad>(json)
        assertThat(iverksett).isNotNull
    }

    @Test
    internal fun `deserialiser iverksettOvergangsstønad til json og serialiser tilbake til object, forvent likhet`() {
        val behandlingId = UUID.randomUUID()
        val iverksett = opprettIverksettOvergangsstønad(behandlingId)
        val parsetIverksett = objectMapper.readValue<IverksettOvergangsstønad>(objectMapper.writeValueAsString(iverksett))
        assertThat(iverksett).isEqualTo(parsetIverksett)
    }
}
