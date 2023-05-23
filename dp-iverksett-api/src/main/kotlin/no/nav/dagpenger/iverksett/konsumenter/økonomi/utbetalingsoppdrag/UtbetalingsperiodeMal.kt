package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag

import no.nav.dagpenger.iverksett.api.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelseMedMetaData
import no.nav.dagpenger.kontrakter.utbetaling.Opphør
import no.nav.dagpenger.kontrakter.utbetaling.StønadType
import no.nav.dagpenger.kontrakter.utbetaling.Utbetalingsperiode
import no.nav.dagpenger.kontrakter.utbetaling.tilKlassifisering
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * * Lager utbetalingsperioder som legges på utbetalingsoppdrag. En utbetalingsperiode tilsvarer linjer hos økonomi
 *
 * @param[andel] andel som skal mappes til periode
 * @param[opphørKjedeFom] fom-dato fra tidligste periode i kjede med endring
 * @param[erEndringPåEksisterendePeriode] ved true vil oppdrag sette asksjonskode ENDR på linje og ikke referere bakover
 * @return Periode til utbetalingsoppdrag
 */
fun lagPeriodeFraAndel(
    andel: AndelTilkjentYtelse,
    type: StønadType,
    behandlingId: UUID,
    vedtaksdato: LocalDate,
    personIdent: String,
    opphørKjedeFom: LocalDate? = null,
    erEndringPåEksisterendePeriode: Boolean = false,
) =
    Utbetalingsperiode(
        erEndringPåEksisterendePeriode = erEndringPåEksisterendePeriode,
        opphør = if (erEndringPåEksisterendePeriode) Opphør(opphørKjedeFom!!) else null,
        forrigePeriodeId = andel.forrigePeriodeId,
        periodeId = andel.periodeId!!,
        datoForVedtak = vedtaksdato,
        klassifisering = type.tilKlassifisering(),
        vedtakdatoFom = andel.periode.fom,
        vedtakdatoTom = andel.periode.tom,
        sats = BigDecimal(andel.beløp),
        satsType = mapSatstype(type),
        utbetalesTil = personIdent,
        behandlingId = behandlingId,
        utbetalingsgrad = andel.utbetalingsgrad(),
    )

fun lagUtbetalingsperiodeForOpphør(
    sisteAndelIKjede: AndelTilkjentYtelse,
    opphørKjedeFom: LocalDate,
    tilkjentYtelse: TilkjentYtelseMedMetaData,
): Utbetalingsperiode {
    return lagPeriodeFraAndel(
        andel = sisteAndelIKjede,
        behandlingId = tilkjentYtelse.behandlingId,
        type = tilkjentYtelse.stønadstype,
        personIdent = tilkjentYtelse.personIdent,
        vedtaksdato = tilkjentYtelse.vedtaksdato,
        opphørKjedeFom = opphørKjedeFom,
        erEndringPåEksisterendePeriode = true,
    )
}

fun mapSatstype(stønadstype: StønadType) = when (stønadstype) {
    StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER,
    StønadType.DAGPENGER_PERMITTERING_ORDINAER,
    StønadType.DAGPENGER_PERMITTERING_FISKEINDUSTRI,
    StønadType.DAGPENGER_EOS,
    -> Utbetalingsperiode.SatsType.DAG
}
