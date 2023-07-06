package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.cucumber

import io.cucumber.datatable.DataTable
import no.nav.dagpenger.iverksett.cucumber.domeneparser.IdTIlUUIDHolder.behandlingIdTilUUID
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.cucumber.domeneparser.Domenebegrep
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.cucumber.domeneparser.parseLong
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.BeregnetUtbetalingsoppdrag
import java.util.UUID

object ValideringUtil {

    fun assertSjekkBehandlingIder(dataTable: DataTable, utbetalingsoppdrag: MutableMap<UUID, BeregnetUtbetalingsoppdrag>) {
        val eksisterendeBehandlingId = utbetalingsoppdrag.filter {
            it.value.utbetalingsoppdrag.utbetalingsperiode.isNotEmpty()
        }.keys
        val forventedeBehandlingId = dataTable.asMaps()
            .map { behandlingIdTilUUID[parseLong(Domenebegrep.BEHANDLING_ID, it).toInt()] }
            .toSet()
        val ukontrollerteBehandlingId = eksisterendeBehandlingId.filterNot { forventedeBehandlingId.contains(it) }
        if (ukontrollerteBehandlingId.isNotEmpty()) {
            error("Har ikke kontrollert behandlingene:$ukontrollerteBehandlingId")
        }
    }
}
