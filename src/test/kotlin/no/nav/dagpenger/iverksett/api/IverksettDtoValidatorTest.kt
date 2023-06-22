package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.api.IverksettDtoValidator.avslåttVedtakHarIkkeUtbetalinger
import no.nav.dagpenger.iverksett.api.IverksettDtoValidator.fraOgMedKommerFørTilOgMedIUtbetalingsperioder
import no.nav.dagpenger.iverksett.api.IverksettDtoValidator.ingenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød
import no.nav.dagpenger.iverksett.api.IverksettDtoValidator.utbetalingerHarKunPositiveBeløp
import no.nav.dagpenger.iverksett.api.IverksettDtoValidator.utbetalingsperioderOverlapperIkkeITid
import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagUtbetalingDto
import no.nav.dagpenger.iverksett.util.opprettIverksettDto
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDate

class IverksettDtoValidatorTest {

    @Test
    fun `Skal få BAD_REQUEST hvis vedtaksresultatet er avslått og det finnes utbetalinger`() {
        val iverksettingDto = opprettIverksettDto(
            vedtaksresultat = Vedtaksresultat.AVSLÅTT,
        )

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            avslåttVedtakHarIkkeUtbetalinger(iverksettingDto)
        }
    }

    @Test
    fun `skal få BAD_REQUEST hvis beløp på utbetaling er negativt`() {
        val iverksettDto = opprettIverksettDto(andelsbeløp = -5)

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            utbetalingerHarKunPositiveBeløp(iverksettDto)
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
        val iverksettDto = opprettIverksettDto(stønadType = StønadType.DAGPENGER_EOS, ferietillegg = Ferietillegg.AVDOD)

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            ingenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød(iverksettDto)
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
