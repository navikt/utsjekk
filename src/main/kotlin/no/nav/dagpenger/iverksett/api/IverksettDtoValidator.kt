package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataDagpengerDto
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataTiltakspengerDto
import org.springframework.http.HttpStatus

object IverksettDtoValidator {

    fun IverksettDto.valider() {
        fraOgMedKommerFørTilOgMedIUtbetalingsperioder(this)
        utbetalingsperioderOverlapperIkkeITid(this)
        utbetalingerHarKunPositiveBeløp(this)
        utbetalingerHarIngenBeløpOverMaksgrense(this)
        ingenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød(this)
        brukersNavKontorErSattForTiltakspenger(this)
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
            val belop = it.beløpPerDag
            belop > 0
        }

        if (!alleBeløpErPositive) {
            throw ApiFeil(
                "Det finnes utbetalinger som ikke har positivt beløpPerDag",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun utbetalingerHarIngenBeløpOverMaksgrense(iverksettDto: IverksettDto) {
        val maksgrense = 5000
        val alleBeløpErUnderMaksgrense = iverksettDto.vedtak.utbetalinger.all {
            it.beløpPerDag < maksgrense
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
            if (it.stønadsdata is StønadsdataDagpengerDto) {
                val sd = it.stønadsdata as StønadsdataDagpengerDto
                sd.stønadstype == StønadTypeDagpenger.DAGPENGER_EØS && sd.ferietillegg == Ferietillegg.AVDØD
            } else {
                false
            }
        }

        if (ugyldigKombinasjon) {
            throw ApiFeil(
                "Ferietillegg til avdød er ikke tillatt for stønadstypen ${StønadTypeDagpenger.DAGPENGER_EØS}",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun brukersNavKontorErSattForTiltakspenger(iverksettDto: IverksettDto) {
        val stønadsdata = iverksettDto.vedtak.utbetalinger.firstOrNull()?.stønadsdata

        if (stønadsdata is StønadsdataTiltakspengerDto && iverksettDto.vedtak.brukersNavKontor == null) {
            throw ApiFeil(
                "Brukers NAV-kontor må være satt på vedtaket for tiltakspenger",
                HttpStatus.BAD_REQUEST,
            )
        }
    }
}
