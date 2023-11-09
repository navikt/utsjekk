package no.nav.dagpenger.iverksett.infrastruktur.json

import com.fasterxml.jackson.module.kotlin.readValue
import java.util.UUID
import no.nav.dagpenger.iverksett.ResourceLoaderTestUtil
import no.nav.dagpenger.iverksett.api.domene.Iverksett
import no.nav.dagpenger.iverksett.infrastruktur.util.ObjectMapperProvider.objectMapper
import no.nav.dagpenger.iverksett.infrastruktur.util.opprettIverksett
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Dersom testene i denne filen feiler i maven-bygg, men ikke når det kjøres i IntelliJ,
 * så hjelper det sannsynligvis å reloade maven dependencies.
 */
class IverksettJsonTransformTest {

    @Test
    fun `deserialiser dagpenger JSON til IverksettDtoJson, kall toDomain, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettDto = objectMapper.readValue<IverksettDto>(json)

        assertNotNull(iverksettDto)
    }

    @Test
    fun `deserialiser JSON til Iverksett, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettEksempel.json")
        val iverksett = objectMapper.readValue<Iverksett>(json)
        assertNotNull(iverksett)
    }

    @Test
    internal fun `deserialiser iverksettDagpenger til json og serialiser tilbake til object, forvent likhet`() {
        val behandlingId = UUID.randomUUID()
        val iverksett = opprettIverksett(behandlingId)
        val parsetIverksett = objectMapper.readValue<Iverksett>(objectMapper.writeValueAsString(iverksett))
        assertEquals(iverksett, parsetIverksett)
    }
}
