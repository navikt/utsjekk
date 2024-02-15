package no.nav.dagpenger.iverksett.utbetaling.api.json

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.dagpenger.iverksett.ResourceLoaderTestUtil
import no.nav.dagpenger.iverksett.felles.http.ObjectMapperProvider.objectMapper
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.util.enIverksetting
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Dersom testene i denne filen feiler i maven-bygg, men ikke når det kjøres i IntelliJ,
 * så hjelper det sannsynligvis å reloade maven dependencies.
 */
class IverksettingJsonTransformTest {
    @Test
    fun `deserialiser dagpenger JSON til IverksettDtoJson, kall toDomain, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettDto = objectMapper.readValue<IverksettDto>(json)

        assertNotNull(iverksettDto)
    }

    @Test
    fun `deserialiser JSON til Iverksett, forvent ingen unntak`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettEksempel.json")
        val iverksetting = objectMapper.readValue<Iverksetting>(json)
        assertNotNull(iverksetting)
    }

    @Test
    internal fun `deserialiser iverksettDagpenger til json og serialiser tilbake til object, forvent likhet`() {
        val behandlingId = GeneriskIdSomUUID(UUID.randomUUID())
        val iverksetting = enIverksetting(behandlingId)
        val parsetIverksetting = objectMapper.readValue<Iverksetting>(objectMapper.writeValueAsString(iverksetting))
        assertEquals(iverksetting, parsetIverksetting)
    }
}
