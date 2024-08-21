package no.nav.utsjekk.iverksetting.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.utsjekk.iverksetting.api.IverksettDtoValidator.valider
import no.nav.utsjekk.iverksetting.api.IverksettTilleggsstønaderDtoValidator.valider
import no.nav.utsjekk.iverksetting.domene.KonsumentConfig
import no.nav.utsjekk.iverksetting.domene.transformer.IverksettDtoMapper
import no.nav.utsjekk.iverksetting.domene.transformer.IverksettV2DtoMapper
import no.nav.utsjekk.iverksetting.domene.transformer.toDomain
import no.nav.utsjekk.iverksetting.tilstand.IverksettingService
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.iverksett.IverksettDto
import no.nav.utsjekk.kontrakter.iverksett.IverksettStatus
import no.nav.utsjekk.kontrakter.iverksett.IverksettTilleggsstønaderDto
import no.nav.utsjekk.kontrakter.iverksett.IverksettV2Dto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    path = ["/api/iverksetting"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
@ProtectedWithClaims(issuer = "azuread")
@Tag(name = "Iverksetting")
class IverksettingController(
    private val iverksettingService: IverksettingService,
    private val validatorService: IverksettingValidatorService,
    private val iverksettDtoMapper: IverksettDtoMapper,
    private val konsumentConfig: KonsumentConfig,
) {
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Start iverksetting av vedtak",
        description = "Iverksetter utbetaling.",
        deprecated = true,
    )
    @ApiResponse(responseCode = "202", description = "iverksetting er mottatt")
    @ApiResponse(responseCode = "400", description = "ugyldig format på iverksetting")
    @ApiResponse(responseCode = "403", description = "ikke autorisert til å starte iverksetting")
    @ApiResponse(responseCode = "409", description = "iverksetting er i konflikt med tidligere iverksetting")
    fun iverksett(
        @RequestBody iverksettDto: IverksettDto,
    ): ResponseEntity<Void> {
        iverksettDto.valider()
        val iverksett = iverksettDtoMapper.tilDomene(iverksettDto)
        validatorService.valider(iverksett)
        iverksettingService.startIverksetting(iverksett)

        return ResponseEntity.accepted().build()
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/tilleggsstonader"])
    @Operation(
        summary = "Start iverksetting av vedtak",
        description = "Iverksetter utbetaling for tilleggsstønader.",
        deprecated = true,
    )
    @ApiResponse(responseCode = "202", description = "iverksetting er mottatt")
    @ApiResponse(responseCode = "400", description = "ugyldig format på iverksetting")
    @ApiResponse(responseCode = "403", description = "ikke autorisert til å starte iverksetting")
    @ApiResponse(responseCode = "409", description = "iverksetting er i konflikt med tidligere iverksetting")
    fun iverksettTilleggsstønader(
        @RequestBody iverksettDto: IverksettTilleggsstønaderDto,
    ): ResponseEntity<Void> {
        iverksettDto.valider()
        val iverksett = iverksettDto.toDomain()
        validatorService.valider(iverksett)
        iverksettingService.startIverksetting(iverksett)

        return ResponseEntity.accepted().build()
    }

    @PostMapping("/v2", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Start iverksetting av vedtak",
        description = "Iverksetter utbetaling.",
    )
    @ApiResponse(responseCode = "202", description = "iverksetting er mottatt")
    @ApiResponse(responseCode = "400", description = "ugyldig format på iverksetting")
    @ApiResponse(responseCode = "403", description = "ikke autorisert til å starte iverksetting")
    @ApiResponse(responseCode = "409", description = "iverksetting er i konflikt med tidligere iverksetting")
    fun iverksett(
        @RequestBody iverksettDto: IverksettV2Dto,
    ): ResponseEntity<Void> {
        iverksettDto.valider()
        val fagsystem = konsumentConfig.finnFagsystem(TokenContext.hentKlientnavn())
        val iverksett = IverksettV2DtoMapper.tilDomene(iverksettDto, fagsystem)
        validatorService.valider(iverksett)
        iverksettingService.startIverksetting(iverksett)

        return ResponseEntity.accepted().build()
    }

    @GetMapping("{sakId}/{behandlingId}/status", produces = ["application/json"])
    @Operation(summary = "Sjekk status på iverksetting med gitt behandlingId")
    @ApiResponse(responseCode = "200", description = "Status returnert i body")
    @ApiResponse(responseCode = "404", description = "Kunne ikke finne iverksetting")
    fun hentStatus(
        @PathVariable sakId: String,
        @PathVariable behandlingId: String,
    ): ResponseEntity<IverksettStatus> {
        val fagsystem = konsumentConfig.finnFagsystem(TokenContext.hentKlientnavn())
        val status =
            iverksettingService.utledStatus(
                fagsystem = fagsystem,
                sakId = sakId,
                behandlingId = behandlingId,
                iverksettingId = null,
            )
        return status?.let { ResponseEntity(status, HttpStatus.OK) } ?: ResponseEntity(null, HttpStatus.NOT_FOUND)
    }

    @GetMapping("{sakId}/{behandlingId}/{iverksettingId}/status", produces = ["application/json"])
    @Operation(summary = "Sjekk status på iverksetting med gitt behandlingId og iverksettingId")
    @ApiResponse(responseCode = "200", description = "Status returnert i body")
    @ApiResponse(responseCode = "404", description = "Kunne ikke finne iverksetting")
    fun hentStatus(
        @PathVariable sakId: String,
        @PathVariable behandlingId: String,
        @PathVariable iverksettingId: String,
    ): ResponseEntity<IverksettStatus> {
        val status =
            iverksettingService.utledStatus(
                fagsystem = Fagsystem.TILLEGGSSTØNADER,
                sakId = sakId,
                behandlingId = behandlingId,
                iverksettingId = iverksettingId,
            )
        return status?.let { ResponseEntity(status, HttpStatus.OK) } ?: ResponseEntity(null, HttpStatus.NOT_FOUND)
    }
}
