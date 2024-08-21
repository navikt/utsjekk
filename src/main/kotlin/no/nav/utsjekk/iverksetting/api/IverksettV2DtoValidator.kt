package no.nav.utsjekk.iverksetting.api

import no.nav.utsjekk.felles.http.advice.ApiFeil
import no.nav.utsjekk.kontrakter.felles.GyldigBehandlingId
import no.nav.utsjekk.kontrakter.felles.GyldigSakId
import no.nav.utsjekk.kontrakter.felles.Satstype
import no.nav.utsjekk.kontrakter.felles.StønadTypeDagpenger
import no.nav.utsjekk.kontrakter.iverksett.Ferietillegg
import no.nav.utsjekk.kontrakter.iverksett.IverksettV2Dto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataDagpengerDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTiltakspengerDto
import org.springframework.http.HttpStatus
import java.time.YearMonth

fun IverksettV2Dto.valider() {
    sakIdTilfredsstillerLengdebegrensning(this)
    behandlingIdTilfredsstillerLengdebegrensning(this)
    fraOgMedKommerFørTilOgMedIUtbetalingsperioder(this)
    utbetalingsperioderOverlapperIkkeITid(this)
    utbetalingsperioderSamsvarerMedSatstype(this)
    iverksettingIdSkalEntenIkkeVæreSattEllerVæreSattForNåværendeOgForrige(this)
    ingenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød(this)
    brukersNavKontorErSattForTiltakspenger(this)
}

private fun sakIdTilfredsstillerLengdebegrensning(iverksettDto: IverksettV2Dto) {
    if (iverksettDto.sakId.length !in 1..GyldigSakId.MAKSLENGDE) {
        throw ApiFeil(
            "SakId må være mellom 1 og ${GyldigSakId.MAKSLENGDE} tegn lang",
            HttpStatus.BAD_REQUEST,
        )
    }
}

private fun behandlingIdTilfredsstillerLengdebegrensning(iverksettDto: IverksettV2Dto) {
    if (iverksettDto.behandlingId.length !in 1..GyldigBehandlingId.MAKSLENGDE) {
        throw ApiFeil(
            "BehandlingId må være mellom 1 og ${GyldigBehandlingId.MAKSLENGDE} tegn lang",
            HttpStatus.BAD_REQUEST,
        )
    }
}

private fun fraOgMedKommerFørTilOgMedIUtbetalingsperioder(iverksettDto: IverksettV2Dto) {
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

private fun utbetalingsperioderOverlapperIkkeITid(iverksettDto: IverksettV2Dto) {
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

private fun utbetalingsperioderSamsvarerMedSatstype(iverksettDto: IverksettV2Dto) {
    val satstype =
        iverksettDto.vedtak.utbetalinger
            .firstOrNull()
            ?.satstype

    if (satstype == Satstype.MÅNEDLIG) {
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

private fun iverksettingIdSkalEntenIkkeVæreSattEllerVæreSattForNåværendeOgForrige(iverksettDto: IverksettV2Dto) {
    if (iverksettDto.iverksettingId != null &&
        iverksettDto.forrigeIverksetting != null &&
        iverksettDto.forrigeIverksetting?.iverksettingId == null
    ) {
        throw ApiFeil(
            "IverksettingId er satt for nåværende iverksetting, men ikke forrige iverksetting",
            HttpStatus.BAD_REQUEST,
        )
    }

    if (iverksettDto.iverksettingId == null &&
        iverksettDto.forrigeIverksetting != null &&
        iverksettDto.forrigeIverksetting?.iverksettingId != null
    ) {
        throw ApiFeil(
            "IverksettingId er satt for forrige iverksetting, men ikke nåværende iverksetting",
            HttpStatus.BAD_REQUEST,
        )
    }
}

private fun ingenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød(iverksettDto: IverksettV2Dto) {
    val ugyldigKombinasjon =
        iverksettDto.vedtak.utbetalinger.any {
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

// TODO denne valideringen kan fjernes når vi fjerner gamle IverksettDto og StønadsdataTiltakspengerDto
private fun brukersNavKontorErSattForTiltakspenger(iverksettDto: IverksettV2Dto) {
    val stønadsdata =
        iverksettDto.vedtak.utbetalinger
            .firstOrNull()
            ?.stønadsdata

    if (stønadsdata is StønadsdataTiltakspengerDto) {
        throw ApiFeil(
            "Må bruke StønadsdataTiltakspengerV2Dto sammen med IverksettV2Dto",
            HttpStatus.BAD_REQUEST,
        )
    }
}
