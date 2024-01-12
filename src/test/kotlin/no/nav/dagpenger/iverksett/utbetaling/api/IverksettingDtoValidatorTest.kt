package no.nav.dagpenger.iverksett.utbetaling.api

import no.nav.dagpenger.iverksett.felles.http.advice.ApiFeil
import java.time.LocalDate
import no.nav.dagpenger.iverksett.utbetaling.api.IverksettDtoValidator.brukersNavKontorErSattForTiltakspenger
import no.nav.dagpenger.iverksett.utbetaling.api.IverksettDtoValidator.fraOgMedKommerFørTilOgMedIUtbetalingsperioder
import no.nav.dagpenger.iverksett.utbetaling.api.IverksettDtoValidator.ingenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød
import no.nav.dagpenger.iverksett.utbetaling.api.IverksettDtoValidator.utbetalingerHarIngenBeløpOverMaksgrense
import no.nav.dagpenger.iverksett.utbetaling.api.IverksettDtoValidator.utbetalingerHarKunPositiveBeløp
import no.nav.dagpenger.iverksett.utbetaling.api.IverksettDtoValidator.utbetalingsperioderOverlapperIkkeITid
import no.nav.dagpenger.iverksett.utbetaling.util.opprettIverksettDto
import no.nav.dagpenger.iverksett.utbetaling.util.lagUtbetalingDto
import no.nav.dagpenger.kontrakter.felles.BrukersNavKontor
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.http.HttpStatus

class IverksettingDtoValidatorTest {

    @Test
    fun `skal få BAD_REQUEST hvis beløp på utbetaling er negativt`() {
        val iverksettDto = opprettIverksettDto(andelsbeløp = -5)

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            utbetalingerHarKunPositiveBeløp(iverksettDto)
        }
    }

    @Test
    fun `skal få BAD_REQUEST hvis beløp på utbetaling er null`() {
        val iverksettDto = opprettIverksettDto(andelsbeløp = 0)

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            utbetalingerHarKunPositiveBeløp(iverksettDto)
        }
    }

    @Test
    fun `skal få BAD_REQUEST hvis beløp på utbetaling er over maksgrense`() {
        val iverksettDto = opprettIverksettDto(andelsbeløp = 20000)

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            utbetalingerHarIngenBeløpOverMaksgrense(iverksettDto)
        }
    }

    @Test
    fun `skal få BAD_REQUEST hvis tom kommer før fom i utbetalingsperiode`() {
        val tmpIverksettDto = opprettIverksettDto()
        val iverksettDto = tmpIverksettDto.copy(
            vedtak = tmpIverksettDto.vedtak.copy(
                utbetalinger = listOf(
                    lagUtbetalingDto(
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
    fun `Utbetalingsperioder som overlapper skal gi BAD_REQUEST`() {
        val tmpIverksettDto = opprettIverksettDto()
        val iverksettDto = tmpIverksettDto.copy(
            vedtak = tmpIverksettDto.vedtak.copy(
                utbetalinger = listOf(
                    lagUtbetalingDto(
                        beløp = 100,
                        fraOgMed = LocalDate.of(2023, 5, 15),
                        tilOgMed = LocalDate.of(2023, 5, 30),
                    ),
                    lagUtbetalingDto(
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
    fun `Ferietillegg til avdød for stønadstype EØS skal gi BAD_REQUEST`() {
        val iverksettDto = opprettIverksettDto(stønadType = StønadTypeDagpenger.DAGPENGER_EØS, ferietillegg = Ferietillegg.AVDØD)

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            ingenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød(iverksettDto)
        }
    }

    @Test
    fun `Skal få BAD_REQUEST når brukers NAV-kontor ikke er satt for tiltakspenger`() {
        val iverksettDto = opprettIverksettDto(stønadType = StønadTypeTiltakspenger.JOBBKLUBB)

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            brukersNavKontorErSattForTiltakspenger(iverksettDto)
        }
    }

    @Test
    fun `Skal få OK når brukers NAV-kontor er satt for tiltakspenger`() {
        val iverksettDto = opprettIverksettDto(
            stønadType = StønadTypeTiltakspenger.JOBBKLUBB,
            brukersNavKontor = BrukersNavKontor("4444", LocalDate.now())
        )

        assertDoesNotThrow {
            brukersNavKontorErSattForTiltakspenger(iverksettDto)
        }
    }
}

fun assertApiFeil(httpStatus: HttpStatus, block: () -> Any) {
    try {
        block()
        fail("Forventet ApiFeil, men fikk det ikke")
    } catch (e: ApiFeil) {
        assertThat(e.httpStatus).isEqualTo(httpStatus)
    }
}
