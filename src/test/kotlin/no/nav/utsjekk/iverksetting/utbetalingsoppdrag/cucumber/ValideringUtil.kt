package no.nav.utsjekk.iverksetting.utbetalingsoppdrag.cucumber

import io.cucumber.datatable.DataTable
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.cucumber.domeneparser.Domenebegrep
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.cucumber.domeneparser.IdTIlUUIDHolder.behandlingIdTilUUID
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.cucumber.domeneparser.parseLong
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.domene.BeregnetUtbetalingsoppdrag
import java.util.UUID

object ValideringUtil {
    fun assertSjekkBehandlingIder(
        dataTable: DataTable,
        utbetalingsoppdrag: MutableMap<UUID, BeregnetUtbetalingsoppdrag>,
    ) {
        val eksisterendeBehandlingId =
            utbetalingsoppdrag.filter { it.value.utbetalingsoppdrag.utbetalingsperiode.isNotEmpty() }.keys
        val forventedeBehandlingId =
            dataTable.asMaps()
                .map { behandlingIdTilUUID[parseLong(Domenebegrep.BEHANDLING_ID, it).toInt()] }
                .toSet()
        val ukontrollerteBehandlingId = eksisterendeBehandlingId.filterNot { forventedeBehandlingId.contains(it) }

        if (ukontrollerteBehandlingId.isNotEmpty()) {
            error("Har ikke kontrollert behandlingene:$ukontrollerteBehandlingId")
        }
    }
}
