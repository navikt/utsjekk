package no.nav.utsjekk.iverksetting.utbetalingsoppdrag

import no.nav.utsjekk.iverksetting.domene.StønadsdataDagpenger
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.BeståendeAndelerBeregner.finnBeståendeAndeler
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.domene.AndelData
import no.nav.utsjekk.iverksetting.util.april
import no.nav.utsjekk.iverksetting.util.februar
import no.nav.utsjekk.iverksetting.util.januar
import no.nav.utsjekk.iverksetting.util.mai
import no.nav.utsjekk.iverksetting.util.mars
import no.nav.utsjekk.kontrakter.felles.StønadTypeDagpenger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BeståendeAndelerBeregnerTest {
    @Test
    fun `ingen endring mellom 2 andeler`() {
        val jan = lagAndel(8.januar, 8.januar, 1, periodeId = 0)
        val forrige = listOf(jan)
        val ny = listOf(lagAndel(8.januar, 8.januar, 1))

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertNull(beståendeAndeler.opphørFra)
        assertEquals(1, beståendeAndeler.andeler.size)
        assertTrue(beståendeAndeler.andeler.contains(jan))
    }

    @Test
    fun `en ny andel`() {
        val forrige = listOf(lagAndel(8.januar, 8.januar, 1, periodeId = 0))
        val ny =
            listOf(
                lagAndel(8.januar, 8.januar, 1),
                lagAndel(5.februar, 5.februar, 1),
            )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertNull(beståendeAndeler.opphørFra)
        assertEquals(1, beståendeAndeler.andeler.size)
        assertEquals(forrige, beståendeAndeler.andeler)
    }

    @Test
    fun `fra 0 til 1 andel`() {
        val forrige = listOf<AndelData>()
        val ny =
            listOf(
                lagAndel(8.januar, 8.januar, 1),
            )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertNull(beståendeAndeler.opphørFra)
        assertTrue(beståendeAndeler.andeler.isEmpty())
    }

    @Test
    fun `en ny andel mellom tidligere perioder`() {
        val januar = lagAndel(8.januar, 8.januar, 1, periodeId = 0)
        val forrige =
            listOf(
                januar,
                lagAndel(13.mars, 13.mars, 1, periodeId = 1, forrigePeriodeId = 0),
            )
        val ny =
            listOf(
                lagAndel(8.januar, 8.januar, 1),
                lagAndel(5.februar, 5.februar, 1),
                lagAndel(13.mars, 13.mars, 1),
            )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertNull(beståendeAndeler.opphørFra)
        assertEquals(1, beståendeAndeler.andeler.size)
        assertTrue(beståendeAndeler.andeler.contains(januar))
    }

    @Test
    fun `fjernet en andel mellom tidligere perioder`() {
        val januar = lagAndel(8.januar, 8.januar, 1, periodeId = 0)
        val forrige =
            listOf(
                januar,
                lagAndel(5.februar, 5.februar, 1, periodeId = 1, forrigePeriodeId = 0),
                lagAndel(13.mars, 13.mars, 1, periodeId = 2, forrigePeriodeId = 1),
            )
        val ny =
            listOf(
                lagAndel(8.januar, 8.januar, 1),
                lagAndel(13.mars, 13.mars, 1),
            )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertEquals(5.februar, beståendeAndeler.opphørFra)
        assertEquals(1, beståendeAndeler.andeler.size)
        assertTrue(beståendeAndeler.andeler.contains(januar))
    }

    @Test
    fun `fjernet en andel`() {
        val januar = lagAndel(8.januar, 8.januar, 1, periodeId = 0)
        val forrige = listOf(januar, lagAndel(5.februar, 5.februar, 1, periodeId = 1, forrigePeriodeId = 0))
        val ny = listOf(lagAndel(8.januar, 8.januar, 1))

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertEquals(5.februar, beståendeAndeler.opphørFra)
        assertEquals(1, beståendeAndeler.andeler.size)
        assertTrue(beståendeAndeler.andeler.contains(januar))
    }

    @Test
    fun `avkortet en andel`() {
        val forrige = listOf(lagAndel(8.januar, 5.februar, 1, periodeId = 0))
        val ny = listOf(lagAndel(8.januar, 8.januar, 1))

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertEquals(8.januar.plusDays(1), beståendeAndeler.opphørFra)
        assertEquals(1, beståendeAndeler.andeler.size)
        assertTrue(beståendeAndeler.andeler.contains(lagAndel(8.januar, 8.januar, 1, periodeId = 0)))
    }

    @Test
    fun `avkortet en lengre andel`() {
        val forrige = listOf(lagAndel(8.januar, 31.mai, 1, periodeId = 0))
        val ny = listOf(lagAndel(8.januar, 5.februar, 1))

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertEquals(5.februar.plusDays(1), beståendeAndeler.opphørFra)
        assertEquals(1, beståendeAndeler.andeler.size)
        assertTrue(beståendeAndeler.andeler.contains(lagAndel(8.januar, 5.februar, 1, periodeId = 0)))
    }

    // TODO er dette et reell case?
    @Test
    fun `forlenget en andel`() {
        val forrige = listOf(lagAndel(8.januar, 8.januar, 1, periodeId = 0))
        val ny = listOf(lagAndel(8.januar, 5.februar, 1))

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertNull(beståendeAndeler.opphørFra)
        assertTrue(beståendeAndeler.andeler.isEmpty())
    }

    @Test
    fun `nytt beløp for periode`() {
        val forrige =
            listOf(
                lagAndel(8.januar, 5.februar, 1, periodeId = 0),
            )
        val ny =
            listOf(
                lagAndel(8.januar, 5.februar, 2),
            )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertNull(beståendeAndeler.opphørFra)
        assertTrue(beståendeAndeler.andeler.isEmpty())
    }

    @Test
    fun `avkortet periode og nytt beløp`() {
        val forrige =
            listOf(
                lagAndel(8.januar, 5.februar, 1, periodeId = 0),
            )
        val ny =
            listOf(
                lagAndel(8.januar, 8.januar, 2),
            )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertNull(beståendeAndeler.opphørFra)
        assertTrue(beståendeAndeler.andeler.isEmpty())
    }

    @Test
    fun `forlenget periode og nytt beløp`() {
        val forrige =
            listOf(
                lagAndel(8.januar, 8.januar, 1, periodeId = 0),
            )
        val ny =
            listOf(
                lagAndel(8.januar, 5.februar, 2),
            )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertNull(beståendeAndeler.opphørFra)
        assertTrue(beståendeAndeler.andeler.isEmpty())
    }

    @Test
    fun `nytt beløp fra feb`() {
        val januar = 8.januar.plusDays(20)
        val forrige =
            listOf(
                lagAndel(8.januar, 5.februar, 1, periodeId = 0),
            )
        val ny =
            listOf(
                lagAndel(8.januar, januar, 1),
                lagAndel(januar.plusDays(1), 5.februar, 2),
            )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertNull(beståendeAndeler.opphørFra)
        assertEquals(1, beståendeAndeler.andeler.size)
        assertTrue(beståendeAndeler.andeler.contains(lagAndel(8.januar, januar, 1, periodeId = 0)))
    }

    @Test
    fun `første andelen får tidligere fom, og en ny andel avkorter`() {
        val forrige =
            listOf(
                lagAndel(5.februar, 31.mai, 1, periodeId = 0),
            )
        val ny =
            listOf(
                lagAndel(8.januar, 13.mars, 1),
                lagAndel(21.april, 31.mai, 2),
            )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertNull(beståendeAndeler.opphørFra)
        assertTrue(beståendeAndeler.andeler.isEmpty())
    }

    @Test
    fun `får nytt beløp fra del av første perioden`() {
        val januar = 8.januar.plusDays(5)
        val forrige =
            listOf(
                lagAndel(8.januar, 5.februar, 1, periodeId = 0),
                lagAndel(13.mars, 21.april, 1, periodeId = 1, forrigePeriodeId = 0),
            )
        val ny =
            listOf(
                lagAndel(8.januar, januar, 1),
                lagAndel(januar.plusDays(1), 5.februar, 2),
                lagAndel(13.mars, 21.april, 2),
            )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertNull(beståendeAndeler.opphørFra)
        assertEquals(1, beståendeAndeler.andeler.size)
        assertTrue(beståendeAndeler.andeler.contains(lagAndel(8.januar, januar, 1, periodeId = 0)))
    }

    @Test
    fun `avkorter den første perioden og beholden den andre`() {
        val forrige =
            listOf(
                lagAndel(8.januar, 5.februar, 1, periodeId = 0),
                lagAndel(13.mars, 21.april, 1, periodeId = 1, forrigePeriodeId = 0),
            )
        val ny =
            listOf(
                lagAndel(8.januar, 8.januar, 1),
                lagAndel(13.mars, 21.april, 2),
            )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertEquals(8.januar.plusDays(1), beståendeAndeler.opphørFra)
        assertEquals(1, beståendeAndeler.andeler.size)
        assertTrue(beståendeAndeler.andeler.contains(lagAndel(8.januar, 8.januar, 1, periodeId = 0)))
    }

    @Test
    fun `forlenger den første perioden og beholden den andre`() {
        val forrige =
            listOf(
                lagAndel(8.januar, 8.januar, 1, periodeId = 0),
                lagAndel(13.mars, 21.april, 1, periodeId = 1, forrigePeriodeId = 0),
            )
        val ny =
            listOf(
                lagAndel(8.januar, 5.februar, 1),
                lagAndel(13.mars, 21.april, 2),
            )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertNull(beståendeAndeler.opphørFra)
        assertTrue(beståendeAndeler.andeler.isEmpty())
    }

    @Test
    fun `første andelen får senere fom, og en ny andel avkorter`() {
        val forrige =
            listOf(
                lagAndel(8.januar, 31.mai, 1, periodeId = 0),
            )
        val ny =
            listOf(
                lagAndel(5.februar, 13.mars, 1),
                lagAndel(21.april, 31.mai, 2),
            )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertEquals(8.januar, beståendeAndeler.opphørFra)
        assertTrue(beståendeAndeler.andeler.isEmpty())
    }

    @Test
    fun `avkorter periode 2`() {
        val forrige =
            listOf(
                lagAndel(8.januar, 8.januar, 1, periodeId = 0),
                lagAndel(5.februar, 13.mars, 1, periodeId = 1, forrigePeriodeId = 0),
            )
        val ny =
            listOf(
                lagAndel(8.januar, 8.januar, 1),
                lagAndel(5.februar, 5.februar, 1),
            )

        val beståendeAndeler = finnBeståendeAndeler(forrige, ny, null)

        assertEquals(5.februar.plusDays(1), beståendeAndeler.opphørFra)
        assertEquals(2, beståendeAndeler.andeler.size)
        assertTrue(
            beståendeAndeler.andeler.containsAll(
                listOf(
                    lagAndel(8.januar, 8.januar, 1, periodeId = 0),
                    lagAndel(5.februar, 5.februar, 1, periodeId = 1, forrigePeriodeId = 0),
                ),
            ),
        )
    }

    private fun lagAndel(
        fom: LocalDate,
        tom: LocalDate,
        beløp: Int,
        periodeId: Long? = null,
        forrigePeriodeId: Long? = null,
        type: StønadTypeDagpenger = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
    ) = AndelData(
        id = "0",
        fom = fom,
        tom = tom,
        beløp = beløp,
        stønadsdata = StønadsdataDagpenger(type),
        periodeId = periodeId,
        forrigePeriodeId = forrigePeriodeId,
    )
}
