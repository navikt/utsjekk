package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import org.springframework.http.HttpStatus

object IverksettDtoValidator {

    fun IverksettDto.valider() {
        fraOgMedKommerFørTilOgMedIUtbetalingsperioder(this)
        utbetalingsperioderOverlapperIkkeITid(this)
        utbetalingerHarKunPositiveBeløp(this)
        utbetalingerHarIngenBeløpOverMaksgrense(this)
        ingenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød(this)
        enhetErSattForTiltakspenger(this)
    }

    internal fun fraOgMedKommerFørTilOgMedIUtbetalingsperioder(iverksettDto: IverksettDto) {
        val alleErOk = iverksettDto.vedtak.utbetalinger.all {
            val fom = it.fraOgMedDato
            val tom = it.tilOgMedDato
            !tom.isBefore(fom)
        }

        if (!alleErOk) {
            throw ApiFeil(
                "Utbetalinger inneholder perioder der tilOgMedDato er før fraOgMedDato",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun utbetalingsperioderOverlapperIkkeITid(iverksettDto: IverksettDto) {
        fraOgMedKommerFørTilOgMedIUtbetalingsperioder(iverksettDto)

        val allePerioderErUavhengige = iverksettDto.vedtak.utbetalinger
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

    internal fun utbetalingerHarKunPositiveBeløp(iverksettDto: IverksettDto) {
        val alleBeløpErPositive = iverksettDto.vedtak.utbetalinger.all {
            val belop = it.belopPerDag
            belop > 0
        }

        if (!alleBeløpErPositive) {
            throw ApiFeil(
                "Det finnes utbetalinger som ikke har positivt belopPerDag",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun utbetalingerHarIngenBeløpOverMaksgrense(iverksettDto: IverksettDto) {
        val maksgrense = 5000
        val alleBeløpErUnderMaksgrense = iverksettDto.vedtak.utbetalinger.all {
            it.belopPerDag < maksgrense
        }

        if (!alleBeløpErUnderMaksgrense) {
            throw ApiFeil(
                "Det finnes utbetalinger med beløp over maksgrensen på $maksgrense kr per dag",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun ingenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød(iverksettDto: IverksettDto) {
        val ugyldigKombinasjon = iverksettDto.vedtak.utbetalinger.any {
            it.stonadstype == StønadTypeDagpenger.DAGPENGER_EOS && it.ferietillegg == Ferietillegg.AVDOD
        }

        if (ugyldigKombinasjon) {
            throw ApiFeil(
                "Ferietillegg til avdød er ikke tillatt for stønadstypen ${StønadTypeDagpenger.DAGPENGER_EOS}",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun enhetErSattForTiltakspenger(iverksettDto: IverksettDto) {
        val stønadstype = iverksettDto.vedtak.utbetalinger.firstOrNull()?.stonadstype

        if (stønadstype is StønadTypeTiltakspenger && iverksettDto.enhet == null) {
            throw ApiFeil(
                "Enhet må være satt for tiltakspenger",
                HttpStatus.BAD_REQUEST,
            )
        }
    }
}
