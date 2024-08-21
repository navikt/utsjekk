package no.nav.utsjekk.utbetaling

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.utsjekk.iverksetting.api.IverksettingValidatorService
import no.nav.utsjekk.iverksetting.domene.KonsumentConfig
import no.nav.utsjekk.iverksetting.tilstand.IverksettingService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping(
    path = ["/api/utbetalinger"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
@ProtectedWithClaims(issuer = "azuread")
class UtbetalingController(
    private val iverksettingService: IverksettingService,
    private val validatorService: IverksettingValidatorService,
    private val konsumentConfig: KonsumentConfig,
) {
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @Tag(name = "Utbetaling")
    @Operation(
        summary = "Start utbetaling av vedtak",
        description = "Starter utbetaling.",
    )
    @ApiResponse(responseCode = "202", description = "utbetaling er mottatt")
    @ApiResponse(responseCode = "400", description = "ugyldig format på utbetaling")
    @ApiResponse(responseCode = "403", description = "ikke autorisert til å starte utbetaling")
    @ApiResponse(responseCode = "409", description = "utbetalingen er i konflikt med tidligere utbetaling(er)")
    fun utbetal(
        @RequestBody utbetalingDto: UtbetalingDto,
    ): ResponseEntity<UUID> {
//        utbetalingDto.valider()
//        val iverksett = IverksettV2DtoMapper.tilDomene(utbetalingDto)
//        validatorService.valider(iverksett)
//        iverksettingService.startIverksetting(iverksett)

        val utbetalingId = UUID.randomUUID()
        val location =
            ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/api/utbetalinger/$utbetalingId")
                .build()
                .toUri()
        return ResponseEntity.created(location).build()
    }
}
