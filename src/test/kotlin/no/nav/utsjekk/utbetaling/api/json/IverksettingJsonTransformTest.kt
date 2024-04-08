package no.nav.utsjekk.utbetaling.api.json

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.utsjekk.felles.http.ObjectMapperProvider.objectMapper
import no.nav.utsjekk.kontrakter.iverksett.IverksettDto
import no.nav.utsjekk.utbetaling.domene.Iverksetting
import no.nav.utsjekk.utbetaling.domene.transformer.RandomOSURId
import no.nav.utsjekk.utbetaling.util.enIverksetting
import no.nav.utsjekk.util.ResourceLoaderTestUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class IverksettingJsonTransformTest {
    @Test
    fun `deserialiser dagpenger JSON til IverksettDtoJson, kall toDomain, forvent likhet`() {
        val json: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")
        val iverksettDto = objectMapper.readValue<IverksettDto>(json)

        assertNotNull(iverksettDto)
    }

    @Test
    fun `deserialiser JSON til Iverksett, forvent ingen unntak`() {
        val json = ResourceLoaderTestUtil.readResource("json/IverksettEksempel.json")
        val iverksetting = objectMapper.readValue<Iverksetting>(json)
        assertNotNull(iverksetting)
    }

    @Test
    internal fun `deserialiser iverksettDagpenger til json og serialiser tilbake til object, forvent likhet`() {
        val behandlingId = RandomOSURId.generate()
        val iverksetting = enIverksetting(behandlingId)
        val parsetIverksetting = objectMapper.readValue<Iverksetting>(objectMapper.writeValueAsString(iverksetting))
        assertEquals(iverksetting, parsetIverksetting)
    }
}
