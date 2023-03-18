package no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk

import no.nav.dagpenger.iverksett.infrastruktur.transformer.toDomain
import no.nav.dagpenger.iverksett.kontrakter.iverksett.IverksettDagpengerdDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/statistikk/vedtakstatistikk"])
@ProtectedWithClaims(issuer = "azuread")
@Profile("dev", "local")
class VedtakstatistikkTestController(
    private val vedtakstatistikkService: VedtakstatistikkService,
) {

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendStatistikk(@RequestBody data: IverksettDagpengerdDto) {
        vedtakstatistikkService.sendTilKafka(data.toDomain(), null)
    }
}
