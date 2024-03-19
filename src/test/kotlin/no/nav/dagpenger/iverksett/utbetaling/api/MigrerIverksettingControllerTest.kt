package no.nav.dagpenger.iverksett.utbetaling.api

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MigrerIverksettingControllerTest {
    @Test
    fun `migrerer fra gammel til ny versjon`() {
        assertEquals(nyJson.replace("\\s".toRegex(), ""), migrerJson(gammelJson))
    }

    @Language("JSON")
    private val gammelJson =
        """
        {
          "fagsak": {
            "fagsakId": {
              "id": "200000084"
            },
            "fagsystem": "TILLEGGSSTØNADER"
          },
          "behandling": {
            "forrigeBehandlingId": {
              "id": "34c9de6c-64ed-4de8-8ae4-d541ffe57f27"
            },
            "forrigeIverksettingId": "34c9de6c-64ed-4de8-8ae4-d541ffe57f27",
            "behandlingId": {
              "id": "34c9de6c-64ed-4de8-8ae4-d541ffe57f27"
            },
            "iverksettingId": "82b68950-0634-49d9-b1f1-402d4d97184a"
          },
          "søker": {
            "personident": "22418546097"
          },
          "vedtak": {
            "vedtakstidspunkt": "2024-02-26T09:45:50.466",
            "saksbehandlerId": "Z994230",
            "beslutterId": "Z994214",
            "brukersNavKontor": null,
            "tilkjentYtelse": {
              "id": "66efe95d-72bc-4dcd-b7f0-d64b1b5067b8",
              "utbetalingsoppdrag": null,
              "andelerTilkjentYtelse": [],
              "sisteAndelIKjede": null,
              "sisteAndelPerKjede": {}
            }
          },
          "forrigeIverksettingBehandlingId": {
            "id": "34c9de6c-64ed-4de8-8ae4-d541ffe57f27"
          }
        }
        """.trimIndent()

    @Language("JSON")
    private val nyJson =
        """
        {
          "fagsak": {
            "fagsakId": "200000084",
            "fagsystem": "TILLEGGSSTØNADER"
          },
          "behandling": {
            "forrigeBehandlingId": "34c9de6c-64ed-4de8-8ae4-d541ffe57f27",
            "forrigeIverksettingId": "34c9de6c-64ed-4de8-8ae4-d541ffe57f27",
            "behandlingId":  "34c9de6c-64ed-4de8-8ae4-d541ffe57f27",
            "iverksettingId": "82b68950-0634-49d9-b1f1-402d4d97184a"
          },
          "søker": {
            "personident": "22418546097"
          },
          "vedtak": {
            "vedtakstidspunkt": "2024-02-26T09:45:50.466",
            "saksbehandlerId": "Z994230",
            "beslutterId": "Z994214",
            "brukersNavKontor": null,
            "tilkjentYtelse": {
              "id": "66efe95d-72bc-4dcd-b7f0-d64b1b5067b8",
              "utbetalingsoppdrag": null,
              "andelerTilkjentYtelse": [],
              "sisteAndelIKjede": null,
              "sisteAndelPerKjede": {}
            }
          }
        }
        """.trimIndent()
}
