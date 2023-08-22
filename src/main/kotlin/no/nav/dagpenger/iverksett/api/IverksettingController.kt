package no.nav.dagpenger.iverksett.api

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.dagpenger.iverksett.api.IverksettDtoValidator.valider
import no.nav.dagpenger.iverksett.api.domene.Brev
import no.nav.dagpenger.iverksett.api.tilgangskontroll.IverksettingTilgangskontrollService
import no.nav.dagpenger.iverksett.api.tilgangskontroll.TokenContext
import no.nav.dagpenger.iverksett.infrastruktur.transformer.toDomain
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.IverksettStatus
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping(
    path = ["/api/iverksetting"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
@ProtectedWithClaims(issuer = "azuread")
class IverksettingController(
    private val iverksettingService: IverksettingService,
    private val validatorService: IverksettingValidatorService,
    private val tilgangskontrollService: IverksettingTilgangskontrollService,
) {
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @Tag(name = "Iverksetting")
    @ApiResponse(responseCode = "202", description = "iverksetting er mottatt")
    @ApiResponse(responseCode = "400", description = "ugyldig iverksetting")
    fun iverksettUtenBrev(
        @Parameter(required = false, hidden = true)
        @RequestBody iverksettDto: IverksettDto,
    ): ResponseEntity<Void> {
        tilgangskontrollService.valider(iverksettDto)

        iverksettDto.valider()
        val iverksett = iverksettDto.toDomain()

        validatorService.valider(iverksett)
        validatorService.validerUtenBrev(iverksett)
        iverksettingService.startIverksetting(iverksett, null)

        return ResponseEntity.accepted().build()
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Tag(name = "Iverksetting")
    @ApiResponse(responseCode = "202", description = "iverksetting er mottatt")
    @ApiResponse(responseCode = "400", description = "ugyldig iverksetting")
    fun iverksett(
        @Parameter(required = false, hidden = true)
        @RequestPart("data") iverksettDto: IverksettDto,
        @RequestPart("fil", required = false) fil: MultipartFile?,
    ): ResponseEntity<Void> {
        tilgangskontrollService.valider(iverksettDto)

        val brev = fil?.let { Brev(it.bytes) }
        iverksettDto.valider()
        val iverksett = iverksettDto.toDomain()

        validatorService.valider(iverksett)
        validatorService.validerBrev(iverksett, brev)
        iverksettingService.startIverksetting(iverksett, brev)

        return ResponseEntity.accepted().build()
    }

    @GetMapping("{behandlingId}/status", produces = ["application/json"])
    @Tag(name = "Iverksetting")
    fun hentStatus(@PathVariable behandlingId: UUID): ResponseEntity<IverksettStatus> {
        val status = iverksettingService.utledStatus(behandlingId)
        return status?.let { ResponseEntity(status, HttpStatus.OK) } ?: ResponseEntity(null, HttpStatus.NOT_FOUND)
    }

    @PostMapping("/start-grensesnittavstemming")
    fun startGrensesnittavstemming(): ResponseEntity<Void> {
        return when (iverksettingService.lagreGrensesnittavstemmingTask()) {
            true -> ResponseEntity.accepted().build()
            false -> ResponseEntity.status(409).build()
        }
    }
}
