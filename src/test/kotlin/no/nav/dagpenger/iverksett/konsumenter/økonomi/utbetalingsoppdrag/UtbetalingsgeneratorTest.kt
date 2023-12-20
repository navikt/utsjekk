package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag

import java.time.LocalDate
import java.util.UUID
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.AndelData
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.Behandlingsinformasjon
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.StønadTypeOgFerietillegg
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.iverksett.Stønadsdata
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataDagpenger
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataTiltakspenger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UtbetalingsgeneratorTest {
    companion object {
        private val FAGSYSTEM_TP = StønadTypeTiltakspenger.JOBBKLUBB.tilFagsystem()
        private val FAGSYSTEM_DP = StønadTypeDagpenger.DAGPENGER_ARBEIDSSOKER_ORDINAER.tilFagsystem()
        private val behandlingsinformasjon = Behandlingsinformasjon(
            saksbehandlerId = "A123456",
            fagsakId = UUID.randomUUID(),
            behandlingId = UUID.randomUUID().toString(),
            personident = "12345678911",
            vedtaksdato = LocalDate.now()
        )
        private val basisAndelData = AndelData(
            id = "",
            fom = LocalDate.of(2023, 10, 10),
            tom = LocalDate.of(2023, 10, 20),
            beløp = 250,
            stønadsdata = StønadsdataDagpenger(StønadTypeDagpenger.DAGPENGER_ARBEIDSSOKER_ORDINAER)
        )
    }

    @Test
    fun `utbetaling for tiltakspenger skal ha fagsystem tiltakspenger`() {
        val nyeAndeler =
            listOf(basisAndelData.copy(stønadsdata = StønadsdataTiltakspenger(StønadTypeTiltakspenger.JOBBKLUBB)))

        val beregnetUtbetalingsoppdrag = Utbetalingsgenerator.lagUtbetalingsoppdrag(
            behandlingsinformasjon = behandlingsinformasjon,
            nyeAndeler = nyeAndeler,
            forrigeAndeler = emptyList(),
            sisteAndelPerKjede = emptyMap()
        )

        assertEquals(FAGSYSTEM_TP, beregnetUtbetalingsoppdrag.utbetalingsoppdrag.fagSystem)
    }

    @Test
    fun `opphør av utbetaling for dagpenger skal ha fagsystem dagpenger`() {
        val forrigeAndel = basisAndelData.copy(periodeId = 0)
        val beregnetUtbetalingsoppdrag = Utbetalingsgenerator.lagUtbetalingsoppdrag(
            behandlingsinformasjon = behandlingsinformasjon,
            nyeAndeler = emptyList(),
            forrigeAndeler = listOf(forrigeAndel),
            sisteAndelPerKjede = mapOf(StønadsdataDagpenger(StønadTypeDagpenger.DAGPENGER_ARBEIDSSOKER_ORDINAER) to forrigeAndel)
        )

        assertEquals(FAGSYSTEM_DP, beregnetUtbetalingsoppdrag.utbetalingsoppdrag.fagSystem)
    }
}