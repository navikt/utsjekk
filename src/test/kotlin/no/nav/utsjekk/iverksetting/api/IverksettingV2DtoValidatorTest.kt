package no.nav.utsjekk.iverksetting.api

import no.nav.utsjekk.felles.http.advice.ApiFeil
import no.nav.utsjekk.iverksetting.domene.transformer.RandomOSURId
import no.nav.utsjekk.iverksetting.util.enIverksettV2Dto
import no.nav.utsjekk.iverksetting.util.enUtbetalingV2Dto
import no.nav.utsjekk.kontrakter.felles.StønadTypeDagpenger
import no.nav.utsjekk.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.utsjekk.kontrakter.iverksett.Ferietillegg
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataDagpengerDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTiltakspengerV2Dto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.http.HttpStatus
import java.time.LocalDate

class IverksettingV2DtoValidatorTest {
    @Test
    fun `skal få BAD_REQUEST hvis sakId er for lang`() {
        val iverksettDto = enIverksettV2Dto(sakId = RandomOSURId.generate() + RandomOSURId.generate())

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            sakIdTilfredsstillerLengdebegrensning(iverksettDto)
        }
    }

    @Test
    fun `skal få BAD_REQUEST hvis behandlingId er for lang`() {
        val iverksettDto = enIverksettV2Dto(behandlingId = RandomOSURId.generate() + RandomOSURId.generate())

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            behandlingIdTilfredsstillerLengdebegrensning(iverksettDto)
        }
    }

    @Test
    fun `skal få BAD_REQUEST hvis tom kommer før fom i utbetalingsperiode`() {
        val tmpIverksettDto = enIverksettV2Dto()
        val iverksettDto =
            tmpIverksettDto.copy(
                vedtak =
                    tmpIverksettDto.vedtak.copy(
                        utbetalinger =
                            listOf(
                                enUtbetalingV2Dto(
                                    beløp = 100u,
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
        val tmpIverksettDto = enIverksettV2Dto()
        val iverksettDto =
            tmpIverksettDto.copy(
                vedtak =
                    tmpIverksettDto.vedtak.copy(
                        utbetalinger =
                            listOf(
                                enUtbetalingV2Dto(
                                    beløp = 100u,
                                    fraOgMed = LocalDate.of(2023, 5, 15),
                                    tilOgMed = LocalDate.of(2023, 5, 30),
                                ),
                                enUtbetalingV2Dto(
                                    beløp = 100u,
                                    fraOgMed = LocalDate.of(2023, 5, 20),
                                    tilOgMed = LocalDate.of(2023, 6, 3),
                                ),
                            ),
                    ),
            )

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            utbetalingsperioderMedLikStønadsdataOverlapperIkkeITid(iverksettDto)
        }
    }

    @Test
    fun `Utbetalingsperioder med ulik stønadsdata som overlapper skal ikke gi ApiFeil`() {
        val tmpIverksettDto = enIverksettV2Dto()
        val iverksettDto =
            tmpIverksettDto.copy(
                vedtak =
                    tmpIverksettDto.vedtak.copy(
                        utbetalinger =
                            listOf(
                                enUtbetalingV2Dto(
                                    beløp = 100u,
                                    fraOgMed = LocalDate.of(2023, 5, 15),
                                    tilOgMed = LocalDate.of(2023, 5, 30),
                                    stønadsdata =
                                        StønadsdataTiltakspengerV2Dto(
                                            stønadstype = StønadTypeTiltakspenger.JOBBKLUBB,
                                            brukersNavKontor = "4401",
                                        ),
                                ),
                                enUtbetalingV2Dto(
                                    beløp = 100u,
                                    fraOgMed = LocalDate.of(2023, 5, 20),
                                    tilOgMed = LocalDate.of(2023, 6, 3),
                                    stønadsdata =
                                        StønadsdataTiltakspengerV2Dto(
                                            stønadstype = StønadTypeTiltakspenger.JOBBKLUBB,
                                            barnetillegg = true,
                                            brukersNavKontor = "4401",
                                        ),
                                ),
                            ),
                    ),
            )

        assertDoesNotThrow {
            utbetalingsperioderMedLikStønadsdataOverlapperIkkeITid(iverksettDto)
        }
    }

    @Test
    fun `Utbetalingsperioder med lik stønadsdata som overlapper skal gi ApiFeil`() {
        val tmpIverksettDto = enIverksettV2Dto()
        val enUtbetalingsperiode =
            enUtbetalingV2Dto(
                beløp = 100u,
                fraOgMed = LocalDate.of(2023, 5, 15),
                tilOgMed = LocalDate.of(2023, 5, 30),
                stønadsdata =
                    StønadsdataTiltakspengerV2Dto(
                        stønadstype = StønadTypeTiltakspenger.JOBBKLUBB,
                        brukersNavKontor = "4401",
                    ),
            )
        val iverksettDto =
            tmpIverksettDto.copy(
                vedtak =
                    tmpIverksettDto.vedtak.copy(utbetalinger = listOf(enUtbetalingsperiode, enUtbetalingsperiode)),
            )

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            utbetalingsperioderMedLikStønadsdataOverlapperIkkeITid(iverksettDto)
        }
    }

    @Test
    fun `Ferietillegg til avdød for stønadstype EØS skal gi BAD_REQUEST`() {
        val iverksettDto =
            enIverksettV2Dto(
                stønadsdata = StønadsdataDagpengerDto(stønadstype = StønadTypeDagpenger.DAGPENGER_EØS, ferietillegg = Ferietillegg.AVDØD),
            )

        assertApiFeil(HttpStatus.BAD_REQUEST) {
            ingenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød(iverksettDto)
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
