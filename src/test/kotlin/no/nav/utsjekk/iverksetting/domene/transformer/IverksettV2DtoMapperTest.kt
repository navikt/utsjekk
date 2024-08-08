package no.nav.utsjekk.iverksetting.domene.transformer

import no.nav.utsjekk.iverksetting.domene.Iverksetting
import no.nav.utsjekk.kontrakter.felles.objectMapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class IverksettV2DtoMapperTest {
    @Test
    fun `klarer å deserialisere iverksetting med brukers navkontor på vedtaksnivå`() {
        @Language("JSON")
        val json = """
            {
              "fagsak": {
                "fagsakId": "202405291003",
                "fagsystem": "TILTAKSPENGER"
              },
              "behandling": {
                "forrigeBehandlingId": null,
                "forrigeIverksettingId": null,
                "behandlingId": "7ZM9H57AQJ87YBM",
                "iverksettingId": null
              },
              "søker": {
                "personident": "14887697311"
              },
              "vedtak": {
                "vedtakstidspunkt": "2024-05-29T11:19:31.530151033",
                "saksbehandlerId": "Z994144",
                "beslutterId": "Z994144",
                "brukersNavKontor": {
                  "enhet": "0220",
                  "gjelderFom": null
                },
                "tilkjentYtelse": {
                  "id": "mz4pQzes94APQDZ6VYMO",
                  "utbetalingsoppdrag": null,
                  "andelerTilkjentYtelse": [],
                  "sisteAndelIKjede": null,
                  "sisteAndelPerKjede": {}
                }
              }
            }
        """.trimIndent()

        assertDoesNotThrow {
            objectMapper.readValue(json, Iverksetting::class.java)
        }
    }
}