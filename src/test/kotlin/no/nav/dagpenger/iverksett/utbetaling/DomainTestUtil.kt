package no.nav.dagpenger.iverksett.utbetaling

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.IverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.util.behandlingsdetaljer
import no.nav.dagpenger.iverksett.utbetaling.util.opprettIverksett
import no.nav.dagpenger.iverksett.utbetaling.util.vedtaksdetaljer
import no.nav.dagpenger.iverksett.utbetaling.util.lagAndelTilkjentYtelse

fun Int.januar(år: Int) = LocalDate.of(år, 1, this)
fun Int.mai(år: Int) = LocalDate.of(år, 5, this)

fun lagIverksettingsdata(
        forrigeBehandlingId: UUID? = null,
        andelsdatoer: List<LocalDate> = emptyList(),
        beløp: Int = 100,
        vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
): Iverksetting {
    return opprettIverksett(
        behandlingsdetaljer = behandlingsdetaljer(
            forrigeBehandlingId = forrigeBehandlingId,
        ),
        vedtaksdetaljer = vedtaksdetaljer(
            andeler = andelsdatoer.map {
                lagAndelTilkjentYtelse(beløp = beløp, fraOgMed = it, tilOgMed = it)
            },
            vedtakstidspunkt = vedtakstidspunkt,
        ),
    )
}

fun lagIverksettingEntitet(iverksettingData: Iverksetting) = IverksettingEntitet(
    iverksettingData.behandling.behandlingId,
    iverksettingData,
)
