package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny

import java.time.LocalDate
import java.time.YearMonth
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.OppdragBeregnerUtil.validerAndeler
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.FAGSAK_ID
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.AndelData
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.Behandlingsinformasjon
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.StønadTypeOgFerietillegg
import no.nav.dagpenger.kontrakter.felles.StønadType
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OppdragBeregnerUtilTest {

    @Nested
    inner class HappyCase {

        @Test
        fun `skal kunne sende inn tomme lister`() {
            validerAndeler(
                lagBehandlingsinformasjon(),
                forrige = listOf(),
                nye = listOf(),
            )
        }

        @Test
        fun `forrige inneholer en andel og nye er tom liste`() {
            validerAndeler(
                lagBehandlingsinformasjon(),
                forrige = listOf(lagAndel(periodeId = 1, forrigePeriodeId = 0)),
                nye = listOf(),
            )
        }

        @Test
        fun `forrige er tom, nye inneholder en andel`() {
            validerAndeler(
                lagBehandlingsinformasjon(),
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
                    lagBehandlingsinformasjon(),
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
                    lagBehandlingsinformasjon(),
                    forrige = listOf(),
                    nye = listOf(lagAndel(id = "1"), lagAndel(id = "1")),
                )
            }.hasMessageContaining("Inneholder duplikat av id'er")
        }

        @Test
        fun `kan ikke inneholde duplikat av idn tvers gamle og nye`() {
            assertThatThrownBy {
                validerAndeler(
                    lagBehandlingsinformasjon(),
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
                    lagBehandlingsinformasjon(),
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
                    lagBehandlingsinformasjon(),
                    forrige = listOf(),
                    nye = listOf(lagAndel(id = "1", periodeId = 1, forrigePeriodeId = null)),
                )
            }.hasMessageContaining("inneholder periodeId/forrigePeriodeId")
        }

        @Test
        fun `kan ikke inneholde forrigePeriodeId`() {
            assertThatThrownBy {
                validerAndeler(
                    lagBehandlingsinformasjon(),
                    forrige = listOf(),
                    nye = listOf(lagAndel(id = "1", periodeId = null, forrigePeriodeId = 1)),
                )
            }.hasMessageContaining("inneholder periodeId/forrigePeriodeId")
        }
    }

    @Nested
    inner class OpphørFra {

        @Test
        fun `kan sende inn opphørFra som er før forrige første periode`() {
            validerAndeler(
                lagBehandlingsinformasjon(opphørFra = YearMonth.now().minusMonths(1)),
                forrige = listOf(lagAndel(id = "1", periodeId = 1, forrigePeriodeId = null)),
                nye = listOf(),
            )
        }

        @Test
        fun `kan sende inn opphørFra når det ikke finnes tidligere perioder`() {
            validerAndeler(
                lagBehandlingsinformasjon(opphørFra = YearMonth.now().minusMonths(1)),
                forrige = listOf(),
                nye = listOf(),
            )
        }

        @Test
        fun `kan ikke sende inn opphørFra etter forrigePeriode sitt første dato`() {
            assertThatThrownBy {
                validerAndeler(
                    lagBehandlingsinformasjon(opphørFra = YearMonth.now().plusMonths(1)),
                    forrige = listOf(lagAndel(id = "1", periodeId = 1, forrigePeriodeId = null)),
                    nye = listOf(),
                )
            }.hasMessageContaining("Ugyldig opphørFra")
        }
    }

    private fun lagAndel(
        id: String = "",
        ytelseType: StønadType = StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER,
        periodeId: Long? = null,
        forrigePeriodeId: Long? = null,
        beløp: Int = 1,
    ) = AndelData(
        id = id,
        fom = YearMonth.now().atDay(1),
        tom = YearMonth.now().atEndOfMonth(),
        beløp = beløp,
        type = StønadTypeOgFerietillegg(ytelseType, null),
        periodeId = periodeId,
        forrigePeriodeId = forrigePeriodeId,
    )

    private fun lagBehandlingsinformasjon(
        opphørFra: YearMonth? = null,
    ) = Behandlingsinformasjon(
        fagsakId = FAGSAK_ID,
        saksbehandlerId = "saksbehandlerId",
        behandlingId = "1",
        personIdent = "1",
        vedtaksdato = LocalDate.now(),
        opphørFra = opphørFra?.atDay(1),
    )
}
