package no.nav.dagpenger.iverksett.utbetaling.api

import no.nav.dagpenger.iverksett.felles.http.advice.ApiFeil
import org.springframework.http.HttpStatus
import java.time.YearMonth

object IverksettTilleggsstønaderDtoValidator {
    fun IverksettTilleggsstønaderDto.valider() {
        fraOgMedKommerFørTilOgMedIUtbetalingsperioder(this)
        utbetalingsperioderOverlapperIkkeITid(this)
        utbetalingerHarKunPositiveBeløp(this)
        utbetalingsperioderSamsvarerMedSatstype(this)
        iverksettingIdSkalEntenIkkeVæreSattEllerVæreSattForNåværendeOgForrige(this)
    }

    private fun fraOgMedKommerFørTilOgMedIUtbetalingsperioder(iverksettDto: IverksettTilleggsstønaderDto) {
        val alleErOk =
            iverksettDto.vedtak.utbetalinger.all {
                !it.tilOgMedDato.isBefore(it.fraOgMedDato)
            }

        if (!alleErOk) {
            throw ApiFeil(
                "Utbetalinger inneholder perioder der tilOgMedDato er før fraOgMedDato",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    private fun utbetalingsperioderOverlapperIkkeITid(iverksettDto: IverksettTilleggsstønaderDto) {
        val allePerioderErUavhengige =
            iverksettDto.vedtak.utbetalinger
                .sortedBy { it.fraOgMedDato }
                .windowed(2, 1, false) {
                    val førstePeriodeTom = it.first().tilOgMedDato
                    val sistePeriodeFom = it.last().fraOgMedDato

                    førstePeriodeTom.isBefore(sistePeriodeFom)
                }.all { it }

        if (!allePerioderErUavhengige) {
            throw ApiFeil(
                "Utbetalinger inneholder perioder som overlapper i tid",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    private fun utbetalingerHarKunPositiveBeløp(iverksettDto: IverksettTilleggsstønaderDto) {
        val alleBeløpErPositive = iverksettDto.vedtak.utbetalinger.all { it.beløp > 0 }

        if (!alleBeløpErPositive) {
            throw ApiFeil(
                "Det finnes utbetalinger som ikke har positivt beløpPerDag",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    private fun utbetalingsperioderSamsvarerMedSatstype(iverksettDto: IverksettTilleggsstønaderDto) {
        val satstype = iverksettDto.vedtak.utbetalinger.firstOrNull()?.satstype

        if (satstype == Satstype.MND) {
            val alleFomErStartenAvMåned = iverksettDto.vedtak.utbetalinger.all { it.fraOgMedDato.dayOfMonth == 1 }
            val alleTomErSluttenAvMåned =
                iverksettDto.vedtak.utbetalinger.all {
                    val sisteDag = YearMonth.from(it.tilOgMedDato).atEndOfMonth().dayOfMonth
                    it.tilOgMedDato.dayOfMonth == sisteDag
                }

            if (!(alleTomErSluttenAvMåned && alleFomErStartenAvMåned)) {
                throw ApiFeil(
                    "Det finnes utbetalinger med månedssats der periodene ikke samsvarer med hele måneder",
                    HttpStatus.BAD_REQUEST,
                )
            }
        }
    }

    private fun iverksettingIdSkalEntenIkkeVæreSattEllerVæreSattForNåværendeOgForrige(iverksettDto: IverksettTilleggsstønaderDto) {
        if (iverksettDto.iverksettingId != null && iverksettDto.forrigeIverksetting != null &&
            iverksettDto.forrigeIverksetting.iverksettingId == null
        ) {
            throw ApiFeil(
                "IverksettingId er satt for nåværende iverksetting, men ikke forrige iverksetting",
                HttpStatus.BAD_REQUEST,
            )
        }

        if (iverksettDto.iverksettingId == null && iverksettDto.forrigeIverksetting != null &&
            iverksettDto.forrigeIverksetting.iverksettingId != null
        ) {
            throw ApiFeil(
                "IverksettingId er satt for forrige iverksetting, men ikke nåværende iverksetting",
                HttpStatus.BAD_REQUEST,
            )
        }
    }
}
