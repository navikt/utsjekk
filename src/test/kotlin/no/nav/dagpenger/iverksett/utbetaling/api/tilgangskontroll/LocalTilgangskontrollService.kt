package no.nav.dagpenger.iverksett.utbetaling.api.tilgangskontroll

import no.nav.dagpenger.iverksett.utbetaling.api.TilgangskontrollService
import no.nav.dagpenger.iverksett.utbetaling.domene.Fagsakdetaljer
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("local")
class LocalTilgangskontrollService : TilgangskontrollService {
    override fun valider(fagsakdetaljer: Fagsakdetaljer) {
        // Noop
    }
}
