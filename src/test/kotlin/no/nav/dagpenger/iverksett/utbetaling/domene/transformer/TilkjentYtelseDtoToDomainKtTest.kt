package no.nav.dagpenger.iverksett.utbetaling.domene.transformer

import no.nav.dagpenger.iverksett.utbetaling.januar
import no.nav.dagpenger.iverksett.utbetaling.util.lagUtbetalingDto
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataDagpengerDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class TilkjentYtelseDtoToDomainKtTest {
    @Test
    fun `slår sammen tom liste med utbetalinger`() {
        val utbetalinger = emptyList<UtbetalingDto>().sammenslått()

        assertEquals(0, utbetalinger.size)
    }

    @Test
    fun `slår sammen liste med 1 utbetaling`() {
        val utbetalinger = listOf(lagUtbetalingDto(beløp = 100)).sammenslått()

        assertEquals(1, utbetalinger.size)
        assertEquals(100, utbetalinger.first().beløpPerDag)
    }

    @Test
    fun `slår sammen to like endagsutbetalinger hvor fom og tom er ved siden av hverandre`() {
        val utbetalinger =
            listOf(
                lagUtbetalingDto(beløp = 100, fraOgMed = 1.januar(2023), tilOgMed = 1.januar(2023)),
                lagUtbetalingDto(beløp = 100, fraOgMed = 2.januar(2023), tilOgMed = 2.januar(2023)),
            ).sammenslått()

        assertEquals(1, utbetalinger.size)
        utbetalinger.first().let {
            assertEquals(1.januar(2023), it.fraOgMedDato)
            assertEquals(2.januar(2023), it.tilOgMedDato)
        }
    }

    @Test
    fun `slår sammen to like utbetalinger hvor fom og tom er ved siden av hverandre`() {
        val utbetalinger =
            listOf(
                lagUtbetalingDto(beløp = 100, fraOgMed = 1.januar(2023), tilOgMed = 10.januar(2023)),
                lagUtbetalingDto(beløp = 100, fraOgMed = 11.januar(2023), tilOgMed = 20.januar(2023)),
            ).sammenslått()

        assertEquals(1, utbetalinger.size)
        utbetalinger.first().let {
            assertEquals(1.januar(2023), it.fraOgMedDato)
            assertEquals(20.januar(2023), it.tilOgMedDato)
        }
    }

    @Test
    fun `slår sammen en rapporteringsperiode`() {
        val utbetalinger =
            listOf(
                lagUtbetalingDto(beløp = 100, fraOgMed = 2.januar(2023), tilOgMed = 2.januar(2023)),
                lagUtbetalingDto(beløp = 100, fraOgMed = 3.januar(2023), tilOgMed = 3.januar(2023)),
                lagUtbetalingDto(beløp = 100, fraOgMed = 4.januar(2023), tilOgMed = 4.januar(2023)),
                lagUtbetalingDto(beløp = 100, fraOgMed = 5.januar(2023), tilOgMed = 5.januar(2023)),
                lagUtbetalingDto(beløp = 100, fraOgMed = 6.januar(2023), tilOgMed = 6.januar(2023)),
                lagUtbetalingDto(beløp = 0, fraOgMed = 7.januar(2023), tilOgMed = 7.januar(2023)),
                lagUtbetalingDto(beløp = 0, fraOgMed = 8.januar(2023), tilOgMed = 8.januar(2023)),
                lagUtbetalingDto(beløp = 100, fraOgMed = 9.januar(2023), tilOgMed = 9.januar(2023)),
                lagUtbetalingDto(beløp = 200, fraOgMed = 10.januar(2023), tilOgMed = 10.januar(2023)),
                lagUtbetalingDto(beløp = 200, fraOgMed = 11.januar(2023), tilOgMed = 11.januar(2023)),
                lagUtbetalingDto(beløp = 200, fraOgMed = 12.januar(2023), tilOgMed = 12.januar(2023)),
                lagUtbetalingDto(beløp = 200, fraOgMed = 13.januar(2023), tilOgMed = 13.januar(2023)),
            ).sammenslått()

        assertEquals(4, utbetalinger.size)
        utbetalinger.component1().let {
            assertEquals(100, it.beløpPerDag)
            assertEquals(2.januar(2023), it.fraOgMedDato)
            assertEquals(6.januar(2023), it.tilOgMedDato)
        }
        utbetalinger.component2().let {
            assertEquals(0, it.beløpPerDag)
            assertEquals(7.januar(2023), it.fraOgMedDato)
            assertEquals(8.januar(2023), it.tilOgMedDato)
        }
        utbetalinger.component3().let {
            assertEquals(100, it.beløpPerDag)
            assertEquals(9.januar(2023), it.fraOgMedDato)
            assertEquals(9.januar(2023), it.tilOgMedDato)
        }
        utbetalinger.component4().let {
            assertEquals(200, it.beløpPerDag)
            assertEquals(10.januar(2023), it.fraOgMedDato)
            assertEquals(13.januar(2023), it.tilOgMedDato)
        }
    }

    @Test
    fun `slår ikke sammen utbetalinger med mellomrom`() {
        val utbetalinger =
            listOf(
                lagUtbetalingDto(beløp = 100, fraOgMed = 2.januar(2023), tilOgMed = 2.januar(2023)),
                lagUtbetalingDto(beløp = 100, fraOgMed = 4.januar(2023), tilOgMed = 4.januar(2023)),
            ).sammenslått()

        assertEquals(2, utbetalinger.size)
        utbetalinger.first().let {
            assertEquals(2.januar(2023), it.fraOgMedDato)
            assertEquals(2.januar(2023), it.tilOgMedDato)
        }
        utbetalinger.component2().let {
            assertEquals(4.januar(2023), it.fraOgMedDato)
            assertEquals(4.januar(2023), it.tilOgMedDato)
        }
    }

    @Test
    fun `slår ikke sammen utbetalinger med forskjellige beløp`() {
        val utbetalinger =
            listOf(
                lagUtbetalingDto(beløp = 100, fraOgMed = 2.januar(2023), tilOgMed = 2.januar(2023)),
                lagUtbetalingDto(beløp = 200, fraOgMed = 3.januar(2023), tilOgMed = 3.januar(2023)),
            ).sammenslått()

        assertEquals(2, utbetalinger.size)
        utbetalinger.first().let {
            assertEquals(100, it.beløpPerDag)
            assertEquals(2.januar(2023), it.fraOgMedDato)
            assertEquals(2.januar(2023), it.tilOgMedDato)
        }
        utbetalinger.component2().let {
            assertEquals(200, it.beløpPerDag)
            assertEquals(3.januar(2023), it.fraOgMedDato)
            assertEquals(3.januar(2023), it.tilOgMedDato)
        }
    }

    @Test
    fun `slår ikke sammen utbetalinger med forskjellige stønadstyper`() {
        val utbetalinger =
            listOf(
                lagUtbetalingDto(
                    beløp = 100,
                    fraOgMed = 2.januar(2023),
                    tilOgMed = 2.januar(2023),
                    stønadsdata =
                        StønadsdataDagpengerDto(
                            stønadstype = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
                        ),
                ),
                lagUtbetalingDto(
                    beløp = 100,
                    fraOgMed = 3.januar(2023),
                    tilOgMed = 3.januar(2023),
                    stønadsdata =
                        StønadsdataDagpengerDto(
                            stønadstype = StønadTypeDagpenger.DAGPENGER_EØS,
                        ),
                ),
            ).sammenslått()

        assertEquals(2, utbetalinger.size)
        utbetalinger.first().let {
            assertEquals(StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR, it.stønadsdata.stønadstype)
            assertEquals(2.januar(2023), it.fraOgMedDato)
            assertEquals(2.januar(2023), it.tilOgMedDato)
        }
        utbetalinger.component2().let {
            assertEquals(StønadTypeDagpenger.DAGPENGER_EØS, it.stønadsdata.stønadstype)
            assertEquals(LocalDate.of(2023, 1, 3), it.fraOgMedDato)
            assertEquals(LocalDate.of(2023, 1, 3), it.tilOgMedDato)
        }
    }

    @Test
    fun `slår ikke sammen utbetalinger med forskjellige ferietillegg`() {
        val utbetalinger =
            listOf(
                lagUtbetalingDto(
                    beløp = 100,
                    fraOgMed = 2.januar(2023),
                    tilOgMed = 2.januar(2023),
                    stønadsdata =
                        StønadsdataDagpengerDto(
                            stønadstype = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
                            ferietillegg = Ferietillegg.ORDINÆR,
                        ),
                ),
                lagUtbetalingDto(
                    beløp = 100,
                    fraOgMed = 3.januar(2023),
                    tilOgMed = 3.januar(2023),
                    stønadsdata =
                        StønadsdataDagpengerDto(
                            stønadstype = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
                            ferietillegg = Ferietillegg.AVDØD,
                        ),
                ),
            ).sammenslått()

        assertEquals(2, utbetalinger.size)
        utbetalinger.first().let {
            val stønadsdata = it.stønadsdata as StønadsdataDagpengerDto
            assertEquals(Ferietillegg.ORDINÆR, stønadsdata.ferietillegg)
            assertEquals(2.januar(2023), it.fraOgMedDato)
            assertEquals(2.januar(2023), it.tilOgMedDato)
        }
        utbetalinger.component2().let {
            val stønadsdata = it.stønadsdata as StønadsdataDagpengerDto
            assertEquals(Ferietillegg.AVDØD, stønadsdata.ferietillegg)
            assertEquals(LocalDate.of(2023, 1, 3), it.fraOgMedDato)
            assertEquals(LocalDate.of(2023, 1, 3), it.tilOgMedDato)
        }
    }
}
