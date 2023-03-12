package no.nav.dagpenger.iverksett.cucumber.domeneparser

enum class Domenebegrep(override val nøkkel: String) : Domenenøkkel {
    BEHANDLING_ID("BehandlingId"),
    FRA_DATO("Fra dato"),
    TIL_DATO("Til dato"),
}
