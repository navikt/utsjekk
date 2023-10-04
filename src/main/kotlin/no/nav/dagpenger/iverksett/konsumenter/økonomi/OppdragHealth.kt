package no.nav.dagpenger.iverksett.konsumenter.økonomi

import no.nav.dagpenger.iverksett.infrastruktur.healthcheck.AbstractHealthIndicator
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!local")
class OppdragHealth(client: OppdragClient) :
    AbstractHealthIndicator(client, "familie.oppdrag")
