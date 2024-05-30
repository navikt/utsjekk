package no.nav.utsjekk.iverksetting.utbetalingsoppdrag.cucumber.domeneparser

import java.util.UUID

object IdTIlUUIDHolder {
    val behandlingIdTilUUID = (1..10).associateWith { UUID.randomUUID() }
}
