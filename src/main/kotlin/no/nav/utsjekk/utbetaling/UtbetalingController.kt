package no.nav.utsjekk.utbetaling

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.utsjekk.iverksetting.api.IverksettingValidatorService
import no.nav.utsjekk.iverksetting.domene.*
import no.nav.utsjekk.iverksetting.domene.transformer.IverksettDtoMapper
import no.nav.utsjekk.iverksetting.tilstand.IverksettingService
import no.nav.utsjekk.kontrakter.felles.BrukersNavKontor
import no.nav.utsjekk.kontrakter.felles.Satstype
import no.nav.utsjekk.kontrakter.felles.StønadTypeTilleggsstønader
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.time.YearMonth
import java.util.*

@RestController
@RequestMapping(
    path = ["/api/utbetalinger"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
@ProtectedWithClaims(issuer = "azuread")
class UtbetalingController(
    private val iverksettingService: IverksettingService,
    private val validatorService: IverksettingValidatorService,
    private val iverksettDtoMapper: IverksettDtoMapper,
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
//        val iverksett = iverksettDtoMapper.tilDomene(utbetalingDto)
//        validatorService.valider(iverksett)
//        iverksettingService.startIverksetting(iverksett)

        val utbetalingId = UUID.randomUUID()
        val iverksetting = iverksetting(
            dto = utbetalingDto,
            utbetalingId = utbetalingId
        )

        datasource[utbetalingId] = iverksetting

        val location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/$utbetalingId") // relative path
            .build()
            .toUri()
        return ResponseEntity.accepted().location(location).build()
    }

    private val datasource = mutableMapOf<UUID, Iverksetting>()

    fun utbetaling(iverksetting: Iverksetting): UtbetalingDto {
        fun stønadstype(stønadsdata: Stønadsdata): Stønadstype =
            when (stønadsdata) {
                is StønadsdataTilleggsstønader -> when (stønadsdata.stønadstype) {
                    StønadTypeTilleggsstønader.TILSYN_BARN_ENSLIG_FORSØRGER -> Stønadstype.Tilleggsstønader.TILSYN_BARN_ENSLIG_FORSØRGER
                    StønadTypeTilleggsstønader.TILSYN_BARN_AAP -> Stønadstype.Tilleggsstønader.TILSYN_BARN_AAP
                    StønadTypeTilleggsstønader.TILSYN_BARN_ETTERLATTE -> Stønadstype.Tilleggsstønader.TILSYN_BARN_ETTERLATTE
                }

                is StønadsdataDagpenger -> TODO("stønadstype from stønadsdata for dagpenger not implemented")
                is StønadsdataTiltakspenger -> TODO("stønadstype from stønadsdata for tiltakspenger not implemented")
            }

        return UtbetalingDto(
            sakId = iverksetting.sakId,
            behandlingId = iverksetting.behandlingId,
            personident = iverksetting.personident,
            vedtak = UtbetalingDto.VedtakDto(
                vedtakstidspunkt = iverksetting.vedtak.vedtakstidspunkt,
                saksbehandlerId = iverksetting.vedtak.saksbehandlerId,
                beslutterId = iverksetting.vedtak.beslutterId,
                utbetalinger = iverksetting.vedtak.tilkjentYtelse.let { ytelse ->
                    ytelse.andelerTilkjentYtelse.map { andel ->
                        when (andel.satstype) {
                            Satstype.DAGLIG -> UtbetalingDto.VedtakDto.DagsatsDto(
                                beløp = andel.beløp.toUInt(),
                                stønadstype = stønadstype(andel.stønadsdata).navn(),
                                brukersNavKontor = iverksetting.vedtak.brukersNavKontor?.enhet,
                                dato = andel.periode.fom,
                            )

                            Satstype.MÅNEDLIG -> UtbetalingDto.VedtakDto.MånedsatsDto(
                                beløp = andel.beløp.toUInt(),
                                stønadstype = stønadstype(andel.stønadsdata).navn(),
                                brukersNavKontor = iverksetting.vedtak.brukersNavKontor?.enhet,
                                måned = YearMonth.of(andel.periode.fom.year, andel.periode.fom.month),
                            )

                            Satstype.ENGANGS -> UtbetalingDto.VedtakDto.EngangssatsDto(
                                beløp = andel.beløp.toUInt(),
                                stønadstype = stønadstype(andel.stønadsdata).navn(),
                                brukersNavKontor = iverksetting.vedtak.brukersNavKontor?.enhet,
                                fom = andel.periode.fom,
                                tom = andel.periode.tom,
                            )
                        }

                    }
                }
            )
        )
    }

    fun iverksetting(
        dto: UtbetalingDto,
        utbetalingId: UUID,
        utbetalingIdRef: UUID? = null
    ): Iverksetting {
        val tidligereUtbetaling = utbetalingIdRef?.let {
            datasource[utbetalingIdRef] ?: notFound("utbetaling med id $utbetalingIdRef")
        }

        val stønadstype = dto.vedtak.utbetalinger.first().stønadstype.let { Stønadstype.Tilleggsstønader.valueOf(it) }
        val navkontor = dto.vedtak.utbetalinger.first().brukersNavKontor

        fun periode(sats: UtbetalingDto.VedtakDto.SatsDto): Periode =
            when (sats) {
                is UtbetalingDto.VedtakDto.DagsatsDto -> Periode(sats.dato, sats.dato)
                is UtbetalingDto.VedtakDto.EngangssatsDto -> Periode(sats.fom, sats.tom)
                is UtbetalingDto.VedtakDto.MånedsatsDto -> Periode(sats.måned.atDay(1), sats.måned.atEndOfMonth())
            }

        fun stønadsdata(sats: UtbetalingDto.VedtakDto.SatsDto): Stønadsdata =
            when (Stønadstype.Tilleggsstønader.valueOf(sats.stønadstype)) {
                Stønadstype.Tilleggsstønader.TILSYN_BARN_ENSLIG_FORSØRGER ->
                    StønadsdataTilleggsstønader(StønadTypeTilleggsstønader.TILSYN_BARN_ENSLIG_FORSØRGER)

                Stønadstype.Tilleggsstønader.TILSYN_BARN_AAP ->
                    StønadsdataTilleggsstønader(StønadTypeTilleggsstønader.TILSYN_BARN_AAP)

                Stønadstype.Tilleggsstønader.TILSYN_BARN_ETTERLATTE ->
                    StønadsdataTilleggsstønader(StønadTypeTilleggsstønader.TILSYN_BARN_ETTERLATTE)
            }

        return Iverksetting(
            fagsak = Fagsakdetaljer(dto.sakId, stønadstype.fagsystem()),
            behandling = Behandlingsdetaljer(
                forrigeBehandlingId = tidligereUtbetaling?.behandlingId,
                forrigeIverksettingId = utbetalingIdRef.toString(),
                behandlingId = dto.behandlingId,
                iverksettingId = utbetalingId.toString(),
            ),
            søker = Søker(dto.personident),
            vedtak = Vedtaksdetaljer(
                vedtakstidspunkt = dto.vedtak.vedtakstidspunkt,
                saksbehandlerId = dto.vedtak.saksbehandlerId,
                beslutterId = dto.vedtak.beslutterId,
                brukersNavKontor = navkontor?.let { BrukersNavKontor(navkontor, null) },
                tilkjentYtelse = TilkjentYtelse(
                    andelerTilkjentYtelse = dto.vedtak.utbetalinger.map { sats ->
                        AndelTilkjentYtelse(
                            beløp = sats.beløp.toInt(),
                            periode = periode(sats),
                            satstype = sats.type,
                            stønadsdata = stønadsdata(sats),
                            periodeId = null,
                            forrigePeriodeId = null,
                        )
                    }
                )
            )
        )
    }

    @GetMapping(path = ["/{id}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun get(@PathVariable id: UUID): ResponseEntity<UtbetalingDto> {
        val iverksetting = datasource[id] ?: notFound("Utbetaling med id $id ikke funnet")
        val utbetaling = utbetaling(iverksetting)
        return ResponseEntity.ok(utbetaling)
    }

    @PostMapping(path = ["/{id}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable id: UUID,
        @RequestBody utbetaling: UtbetalingDto,
    ): ResponseEntity<Unit> {
        val utbetalingId = UUID.randomUUID()

        val iverksetting = iverksetting(
            dto = utbetaling,
            utbetalingId = utbetalingId,
            utbetalingIdRef = id
        )

        datasource[utbetalingId] = iverksetting

        val location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/$utbetalingId") // relative path
            .build()
            .toUri()

        return ResponseEntity.accepted().location(location).build()
    }
}









