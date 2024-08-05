package no.nav.utsjekk.iverksetting.utbetalingsoppdrag

import no.nav.utsjekk.iverksetting.domene.StønadsdataDagpenger
import no.nav.utsjekk.iverksetting.domene.StønadsdataTiltakspenger
import no.nav.utsjekk.iverksetting.domene.transformer.RandomOSURId
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.domene.AndelData
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.domene.Behandlingsinformasjon
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.StønadTypeDagpenger
import no.nav.utsjekk.kontrakter.felles.StønadTypeTiltakspenger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UtbetalingsgeneratorTest {
    companion object {
        private val behandlingsinformasjon =
            Behandlingsinformasjon(
                saksbehandlerId = "A123456",
                beslutterId = "B123456",
                fagsystem = Fagsystem.DAGPENGER,
                fagsakId = RandomOSURId.generate(),
                behandlingId = RandomOSURId.generate(),
                personident = "12345678911",
                vedtaksdato = LocalDate.now(),
                iverksettingId = null,
            )
        private val basisAndelData =
            AndelData(
                id = "",
                fom = LocalDate.of(2023, 10, 10),
                tom = LocalDate.of(2023, 10, 20),
                beløp = 250,
                stønadsdata = StønadsdataDagpenger(StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR),
            )
    }

    @Test
    fun `utbetaling for tiltakspenger skal ha fagsystem tiltakspenger`() {
        val nyeAndeler =
            listOf(basisAndelData.copy(stønadsdata = StønadsdataTiltakspenger(StønadTypeTiltakspenger.JOBBKLUBB)))
        val behandlingsinformasjonTiltakspenger = behandlingsinformasjon.copy(fagsystem = Fagsystem.TILTAKSPENGER)

        val beregnetUtbetalingsoppdrag =
            Utbetalingsgenerator.lagUtbetalingsoppdrag(
                behandlingsinformasjon = behandlingsinformasjonTiltakspenger,
                nyeAndeler = nyeAndeler,
                forrigeAndeler = emptyList(),
                sisteAndelPerKjede = emptyMap(),
            )

        assertEquals(Fagsystem.TILTAKSPENGER, beregnetUtbetalingsoppdrag.utbetalingsoppdrag.fagsystem)
    }

    @Test
    fun `opphør av utbetaling for dagpenger skal ha fagsystem dagpenger`() {
        val forrigeAndel = basisAndelData.copy(periodeId = 0)
        val beregnetUtbetalingsoppdrag =
            Utbetalingsgenerator.lagUtbetalingsoppdrag(
                behandlingsinformasjon = behandlingsinformasjon,
                nyeAndeler = emptyList(),
                forrigeAndeler = listOf(forrigeAndel),
                sisteAndelPerKjede =
                    mapOf(
                        StønadsdataDagpenger(StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR).tilKjedenøkkel() to forrigeAndel,
                    ),
            )

        assertEquals(Fagsystem.DAGPENGER, beregnetUtbetalingsoppdrag.utbetalingsoppdrag.fagsystem)
    }
}
