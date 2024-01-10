package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag

import no.nav.dagpenger.iverksett.api.domene.StønadsdataDagpenger
import java.time.YearMonth
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.OppdragBeregnerUtil.validerAndeler
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.AndelData
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OppdragBeregnerUtilTest {

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
        fun `kan ikke inneholde duplikat av idn i forrige`() {
            assertThatThrownBy {
                validerAndeler(
                    forrige = listOf(
                        lagAndel(id = "1", periodeId = 1, forrigePeriodeId = null),
                        lagAndel(id = "1", periodeId = 2, forrigePeriodeId = null),
                    ),
                    nye = listOf(),
                )
            }.hasMessageContaining("Inneholder duplikat av id'er")
        }

        @Test
        fun `kan ikke inneholde duplikat av idn i nye`() {
            assertThatThrownBy {
                validerAndeler(
                    forrige = listOf(),
                    nye = listOf(lagAndel(id = "1"), lagAndel(id = "1")),
                )
            }.hasMessageContaining("Inneholder duplikat av id'er")
        }

        @Test
        fun `kan ikke inneholde duplikat av idn tvers gamle og nye`() {
            assertThatThrownBy {
                validerAndeler(
                    forrige = listOf(lagAndel(id = "1", periodeId = 1, forrigePeriodeId = null)),
                    nye = listOf(lagAndel(id = "1")),
                )
            }.hasMessageContaining("Inneholder duplikat av id'er")
        }
    }

    @Nested
    inner class ForrigeAndeler {

        @Test
        fun `forrige må inneholde periodeId`() {
            assertThatThrownBy {
                validerAndeler(
                    forrige = listOf(lagAndel(periodeId = null, forrigePeriodeId = null)),
                    nye = listOf(),
                )
            }.hasMessageContaining("mangler periodeId")
        }
    }

    @Nested
    inner class NyeAndeler {

        @Test
        fun `kan ikke inneholde periodeId`() {
            assertThatThrownBy {
                validerAndeler(
                    forrige = listOf(),
                    nye = listOf(lagAndel(id = "1", periodeId = 1, forrigePeriodeId = null)),
                )
            }.hasMessageContaining("inneholder periodeId/forrigePeriodeId")
        }

        @Test
        fun `kan ikke inneholde forrigePeriodeId`() {
            assertThatThrownBy {
                validerAndeler(
                    forrige = listOf(),
                    nye = listOf(lagAndel(id = "1", periodeId = null, forrigePeriodeId = 1)),
                )
            }.hasMessageContaining("inneholder periodeId/forrigePeriodeId")
        }
    }

    private fun lagAndel(
        id: String = "",
        ytelseType: StønadTypeDagpenger = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
        periodeId: Long? = null,
        forrigePeriodeId: Long? = null,
        beløp: Int = 1,
    ) = AndelData(
        id = id,
        fom = YearMonth.now().atDay(1),
        tom = YearMonth.now().atEndOfMonth(),
        beløp = beløp,
        stønadsdata = StønadsdataDagpenger(ytelseType),
        periodeId = periodeId,
        forrigePeriodeId = forrigePeriodeId,
    )
}
