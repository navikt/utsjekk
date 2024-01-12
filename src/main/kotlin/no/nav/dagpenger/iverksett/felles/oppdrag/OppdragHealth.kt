package no.nav.dagpenger.iverksett.felles.oppdrag

import no.nav.dagpenger.iverksett.felles.helsesjekk.AbstractHealthIndicator
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!local")
class OppdragHealth(client: OppdragClient) :
    AbstractHealthIndicator(client, "familie.oppdrag")
