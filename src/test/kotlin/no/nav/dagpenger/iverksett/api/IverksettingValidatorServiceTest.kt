package no.nav.dagpenger.iverksett.api

import io.mockk.mockk
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagAndelTilkjentYtelseDto
import no.nav.dagpenger.iverksett.util.opprettIverksettDto
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDate

class IverksettingValidatorServiceTest {

    lateinit var iverksettService: IverksettingService
    lateinit var iverksettResultatService: IverksettResultatService
    lateinit var validatorService: IverksettingValidatorService

    @BeforeEach
    fun init() {
        iverksettResultatService = mockk()
        iverksettService = mockk()

        validatorService = IverksettingValidatorService(
            iverksettResultatService = iverksettResultatService,
            iverksettingService = iverksettService,
            featureToggleService = mockk(relaxed = true),
        )
    }

    @Test
    fun `Skal få BAD_REQUEST hvis vedtaksresultatet er avslått og det finnes utbetalinger`() {
        val iverksettingDto = opprettIverksettDto(
            vedtaksresultat = Vedtaksresultat.AVSLÅTT,
        )

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            validatorService.validerAtAvslåttVedtakIkkeHarUtbetalinger(iverksettingDto)
        }
    }

    @Test
    fun `skal få BAD_REQUEST hvis beløp på utbetaling er negativt`() {
        val iverksettDto = opprettIverksettDto(andelsbeløp = -5)

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            validatorService.validerAtUtbetalingerBareHarPositiveBeløp(iverksettDto)
        }
    }

    @Test
    fun `skal få BAD_REQUEST hvis tom kommer før fom i utbetalingsperiode`() {
        val tmpIverksettDto = opprettIverksettDto()
        val iverksettDto = tmpIverksettDto.copy(
            vedtak = tmpIverksettDto.vedtak.copy(
                utbetalinger = listOf(
                    lagAndelTilkjentYtelseDto(
                        beløp = 100,
                        fraOgMed = LocalDate.of(2023, 5, 15),
                        tilOgMed = LocalDate.of(2023, 5, 5),
                    ),
                ),
            ),
        )

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            validatorService.validerAtFraOgMedKommerFørTilOgMedIUtbetalingsperioder(iverksettDto)
        }
    }

    @Test
    fun `Utbetalingsperioder som overlapper skal gi BAD_REQUEST`() {
        val tmpIverksettDto = opprettIverksettDto()
        val iverksettDto = tmpIverksettDto.copy(
            vedtak = tmpIverksettDto.vedtak.copy(
                utbetalinger = listOf(
                    lagAndelTilkjentYtelseDto(
                        beløp = 100,
                        fraOgMed = LocalDate.of(2023, 5, 15),
                        tilOgMed = LocalDate.of(2023, 5, 30),
                    ),
                    lagAndelTilkjentYtelseDto(
                        beløp = 100,
                        fraOgMed = LocalDate.of(2023, 5, 20),
                        tilOgMed = LocalDate.of(2023, 6, 3),
                    ),
                ),
            ),
        )

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            validatorService.validerAtUtbetalingsperioderIkkeOverlapperITid(iverksettDto)
        }
    }

    @Test
    fun `Ferietillegg til avdød for stønadstype EØS skal gi BAD_REQUEST`() {
        val iverksettDto = opprettIverksettDto(stønadType = StønadType.DAGPENGER_EOS, ferietillegg = Ferietillegg.AVDOD)

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            validatorService.validerAtIngenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød(iverksettDto)
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
