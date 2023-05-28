package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag

import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagAndelTilkjentYtelse
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsperiode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class UtbetalingsperiodeMalKtTest {

    @Test
    fun `skal mappe stønadstype til riktig satstype`() {
        assertThat(mapSatstype(StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER)).isEqualTo(Utbetalingsperiode.SatsType.DAG)
    }

    @Test
    internal fun `skal sette satstype til DAG for dagpenger`() {
        val utbetalingsperiode = lagUtbetalingsperiode(StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER)
        assertThat(utbetalingsperiode.satsType).isEqualTo(Utbetalingsperiode.SatsType.DAG)
    }

    private fun lagUtbetalingsperiode(stønadType: StønadType) =
        lagPeriodeFraAndel(
            andel = lagAndelTilkjentYtelse(
                beløp = 10,
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now(),
                periodeId = 1,
            ),
            type = stønadType,
            behandlingId = UUID.randomUUID(),
            vedtaksdato = LocalDate.now(),
            personIdent = "",
        )
}
