package no.nav.dagpenger.iverksett.kontrakter.felles

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class Behandlingstema(@JsonValue val value: String) {
    Dagpenger("abXXXX"), //TODO Må finnes
    Feilutbetaling("ab0006"),
    Tilbakebetaling("ab0007"), // Tilbakekreving
    Klage("ae0058"),
    ;

    companion object {
        private val behandlingstemaMap = values().associateBy(Behandlingstema::value) + values().associateBy { it.name }

        @JvmStatic
        @JsonCreator
        fun fromValue(value: String): Behandlingstema {
            return behandlingstemaMap[value] ?: throw error("Fant ikke Behandlingstema for value=$value")
        }
    }
}
