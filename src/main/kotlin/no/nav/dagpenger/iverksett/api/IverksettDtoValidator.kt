package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import org.springframework.http.HttpStatus
import java.lang.IllegalArgumentException

object IverksettDtoValidator {

    fun IverksettDto.valider() {
        validerAtInnvilgetUtbetalingsvedtakHarUtbetalinger(this)
        validerAtAvslåttVedtakIkkeHarUtbetalinger(this)
        validerAtFraOgMedKommerFørTilOgMedIUtbetalingsperioder(this)
        validerAtUtbetalingsperioderIkkeOverlapperITid(this)
        validerAtUtbetalingerBareHarPositiveBeløp(this)
        validerAtIngenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød(this)
    }

    internal fun validerAtInnvilgetUtbetalingsvedtakHarUtbetalinger(iverksettDto: IverksettDto) {
        if (iverksettDto.vedtak.utbetalinger.isEmpty() &&
            iverksettDto.vedtak.resultat == Vedtaksresultat.INNVILGET &&
            iverksettDto.vedtak.vedtakstype == VedtakType.UTBETALINGSVEDTAK
        ) {
            throw ApiFeil(
                "Kan ikke ha iverksetting av utbertaliingsvedtak uten utbetalinger",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun validerAtAvslåttVedtakIkkeHarUtbetalinger(iverksettDto: IverksettDto) {
        if (iverksettDto.vedtak.utbetalinger.isNotEmpty() &&
            iverksettDto.vedtak.resultat == Vedtaksresultat.AVSLÅTT
        ) {
            throw ApiFeil(
                "Kan ikke ha iverksetting med utbetalinger når vedtaket er avslått",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun validerAtFraOgMedKommerFørTilOgMedIUtbetalingsperioder(iverksettDto: IverksettDto) {
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

    internal fun validerAtUtbetalingsperioderIkkeOverlapperITid(iverksettDto: IverksettDto) {
        validerAtFraOgMedKommerFørTilOgMedIUtbetalingsperioder(iverksettDto)

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

    internal fun validerAtUtbetalingerBareHarPositiveBeløp(iverksettDto: IverksettDto) {
        val alleBeløpErPositive = iverksettDto.vedtak.utbetalinger.all {
            val belop = it.belopPerDag ?: it.beløp ?: throw IllegalArgumentException("Fant hverken belopPerDag eller beløp")
            belop > 0
        }

        if (!alleBeløpErPositive) {
            throw ApiFeil(
                "Det finnes utbetalinger ikke har positivt belopPerDag",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun validerAtIngenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød(iverksettDto: IverksettDto) {
        val ugyldigKombinasjon = iverksettDto.vedtak.utbetalinger.any {
            it.stonadstype == StønadType.DAGPENGER_EOS && it.ferietillegg == Ferietillegg.AVDOD
        }

        if (ugyldigKombinasjon) {
            throw ApiFeil(
                "Ferietillegg til avdød er ikke tillatt for stønadstypen ${StønadType.DAGPENGER_EOS}",
                HttpStatus.BAD_REQUEST,
            )
        }
    }
}
