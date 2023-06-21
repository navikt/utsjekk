package no.nav.dagpenger.iverksett.api

import io.mockk.mockk
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.lagIverksettData
import no.nav.dagpenger.iverksett.mai
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
        val iverksetting = lagIverksettData(
            beløp = 345,
            andelsdatoer = listOf(1.mai(2021)),
            vedtaksresultat = Vedtaksresultat.AVSLÅTT,
        )

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            validatorService.validerAtAvslåttVedtakIkkeHarUtbetalinger(iverksetting)
        }
    }

    @Test
    fun `skal få BAD_REQUEST hvis beløp på utbetaling er negativt`() {
        val iverksetting = lagIverksettData(
            beløp = -5,
            andelsdatoer = listOf(1.mai(2023)),
        )

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            validatorService.validerAtUtbetalingerBareHarPositiveBeløp(iverksetting)
        }
    }

    @Test
    fun `skal få BAD_REQUEST hvis tom kommer før fom i utbetalingsperiode`() {
        // TODO dette valideres allerede når andelene opprettes - fører til en IllegalArgumentException ved feil
        val tmp = lagIverksettData()
        val tilkjentYtelse = tmp.vedtak.tilkjentYtelse?.copy(
            andelerTilkjentYtelse = listOf(
                lagAndelTilkjentYtelse(
                    beløp = 345,
                    fraOgMed = LocalDate.of(2023, 5, 15),
                    tilOgMed = LocalDate.of(2023, 5, 10),
                ),
            ),
        )
        val iverksetting = tmp.copy(
            vedtak = tmp.vedtak.copy(tilkjentYtelse = tilkjentYtelse),
        )

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            validatorService.validerAtFraOgMedKommerFørTilOgMedIUtbetalingsperioder(iverksetting)
        }
    }

    @Test
    fun `Utbetalingsperioder som overlapper skal gi BAD_REQUEST`() {
        val tmp = lagIverksettData()
        val tilkjentYtelse = tmp.vedtak.tilkjentYtelse?.copy(
            andelerTilkjentYtelse = listOf(
                lagAndelTilkjentYtelse(
                    beløp = 345,
                    fraOgMed = LocalDate.of(2023, 5, 15),
                    tilOgMed = LocalDate.of(2023, 5, 30),
                ),
                lagAndelTilkjentYtelse(
                    beløp = 500,
                    fraOgMed = LocalDate.of(2023, 5, 20),
                    tilOgMed = LocalDate.of(2023, 6, 3),
                ),
            ),
        )
        val iverksetting = tmp.copy(
            vedtak = tmp.vedtak.copy(tilkjentYtelse = tilkjentYtelse),
        )

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            validatorService.validerAtUtbetalingsperioderIkkeOverlapperITid(iverksetting)
        }
    }

    @Test
    fun `Ferietillegg til avdød for stønadstype EØS skal gi BAD_REQUEST`() {
        val tmp = lagIverksettData()
        val tilkjentYtelse = tmp.vedtak.tilkjentYtelse?.copy(
            andelerTilkjentYtelse = listOf(
                lagAndelTilkjentYtelse(
                    beløp = 345,
                    fraOgMed = LocalDate.of(2023, 5, 15),
                    tilOgMed = LocalDate.of(2023, 5, 30),
                    stønadstype = StønadType.DAGPENGER_EOS,
                    ferietillegg = Ferietillegg.AVDOD,
                ),
            ),
        )
        val iverksetting = tmp.copy(
            vedtak = tmp.vedtak.copy(tilkjentYtelse = tilkjentYtelse),
        )

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            validatorService.validerAtDetFinnesKlassifiseringForStønadstypeOgFerietillegg(iverksetting)
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
