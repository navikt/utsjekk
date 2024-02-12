package no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag

import no.nav.dagpenger.iverksett.utbetaling.domene.StønadsdataDagpenger
import no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.AndelValidator.validerAndeler
import no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.domene.AndelData
import no.nav.dagpenger.kontrakter.felles.Satstype
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class AndelValidatorTest {
    @Nested
    inner class HappyCase {
        @Test
        fun `skal kunne sende inn tomme lister`() {
            validerAndeler(
                forrige = listOf(),
                nye = listOf(),
            )
        }

        @Test
        fun `forrige inneholer en andel og nye er tom liste`() {
            validerAndeler(
                forrige = listOf(lagAndel(periodeId = 1, forrigePeriodeId = 0)),
                nye = listOf(),
            )
        }

        @Test
        fun `forrige er tom, nye inneholder en andel`() {
            validerAndeler(
                forrige = listOf(),
                nye = listOf(lagAndel()),
            )
        }
    }

    @Nested
    inner class IdDuplikat {
        @Test
        fun `kan ikke inneholde duplikat av ider i forrige`() {
            val exception =
                assertThrows<IllegalStateException> {
                    validerAndeler(
                        forrige =
                            listOf(
                                lagAndel(id = "1", periodeId = 1, forrigePeriodeId = null),
                                lagAndel(id = "1", periodeId = 2, forrigePeriodeId = null),
                            ),
                        nye = listOf(),
                    )
                }
            assertTrue(exception.message?.contains("Inneholder duplikat av id'er")!!)
        }

        @Test
        fun `kan ikke inneholde duplikat av ider i nye`() {
            val exception =
                assertThrows<IllegalStateException> {
                    validerAndeler(
                        forrige = listOf(),
                        nye = listOf(lagAndel(id = "1"), lagAndel(id = "1")),
                    )
                }
            assertTrue(exception.message?.contains("Inneholder duplikat av id'er")!!)
        }

        @Test
        fun `kan ikke inneholde duplikat av ider tvers gamle og nye`() {
            val exception =
                assertThrows<IllegalStateException> {
                    validerAndeler(
                        forrige = listOf(lagAndel(id = "1", periodeId = 1, forrigePeriodeId = null)),
                        nye = listOf(lagAndel(id = "1")),
                    )
                }
            assertTrue(exception.message?.contains("Inneholder duplikat av id'er")!!)
        }
    }

    @Nested
    inner class ForrigeAndeler {
        @Test
        fun `forrige må inneholde periodeId`() {
            val exception =
                assertThrows<IllegalStateException> {
                    validerAndeler(
                        forrige = listOf(lagAndel(periodeId = null, forrigePeriodeId = null)),
                        nye = listOf(),
                    )
                }
            assertTrue(exception.message?.contains("mangler periodeId")!!)
        }
    }

    @Nested
    inner class NyeAndeler {
        @Test
        fun `kan ikke inneholde periodeId`() {
            val exception =
                assertThrows<IllegalStateException> {
                    validerAndeler(
                        forrige = listOf(),
                        nye = listOf(lagAndel(id = "1", periodeId = 1, forrigePeriodeId = null)),
                    )
                }
            assertTrue(exception.message?.contains("inneholder periodeId/forrigePeriodeId")!!)
        }

        @Test
        fun `kan ikke inneholde forrigePeriodeId`() {
            val exception =
                assertThrows<IllegalStateException> {
                    validerAndeler(
                        forrige = listOf(),
                        nye = listOf(lagAndel(id = "1", periodeId = null, forrigePeriodeId = 1)),
                    )
                }
            assertTrue(exception.message?.contains("inneholder periodeId/forrigePeriodeId")!!)
        }
    }

    @Nested
    inner class SatsTyper {
        @Test
        fun `andeler med månedssats kan ikke starte og slutte midt i måneden`() {
            val exception =
                assertThrows<IllegalArgumentException> {
                    validerAndeler(
                        forrige = listOf(),
                        nye =
                            listOf(
                                lagAndel(
                                    id = "1",
                                    fom = LocalDate.of(2024, 1, 15),
                                    tom = LocalDate.of(2024, 1, 25),
                                    satstype = Satstype.MÅNEDLIG,
                                    periodeId = null,
                                    forrigePeriodeId = null,
                                ),
                            ),
                    )
                }
            assertTrue(exception.message?.contains("må starte den første i måneden og slutte den siste i måneden")!!)
        }
    }

    private fun lagAndel(
        id: String = "",
        ytelseType: StønadTypeDagpenger = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
        fom: LocalDate = LocalDate.now(),
        tom: LocalDate = LocalDate.now().plusDays(5),
        satstype: Satstype = Satstype.DAGLIG,
        periodeId: Long? = null,
        forrigePeriodeId: Long? = null,
        beløp: Int = 1,
    ) = AndelData(
        id = id,
        fom = fom,
        tom = tom,
        beløp = beløp,
        satstype = satstype,
        stønadsdata = StønadsdataDagpenger(ytelseType),
        periodeId = periodeId,
        forrigePeriodeId = forrigePeriodeId,
    )
}
