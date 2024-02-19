package no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.cucumber.domeneparser

import java.util.UUID

object IdTIlUUIDHolder {
    val behandlingIdTilUUID = (1..10).associateWith { UUID.randomUUID() }
}
