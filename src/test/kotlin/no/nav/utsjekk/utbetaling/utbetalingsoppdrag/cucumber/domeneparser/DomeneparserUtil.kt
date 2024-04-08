package no.nav.utsjekk.utbetaling.utbetalingsoppdrag.cucumber.domeneparser

import io.cucumber.datatable.DataTable

interface Domenenøkkel {
    val nøkkel: String
}

enum class Domenebegrep(override val nøkkel: String) : Domenenøkkel {
    ID("Id"),
    BEHANDLING_ID("BehandlingId"),
    FRA_DATO("Fra dato"),
    TIL_DATO("Til dato"),
}

object DomeneparserUtil {
    fun DataTable.groupByBehandlingId(): Map<String, List<Map<String, String>>> =
        this.asMaps().groupBy { rad -> parseString(Domenebegrep.BEHANDLING_ID, rad) }
}
