package no.nav.dagpenger.iverksett

import no.nav.dagpenger.iverksett.api.domene.Brev
import no.nav.dagpenger.iverksett.api.domene.Iverksett
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.Tilbakekrevingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.VedtaksperiodeDagpenger
import no.nav.dagpenger.iverksett.api.domene.behandlingId
import no.nav.dagpenger.iverksett.infrastruktur.util.behandlingsdetaljer
import no.nav.dagpenger.iverksett.infrastruktur.util.opprettIverksettDagpenger
import no.nav.dagpenger.iverksett.infrastruktur.util.vedtaksdetaljerDagpenger
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottakere
import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagUtbetalingDto
import no.nav.dagpenger.iverksett.konsumenter.økonomi.simulering.grupperPosteringerEtterDato
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.iverksett.BehandlingType
import no.nav.dagpenger.kontrakter.iverksett.BehandlingÅrsak
import no.nav.dagpenger.kontrakter.iverksett.SimuleringDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import no.nav.dagpenger.kontrakter.oppdrag.simulering.BeriketSimuleringsresultat
import no.nav.dagpenger.kontrakter.oppdrag.simulering.BetalingType
import no.nav.dagpenger.kontrakter.oppdrag.simulering.DetaljertSimuleringResultat
import no.nav.dagpenger.kontrakter.oppdrag.simulering.FagOmrådeKode
import no.nav.dagpenger.kontrakter.oppdrag.simulering.MottakerType
import no.nav.dagpenger.kontrakter.oppdrag.simulering.PosteringType
import no.nav.dagpenger.kontrakter.oppdrag.simulering.SimuleringMottaker
import no.nav.dagpenger.kontrakter.oppdrag.simulering.Simuleringsoppsummering
import no.nav.dagpenger.kontrakter.oppdrag.simulering.Simuleringsperiode
import no.nav.dagpenger.kontrakter.oppdrag.simulering.SimulertPostering
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID

fun simuleringDto(
    andeler: List<UtbetalingDto> = listOf(lagDefaultAndeler()),
    forrigeBehandlingId: UUID? = UUID.randomUUID(),
): SimuleringDto {
    val behandlingId = UUID.fromString("4b657902-d994-11eb-b8bc-0242ac130003")

    return SimuleringDto(
        utbetalinger = andeler,
        saksbehandlerId = "saksbehandlerId",
        eksternBehandlingId = 1,
        stønadstype = StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER,
        sakId = UUID.randomUUID(),
        behandlingId = behandlingId,
        personIdent = "12345611111",
        vedtaksdato = LocalDate.of(2021, 5, 1),
        forrigeBehandlingId = forrigeBehandlingId,
    )
}

private fun lagDefaultAndeler() =
    lagUtbetalingDto(
        beløp = 15000,
        fraOgMed = LocalDate.of(2021, 1, 1),
        tilOgMed = LocalDate.of(2023, 12, 31),
    )

fun detaljertSimuleringResultat(): DetaljertSimuleringResultat {
    return DetaljertSimuleringResultat(
        simuleringMottaker = listOf(
            SimuleringMottaker(
                simulertPostering = listOf(
                    SimulertPostering(
                        fagOmrådeKode = FagOmrådeKode.DAGPENGER,
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
        fagOmrådeKode = FagOmrådeKode.DAGPENGER,
        fom = måned.plusMonths(index.toLong()).atDay(1),
        tom = måned.plusMonths(index.toLong()).atEndOfMonth(),
        betalingType = betalingstype,
        beløp = beløp.toBigDecimal(),
        posteringType = posteringstype,
        forfallsdato = måned.plusMonths(index.toLong())
            .atEndOfMonth(), // Forfallsdato i bank (dagen går til brukeren). Det sendes til banken kanskje en uke i forveien
        utenInntrekk = false,
    )
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

fun Int.mai(år: Int) = LocalDate.of(år, 5, this)

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
    forrigeIverksetting: IverksettDagpenger? = null,
    forrigeBehandlingId: UUID? = forrigeIverksetting?.behandlingId,
    behandlingType: BehandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
    vedtaksresultat: Vedtaksresultat = Vedtaksresultat.INNVILGET,
    vedtaksperioder: List<VedtaksperiodeDagpenger> = emptyList(),
    erMigrering: Boolean = false,
    andelsdatoer: List<LocalDate> = emptyList(),
    beløp: Int = 100,
    årsak: BehandlingÅrsak = BehandlingÅrsak.SØKNAD,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    brevmottakere: Brevmottakere = Brevmottakere(emptyList()),
): IverksettDagpenger {
    val behandlingÅrsak = if (erMigrering) BehandlingÅrsak.MIGRERING else årsak
    return opprettIverksettDagpenger(
        behandlingsdetaljer = behandlingsdetaljer(
            forrigeBehandlingId = forrigeBehandlingId,
            behandlingType = behandlingType,
            behandlingÅrsak = behandlingÅrsak,
        ),
        vedtaksdetaljer = vedtaksdetaljerDagpenger(
            vedtaksresultat = vedtaksresultat,
            vedtaksperioder = vedtaksperioder,
            andeler = andelsdatoer.map {
                lagAndelTilkjentYtelse(beløp = beløp, fraOgMed = it, tilOgMed = it)
            },
            startdato = andelsdatoer.minByOrNull { it } ?: LocalDate.now(),
            vedtakstidspunkt = vedtakstidspunkt,
            brevmottakere = brevmottakere,
        ),
    )
}

fun lagIverksett(iverksettData: IverksettDagpenger, brev: Brev? = null) = Iverksett(
    iverksettData.behandling.behandlingId,
    iverksettData,
    brev,
)
