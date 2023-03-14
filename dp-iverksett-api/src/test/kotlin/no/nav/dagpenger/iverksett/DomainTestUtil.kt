package no.nav.dagpenger.iverksett

import no.nav.dagpenger.iverksett.iverksetting.domene.Brev
import no.nav.dagpenger.iverksett.iverksetting.domene.Iverksett
import no.nav.dagpenger.iverksett.iverksetting.domene.IverksettData
import no.nav.dagpenger.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.dagpenger.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.dagpenger.iverksett.iverksetting.domene.VedtaksperiodeOvergangsstønad
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingType
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.Datoperiode
import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import no.nav.dagpenger.iverksett.kontrakter.felles.Vedtaksresultat
import no.nav.dagpenger.iverksett.kontrakter.iverksett.AndelTilkjentYtelseDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.SimuleringDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.TilkjentYtelseDto
import no.nav.dagpenger.iverksett.util.behandlingsdetaljer
import no.nav.dagpenger.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.dagpenger.iverksett.util.vedtaksdetaljerOvergangsstønad
import no.nav.dagpenger.iverksett.økonomi.lagAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.økonomi.lagAndelTilkjentYtelseDto
import no.nav.dagpenger.iverksett.økonomi.simulering.grupperPosteringerEtterDato
import no.nav.familie.kontrakter.felles.simulering.BeriketSimuleringsresultat
import no.nav.familie.kontrakter.felles.simulering.BetalingType
import no.nav.familie.kontrakter.felles.simulering.DetaljertSimuleringResultat
import no.nav.familie.kontrakter.felles.simulering.FagOmrådeKode
import no.nav.familie.kontrakter.felles.simulering.MottakerType
import no.nav.familie.kontrakter.felles.simulering.PosteringType
import no.nav.familie.kontrakter.felles.simulering.SimuleringMottaker
import no.nav.familie.kontrakter.felles.simulering.Simuleringsoppsummering
import no.nav.familie.kontrakter.felles.simulering.Simuleringsperiode
import no.nav.familie.kontrakter.felles.simulering.SimulertPostering
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import no.nav.dagpenger.iverksett.kontrakter.iverksett.TilkjentYtelseMedMetadata as TilkjentYtelseMedMetadataDto

fun simuleringDto(
    andeler: List<AndelTilkjentYtelseDto> = listOf(lagDefaultAndeler()),
    forrigeBehandlingId: UUID? = UUID.randomUUID(),
): SimuleringDto {
    val behandlingId = UUID.fromString("4b657902-d994-11eb-b8bc-0242ac130003")
    val tilkjentYtelseMedMetaData = TilkjentYtelseMedMetadataDto(
        tilkjentYtelse = TilkjentYtelseDto(
            andelerTilkjentYtelse = andeler,
            startdato = andeler.minOfOrNull { it.periode.fomDato } ?: LocalDate.now(),
        ),
        saksbehandlerId = "saksbehandlerId",
        eksternBehandlingId = 1,
        stønadstype = StønadType.OVERGANGSSTØNAD,
        eksternFagsakId = 1,
        behandlingId = behandlingId,
        personIdent = "12345611111",
        vedtaksdato = LocalDate.of(2021, 5, 1),
    )

    return SimuleringDto(tilkjentYtelseMedMetaData, forrigeBehandlingId)
}

private fun lagDefaultAndeler() =
    lagAndelTilkjentYtelseDto(
        beløp = 15000,
        fraOgMed = LocalDate.of(2021, 1, 1),
        tilOgMed = LocalDate.of(2023, 12, 31),
        kildeBehandlingId = UUID.randomUUID(),
    )

fun detaljertSimuleringResultat(): DetaljertSimuleringResultat {
    return DetaljertSimuleringResultat(
        simuleringMottaker = listOf(
            SimuleringMottaker(
                simulertPostering = listOf(
                    SimulertPostering(
                        fagOmrådeKode = FagOmrådeKode.ENSLIG_FORSØRGER_OVERGANGSSTØNAD,
                        fom = LocalDate.of(2021, 1, 1),
                        tom = LocalDate.of(2021, 12, 31),
                        betalingType = BetalingType.DEBIT,
                        beløp = BigDecimal.valueOf(15000),
                        posteringType = PosteringType.YTELSE,
                        forfallsdato = LocalDate.of(2021, 10, 1),
                        utenInntrekk = false,
                    ),
                ),
                mottakerNummer = null,
                mottakerType = MottakerType.BRUKER,
            ),
        ),
    )
}

fun beriketSimuleringsresultat(
    feilutbetaling: BigDecimal = BigDecimal.ZERO,
    fom: LocalDate = LocalDate.of(2021, 1, 1),
    tom: LocalDate = LocalDate.of(2021, 12, 31),
) = BeriketSimuleringsresultat(
    detaljer = detaljertSimuleringResultat(),
    oppsummering = simuleringsoppsummering(feilutbetaling, fom, tom),
)

fun simuleringsoppsummering(
    feilutbetaling: BigDecimal = BigDecimal.ZERO,
    fom: LocalDate = LocalDate.of(2021, 1, 1),
    tom: LocalDate = LocalDate.of(2021, 12, 31),
) =
    Simuleringsoppsummering(
        perioder = listOf(
            Simuleringsperiode(
                fom = fom,
                tom = tom,
                forfallsdato = LocalDate.of(2021, 10, 1),
                nyttBeløp = BigDecimal.valueOf(15000),
                tidligereUtbetalt = BigDecimal.ZERO,
                resultat = BigDecimal.valueOf(15000),
                feilutbetaling = feilutbetaling,
            ),
        ),
        etterbetaling = BigDecimal.valueOf(15000),
        feilutbetaling = feilutbetaling,
        fom = fom,
        fomDatoNestePeriode = null,
        tomDatoNestePeriode = null,
        forfallsdatoNestePeriode = null,
        tidSimuleringHentet = LocalDate.now(),
        tomSisteUtbetaling = tom,
    )

fun posteringer(
    måned: YearMonth = januar(2021),
    antallMåneder: Int = 1,
    beløp: Int = 5000,
    posteringstype: PosteringType = PosteringType.YTELSE,
    betalingstype: BetalingType = if (beløp >= 0) BetalingType.DEBIT else BetalingType.KREDIT,

): List<SimulertPostering> = MutableList(antallMåneder) { index ->
    SimulertPostering(
        fagOmrådeKode = FagOmrådeKode.ENSLIG_FORSØRGER_OVERGANGSSTØNAD,
        fom = måned.plusMonths(index.toLong()).atDay(1),
        tom = måned.plusMonths(index.toLong()).atEndOfMonth(),
        betalingType = betalingstype,
        beløp = beløp.toBigDecimal(),
        posteringType = posteringstype,
        forfallsdato = måned.plusMonths(index.toLong())
            .atEndOfMonth(), // Forfallsdato i bank (dagen går til brukeren). Det sendes til banken kanskje en uke i forveien
        utenInntrekk = false,
    ) // Brukes ikke for EF
}

fun Tilbakekrevingsdetaljer.medFeilutbetaling(feilutbetaling: BigDecimal, periode: Datoperiode) =
    this.copy(
        tilbakekrevingMedVarsel =
        this.tilbakekrevingMedVarsel?.copy(
            sumFeilutbetaling = feilutbetaling,
            perioder = listOf(periode),
        ),
    )

fun Int.januar(år: Int) = LocalDate.of(år, 1, this)
fun Int.februar(år: Int) = LocalDate.of(år, 2, this)
fun Int.august(år: Int) = LocalDate.of(år, 8, this)
fun Int.november(år: Int) = LocalDate.of(år, 11, this)

fun januar(år: Int) = YearMonth.of(år, 1)
fun februar(år: Int) = YearMonth.of(år, 2)
fun mai(år: Int) = YearMonth.of(år, 5)
fun juli(år: Int) = YearMonth.of(år, 7)
fun september(år: Int) = YearMonth.of(år, 9)

fun List<SimulertPostering>.tilSimuleringsperioder() =
    grupperPosteringerEtterDato(this.tilSimuleringMottakere())

fun List<SimulertPostering>.tilSimuleringMottakere() =
    listOf(SimuleringMottaker(this, "12345678901", MottakerType.BRUKER))

fun List<SimulertPostering>.tilDetaljertSimuleringsresultat() =
    DetaljertSimuleringResultat(this.tilSimuleringMottakere())

fun lagIverksettData(
    forrigeBehandlingId: UUID? = null,
    behandlingType: BehandlingType,
    vedtaksresultat: Vedtaksresultat,
    vedtaksperioder: List<VedtaksperiodeOvergangsstønad> = emptyList(),
    erMigrering: Boolean = false,
    andelsdatoer: List<YearMonth> = emptyList(),
    årsak: BehandlingÅrsak = BehandlingÅrsak.SØKNAD,
): IverksettOvergangsstønad {
    val behandlingÅrsak = if (erMigrering) BehandlingÅrsak.MIGRERING else årsak
    return opprettIverksettOvergangsstønad(
        behandlingsdetaljer = behandlingsdetaljer(
            forrigeBehandlingId = forrigeBehandlingId,
            behandlingType = behandlingType,
            behandlingÅrsak = behandlingÅrsak,
        ),
        vedtaksdetaljer = vedtaksdetaljerOvergangsstønad(
            vedtaksresultat = vedtaksresultat,
            vedtaksperioder = vedtaksperioder,
            andeler = andelsdatoer.map {
                lagAndelTilkjentYtelse(beløp = 0, fraOgMed = it.minusMonths(1), tilOgMed = it)
            },
            startdato = andelsdatoer.minByOrNull { it } ?: YearMonth.now(),
        ),
    )
}

fun lagIverksett(iverksettData: IverksettData, brev: Brev? = null) = Iverksett(
    iverksettData.behandling.behandlingId,
    iverksettData,
    iverksettData.behandling.eksternId,
    brev,
)
