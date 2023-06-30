package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber

import io.cucumber.datatable.DataTable
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.domeneparser.Domenebegrep
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.domeneparser.parseLong
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.BeregnetUtbetalingsoppdrag

object ValideringUtil {

    fun assertSjekkBehandlingIder(dataTable: DataTable, utbetalingsoppdrag: MutableMap<Long, BeregnetUtbetalingsoppdrag>) {
        val eksisterendeBehandlingId = utbetalingsoppdrag.filter {
            it.value.utbetalingsoppdrag.utbetalingsperiode.isNotEmpty()
        }.keys
        val forventedeBehandlingId = dataTable.asMaps().map { parseLong(Domenebegrep.BEHANDLING_ID, it) }.toSet()
        val ukontrollerteBehandlingId = eksisterendeBehandlingId.filterNot { forventedeBehandlingId.contains(it) }
        if (ukontrollerteBehandlingId.isNotEmpty()) {
            error("Har ikke kontrollert behandlingene:$ukontrollerteBehandlingId")
        }
    }
}
