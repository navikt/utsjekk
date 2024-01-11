package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag

import no.nav.dagpenger.iverksett.api.domene.StønadsdataDagpenger
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.BeståendeAndelerBeregner.finnBeståendeAndeler
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.AndelData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger

class BeståendeAndelerBeregnerTest {
    companion object {
        private val JAN_8 = LocalDate.of(2021, 1, 8)
        private val FEB_5 = LocalDate.of(2021, 2, 5)
        private val MARS_13 = LocalDate.of(2021, 3, 13)
        private val APRIL_21 = LocalDate.of(2021, 4, 21)
        private val MAI_31 = LocalDate.of(2021, 5, 31)
    }

    @Test
    fun `ingen endring mellom 2 andeler`() {
        val jan = lagAndel(JAN_8, JAN_8, 1, periodeId = 0)
        val forrige = listOf(jan)
        val ny = listOf(lagAndel(JAN_8, JAN_8, 1))

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isNull()
        assertThat(beståendeAndeler.andeler).containsExactly(jan)
    }

    @Test
    fun `en ny andel`() {
        val forrige = listOf(lagAndel(JAN_8, JAN_8, 1, periodeId = 0))
        val ny = listOf(
            lagAndel(JAN_8, JAN_8, 1),
            lagAndel(FEB_5, FEB_5, 1),
        )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isNull()
        assertThat(beståendeAndeler.andeler).containsExactlyElementsOf(forrige)
    }

    @Test
    fun `fra 0 til 1 andel`() {
        val forrige = listOf<AndelData>()
        val ny = listOf(
            lagAndel(JAN_8, JAN_8, 1),
        )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isNull()
        assertThat(beståendeAndeler.andeler).isEmpty()
    }

    @Test
    fun `en ny andel mellom tidligere perioder`() {
        val januar = lagAndel(JAN_8, JAN_8, 1, periodeId = 0)
        val forrige = listOf(
            januar,
            lagAndel(MARS_13, MARS_13, 1, periodeId = 1, forrigePeriodeId = 0),
        )
        val ny = listOf(
            lagAndel(JAN_8, JAN_8, 1),
            lagAndel(FEB_5, FEB_5, 1),
            lagAndel(MARS_13, MARS_13, 1),
        )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isNull()
        assertThat(beståendeAndeler.andeler).containsExactly(januar)
    }

    @Test
    fun `fjernet en andel mellom tidligere perioder`() {
        val januar = lagAndel(JAN_8, JAN_8, 1, periodeId = 0)
        val forrige = listOf(
            januar,
            lagAndel(FEB_5, FEB_5, 1, periodeId = 1, forrigePeriodeId = 0),
            lagAndel(MARS_13, MARS_13, 1, periodeId = 2, forrigePeriodeId = 1),
        )
        val ny = listOf(
            lagAndel(JAN_8, JAN_8, 1),
            lagAndel(MARS_13, MARS_13, 1),
        )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isEqualTo(FEB_5)
        assertThat(beståendeAndeler.andeler).containsExactly(januar)
    }

    @Test
    fun `fjernet en andel`() {
        val januar = lagAndel(JAN_8, JAN_8, 1, periodeId = 0)
        val forrige = listOf(januar, lagAndel(FEB_5, FEB_5, 1, periodeId = 1, forrigePeriodeId = 0))
        val ny = listOf(lagAndel(JAN_8, JAN_8, 1))

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isEqualTo(FEB_5)
        assertThat(beståendeAndeler.andeler).containsExactly(januar)
    }

    @Test
    fun `avkortet en andel`() {
        val forrige = listOf(lagAndel(JAN_8, FEB_5, 1, periodeId = 0))
        val ny = listOf(lagAndel(JAN_8, JAN_8, 1))

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isEqualTo(JAN_8.plusDays(1))
        assertThat(beståendeAndeler.andeler)
            .containsExactly(lagAndel(JAN_8, JAN_8, 1, periodeId = 0))
    }

    @Test
    fun `avkortet en lengre andel`() {
        val forrige = listOf(lagAndel(JAN_8, MAI_31, 1, periodeId = 0))
        val ny = listOf(lagAndel(JAN_8, FEB_5, 1))

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isEqualTo(FEB_5.plusDays(1))
        assertThat(beståendeAndeler.andeler)
            .containsExactly(lagAndel(JAN_8, FEB_5, 1, periodeId = 0))
    }

    // TODO er dette et reell case?
    @Test
    fun `forlenget en andel`() {
        val forrige = listOf(lagAndel(JAN_8, JAN_8, 1, periodeId = 0))
        val ny = listOf(lagAndel(JAN_8, FEB_5, 1))

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isNull()
        assertThat(beståendeAndeler.andeler).isEmpty()
    }

    @Test
    fun `nytt beløp for periode`() {
        val forrige = listOf(
            lagAndel(JAN_8, FEB_5, 1, periodeId = 0),
        )
        val ny = listOf(
            lagAndel(JAN_8, FEB_5, 2),
        )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isNull()
        assertThat(beståendeAndeler.andeler).isEmpty()
    }

    @Test
    fun `avkortet periode og nytt beløp`() {
        val forrige = listOf(
            lagAndel(JAN_8, FEB_5, 1, periodeId = 0),
        )
        val ny = listOf(
            lagAndel(JAN_8, JAN_8, 2),
        )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isNull()
        assertThat(beståendeAndeler.andeler).isEmpty()
    }

    @Test
    fun `forlenget periode og nytt beløp`() {
        val forrige = listOf(
            lagAndel(JAN_8, JAN_8, 1, periodeId = 0),
        )
        val ny = listOf(
            lagAndel(JAN_8, FEB_5, 2),
        )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isNull()
        assertThat(beståendeAndeler.andeler).isEmpty()
    }

    @Test
    fun `nytt beløp fra feb`() {
        val ny_jan_dato = JAN_8.plusDays(20)
        val forrige = listOf(
            lagAndel(JAN_8, FEB_5, 1, periodeId = 0),
        )
        val ny = listOf(
            lagAndel(JAN_8, ny_jan_dato, 1),
            lagAndel(ny_jan_dato.plusDays(1), FEB_5, 2),
        )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isNull()
        assertThat(beståendeAndeler.andeler)
            .containsExactly(lagAndel(JAN_8, ny_jan_dato, 1, periodeId = 0))
    }

    @Test
    fun `første andelen får tidligere fom, og en ny andel avkorter`() {
        val forrige = listOf(
            lagAndel(FEB_5, MAI_31, 1, periodeId = 0),
        )
        val ny = listOf(
            lagAndel(JAN_8, MARS_13, 1),
            lagAndel(APRIL_21, MAI_31, 2),
        )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isNull()
        assertThat(beståendeAndeler.andeler).isEmpty()
    }

    @Test
    fun `får nytt beløp fra del av første perioden`() {
        val ny_januar_dato = JAN_8.plusDays(5)
        val forrige = listOf(
            lagAndel(JAN_8, FEB_5, 1, periodeId = 0),
            lagAndel(MARS_13, APRIL_21, 1, periodeId = 1, forrigePeriodeId = 0),
        )
        val ny = listOf(
            lagAndel(JAN_8, ny_januar_dato, 1),
            lagAndel(ny_januar_dato.plusDays(1), FEB_5, 2),
            lagAndel(MARS_13, APRIL_21, 2),
        )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isNull()
        assertThat(beståendeAndeler.andeler)
            .containsExactly(lagAndel(JAN_8, ny_januar_dato, 1, periodeId = 0))
    }

    @Test
    fun `avkorter den første perioden og beholden den andre`() {
        val forrige = listOf(
            lagAndel(JAN_8, FEB_5, 1, periodeId = 0),
            lagAndel(MARS_13, APRIL_21, 1, periodeId = 1, forrigePeriodeId = 0),
        )
        val ny = listOf(
            lagAndel(JAN_8, JAN_8, 1),
            lagAndel(MARS_13, APRIL_21, 2),
        )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isEqualTo(JAN_8.plusDays(1))
        assertThat(beståendeAndeler.andeler)
            .containsExactly(lagAndel(JAN_8, JAN_8, 1, periodeId = 0))
    }

    @Test
    fun `forlenger den første perioden og beholden den andre`() {
        val forrige = listOf(
            lagAndel(JAN_8, JAN_8, 1, periodeId = 0),
            lagAndel(MARS_13, APRIL_21, 1, periodeId = 1, forrigePeriodeId = 0),
        )
        val ny = listOf(
            lagAndel(JAN_8, FEB_5, 1),
            lagAndel(MARS_13, APRIL_21, 2),
        )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isNull()
        assertThat(beståendeAndeler.andeler).isEmpty()
    }

    @Test
    fun `første andelen får senere fom, og en ny andel avkorter`() {
        val forrige = listOf(
            lagAndel(JAN_8, MAI_31, 1, periodeId = 0),
        )
        val ny = listOf(
            lagAndel(FEB_5, MARS_13, 1),
            lagAndel(APRIL_21, MAI_31, 2),
        )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isEqualTo(JAN_8)
        assertThat(beståendeAndeler.andeler).isEmpty()
    }

    @Test
    fun `avkorter periode 2`() {
        val forrige = listOf(
            lagAndel(JAN_8, JAN_8, 1, periodeId = 0),
            lagAndel(FEB_5, MARS_13, 1, periodeId = 1, forrigePeriodeId = 0),
        )
        val ny = listOf(
            lagAndel(JAN_8, JAN_8, 1),
            lagAndel(FEB_5, FEB_5, 1),
        )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertThat(beståendeAndeler.opphørFra).isEqualTo(FEB_5.plusDays(1))
        assertThat(beståendeAndeler.andeler).containsExactly(
            lagAndel(JAN_8, JAN_8, 1, periodeId = 0),
            lagAndel(FEB_5, FEB_5, 1, periodeId = 1, forrigePeriodeId = 0),
        )
    }

    private fun lagAndel(
        fom: LocalDate,
        tom: LocalDate,
        beløp: Int,
        periodeId: Long? = null,
        forrigePeriodeId: Long? = null,
        type: StønadTypeDagpenger = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
    ): AndelData {
        return AndelData(
            id = "0",
            fom = fom,
            tom = tom,
            beløp = beløp,
            stønadsdata = StønadsdataDagpenger(type),
            periodeId = periodeId,
            forrigePeriodeId = forrigePeriodeId,
        )
    }
}
