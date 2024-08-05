package no.nav.utsjekk.iverksetting.api

import no.nav.utsjekk.felles.http.advice.ApiFeil
import no.nav.utsjekk.iverksetting.api.IverksettDtoValidator.behandlingIdTilfredsstillerLengdebegrensning
import no.nav.utsjekk.iverksetting.api.IverksettDtoValidator.brukersNavKontorErSattForTiltakspenger
import no.nav.utsjekk.iverksetting.api.IverksettDtoValidator.fraOgMedKommerFørTilOgMedIUtbetalingsperioder
import no.nav.utsjekk.iverksetting.api.IverksettDtoValidator.ingenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød
import no.nav.utsjekk.iverksetting.api.IverksettDtoValidator.sakIdTilfredsstillerLengdebegrensning
import no.nav.utsjekk.iverksetting.api.IverksettDtoValidator.utbetalingerHarIngenBeløpOverMaksgrense
import no.nav.utsjekk.iverksetting.api.IverksettDtoValidator.utbetalingerHarKunPositiveBeløp
import no.nav.utsjekk.iverksetting.api.IverksettDtoValidator.utbetalingsperioderOverlapperIkkeITid
import no.nav.utsjekk.iverksetting.domene.transformer.RandomOSURId
import no.nav.utsjekk.iverksetting.util.enIverksettDto
import no.nav.utsjekk.iverksetting.util.enUtbetalingDto
import no.nav.utsjekk.kontrakter.felles.BrukersNavKontor
import no.nav.utsjekk.kontrakter.felles.StønadTypeDagpenger
import no.nav.utsjekk.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.utsjekk.kontrakter.iverksett.Ferietillegg
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTiltakspengerDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.http.HttpStatus
import java.time.LocalDate

class IverksettingDtoValidatorTest {
    @Test
    fun `skal få BAD_REQUEST hvis sakId er for lang`() {
        val iverksettDto = enIverksettDto(sakId = RandomOSURId.generate() + RandomOSURId.generate())

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            sakIdTilfredsstillerLengdebegrensning(iverksettDto)
        }
    }

    @Test
    fun `skal få BAD_REQUEST hvis behandlingId er for lang`() {
        val iverksettDto = enIverksettDto(behandlingId = RandomOSURId.generate() + RandomOSURId.generate())

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            behandlingIdTilfredsstillerLengdebegrensning(iverksettDto)
        }
    }

    @Test
    fun `skal få BAD_REQUEST hvis beløp på utbetaling er negativt`() {
        val iverksettDto = enIverksettDto(andelsbeløp = -5)

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            utbetalingerHarKunPositiveBeløp(iverksettDto)
        }
    }

    @Test
    fun `skal få BAD_REQUEST hvis beløp på utbetaling er null`() {
        val iverksettDto = enIverksettDto(andelsbeløp = 0)

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            utbetalingerHarKunPositiveBeløp(iverksettDto)
        }
    }

    @Test
    fun `skal få BAD_REQUEST hvis beløp på utbetaling er over maksgrense`() {
        val iverksettDto = enIverksettDto(andelsbeløp = 20000)

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            utbetalingerHarIngenBeløpOverMaksgrense(iverksettDto)
        }
    }

    @Test
    fun `skal få BAD_REQUEST hvis tom kommer før fom i utbetalingsperiode`() {
        val tmpIverksettDto = enIverksettDto()
        val iverksettDto =
            tmpIverksettDto.copy(
                vedtak =
                    tmpIverksettDto.vedtak.copy(
                        utbetalinger =
                            listOf(
                                enUtbetalingDto(
                                    beløp = 100,
                                    fraOgMed = LocalDate.of(2023, 5, 15),
                                    tilOgMed = LocalDate.of(2023, 5, 5),
                                ),
                            ),
                    ),
            )

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            fraOgMedKommerFørTilOgMedIUtbetalingsperioder(iverksettDto)
        }
    }

    @Test
    fun `Utbetalingsperioder med lik stønadsdata som overlapper skal gi BAD_REQUEST`() {
        val tmpIverksettDto = enIverksettDto()
        val iverksettDto =
            tmpIverksettDto.copy(
                vedtak =
                    tmpIverksettDto.vedtak.copy(
                        utbetalinger =
                            listOf(
                                enUtbetalingDto(
                                    beløp = 100,
                                    fraOgMed = LocalDate.of(2023, 5, 15),
                                    tilOgMed = LocalDate.of(2023, 5, 30),
                                ),
                                enUtbetalingDto(
                                    beløp = 100,
                                    fraOgMed = LocalDate.of(2023, 5, 20),
                                    tilOgMed = LocalDate.of(2023, 6, 3),
                                ),
                            ),
                    ),
            )

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            utbetalingsperioderOverlapperIkkeITid(iverksettDto)
        }
    }

    @Test
    fun `Utbetalingsperioder med ulik stønadsdata som overlapper skal ikke gi ApiFeil`() {
        val tmpIverksettDto = enIverksettDto()
        val iverksettDto =
            tmpIverksettDto.copy(
                vedtak =
                    tmpIverksettDto.vedtak.copy(
                        utbetalinger =
                            listOf(
                                enUtbetalingDto(
                                    beløp = 100,
                                    fraOgMed = LocalDate.of(2023, 5, 15),
                                    tilOgMed = LocalDate.of(2023, 5, 30),
                                    stønadsdata = StønadsdataTiltakspengerDto(stønadstype = StønadTypeTiltakspenger.JOBBKLUBB),
                                ),
                                enUtbetalingDto(
                                    beløp = 100,
                                    fraOgMed = LocalDate.of(2023, 5, 20),
                                    tilOgMed = LocalDate.of(2023, 6, 3),
                                    stønadsdata =
                                        StønadsdataTiltakspengerDto(
                                            stønadstype = StønadTypeTiltakspenger.JOBBKLUBB,
                                            barnetillegg = true,
                                        ),
                                ),
                            ),
                    ),
            )

        assertDoesNotThrow {
            utbetalingsperioderOverlapperIkkeITid(iverksettDto)
        }
    }

    @Test
    fun `Ferietillegg til avdød for stønadstype EØS skal gi BAD_REQUEST`() {
        val iverksettDto = enIverksettDto(stønadType = StønadTypeDagpenger.DAGPENGER_EØS, ferietillegg = Ferietillegg.AVDØD)

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            ingenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød(iverksettDto)
        }
    }

    @Test
    fun `Skal få BAD_REQUEST når brukers NAV-kontor ikke er satt for tiltakspenger`() {
        val iverksettDto = enIverksettDto(stønadType = StønadTypeTiltakspenger.JOBBKLUBB)

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            brukersNavKontorErSattForTiltakspenger(iverksettDto)
        }
    }

    @Test
    fun `Skal få OK når brukers NAV-kontor er satt for tiltakspenger`() {
        val iverksettDto =
            enIverksettDto(
                stønadType = StønadTypeTiltakspenger.JOBBKLUBB,
                brukersNavKontor = BrukersNavKontor("4444", LocalDate.now()),
            )

        assertDoesNotThrow {
            brukersNavKontorErSattForTiltakspenger(iverksettDto)
        }
    }
}

fun assertApiFeil(
    httpStatus: HttpStatus,
    block: () -> Any,
) {
    try {
        block()
        error("Forventet ApiFeil, men fikk det ikke")
    } catch (e: ApiFeil) {
        assertEquals(httpStatus, e.httpStatus)
    }
}
