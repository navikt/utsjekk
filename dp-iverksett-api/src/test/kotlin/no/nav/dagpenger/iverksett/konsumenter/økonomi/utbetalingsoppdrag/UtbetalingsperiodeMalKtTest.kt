package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag

import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import no.nav.dagpenger.iverksett.kontrakter.oppdrag.Utbetalingsperiode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

internal class UtbetalingsperiodeMalKtTest {

    @Test
    fun `skal mappe stønadstype til riktig satstype`() {
        assertThat(mapSatstype(StønadType.DAGPENGER)).isEqualTo(Utbetalingsperiode.SatsType.DAG)
    }

    @Test
    internal fun `skal sette satstype til DAG for dagpenger`() {
        val utbetalingsperiode = lagUtbetalingsperiode(StønadType.DAGPENGER)
        assertThat(utbetalingsperiode.satsType).isEqualTo(Utbetalingsperiode.SatsType.DAG)
    }

    private fun lagUtbetalingsperiode(stønadType: StønadType) =
        lagPeriodeFraAndel(
            andel = lagAndelTilkjentYtelse(
                beløp = 10,
                fraOgMed = YearMonth.now(),
                tilOgMed = YearMonth.now(),
                periodeId = 1,
            ),
            type = stønadType,
            eksternBehandlingId = 1,
            vedtaksdato = LocalDate.now(),
            personIdent = "",
        )
}
