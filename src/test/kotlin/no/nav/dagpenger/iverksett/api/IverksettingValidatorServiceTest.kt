package no.nav.dagpenger.iverksett.api

import io.mockk.mockk
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.dagpenger.iverksett.lagIverksettData
import no.nav.dagpenger.iverksett.mai
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

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
            vedtaksresultat = Vedtaksresultat.AVSLÅTT
        )

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            validatorService.validerAtAvslåttVedtakIkkeHarUtbetalinger(iverksetting)
        }
    }
}

fun assertApiFeil(httpStatus: HttpStatus, block: ()->Any){
    try {
        block()
        fail("Forventet ApiFeil, men fikk det ikke")
    } catch(e: ApiFeil){
        assertThat(e.httpStatus).isEqualTo(httpStatus)
    }
}
