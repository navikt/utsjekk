package no.nav.dagpenger.iverksett.api.tilgangskontroll

import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("local")
class LocalTilgangskontrollService: TilgangskontrollService {
    override fun valider(iverksett: IverksettDto) {
        // Noop
    }
}