package no.nav.utsjekk.iverksetting.api

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.utsjekk.iverksetting.tilstand.IverksettingsresultatRepository
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
class KjedenøkkelMigrator(
    private val iverksettingsresultatRepository: IverksettingsresultatRepository,
) {
    @PostMapping("/intern/migrer")
    fun migrer() {
        val gamleIverksettingsresultater = iverksettingsresultatRepository.hentGammelVersjon()
        log.info("Migrerer kjedenøkkel for ${gamleIverksettingsresultater.size} iverksettingsresultater")
        val oppdaterte = gamleIverksettingsresultater.map { it.tilNy() }
        oppdaterte.forEachIndexed { index, ir ->
            iverksettingsresultatRepository.update(
                ir,
            )
            log.info("Oppdatert iverksettingsresultat #$index for behandling ${ir.behandlingId}/iverksetting ${ir.iverksettingId}")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
