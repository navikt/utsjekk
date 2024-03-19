package no.nav.dagpenger.iverksett.utbetaling.api

import no.nav.dagpenger.iverksett.utbetaling.tilstand.SettMottattTidspunktForEksisterendeIverksettinger
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
class MigrerMottattTidspunktController(
    private val settMottattTidspunktForEksisterendeIverksettinger: SettMottattTidspunktForEksisterendeIverksettinger,
) {
    @PostMapping("/intern/migrer")
    fun migrerMottattTidspunkt(): ResponseEntity<String> {
        val antallMigrerte = settMottattTidspunktForEksisterendeIverksettinger.migrer()
        return ResponseEntity.ok("Migrerte $antallMigrerte iverksettinger")
    }
}
