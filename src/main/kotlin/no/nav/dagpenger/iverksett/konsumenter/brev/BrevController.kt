package no.nav.dagpenger.iverksett.konsumenter.brev

import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.dagpenger.iverksett.konsumenter.brev.frittstående.FrittståendeBrevService
import no.nav.dagpenger.kontrakter.felles.FrittståendeBrevDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/brev"])
@ProtectedWithClaims(issuer = "azuread")
class BrevController(
    private val frittståendeBrevService: FrittståendeBrevService,
) {

    @PostMapping()
    @Tag(name = "Brev")
    fun distribuerFrittståendeBrev(
        @RequestBody data: FrittståendeBrevDto,
    ): ResponseEntity<Any> {
        if (data.mottakere == null) {
            frittståendeBrevService.journalførOgDistribuerBrev(data)
        } else {
            frittståendeBrevService.opprettTask(data)
        }
        return ResponseEntity.ok().build()
    }
}
