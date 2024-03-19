package no.nav.dagpenger.iverksett.utbetaling.api

import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
class MigrerIverksettingController(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    @PostMapping("/intern/migrer")
    fun migrer(): ResponseEntity<String> {
        jdbcTemplate.query(
            "select id, data from iverksetting",
        ) { rs, _ -> rs.getInt("id") to rs.getString("data") }
            .map { (id, data) ->
                id to migrerJson(data)
            }
            .forEach { (id, data) ->
                jdbcTemplate.update(
                    "update iverksetting set data = to_json(:data::json) where id = :id",
                    mapOf("id" to id, "data" to data),
                )
            }

        return ResponseEntity.status(HttpStatus.OK).build()
    }
}

fun migrerJson(raw: String): String {
    val json = objectMapper.readTree(raw) as ObjectNode
    (json.get("fagsak") as ObjectNode).also {
        it.replace("fagsakId", it.get("fagsakId").get("id"))
    }
    (json.get("behandling") as ObjectNode).also {
        it.replace("forrigeBehandlingId", it.get("forrigeBehandlingId").get("id"))
        it.replace("behandlingId", it.get("behandlingId").get("id"))
    }
    json.remove("forrigeIverksettingBehandlingId")
    return objectMapper.writeValueAsString(json)
}
