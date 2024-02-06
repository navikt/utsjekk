package no.nav.dagpenger.iverksett.utbetaling

import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.IverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.util.behandlingsdetaljer
import no.nav.dagpenger.iverksett.utbetaling.util.lagAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.util.opprettIverksett
import no.nav.dagpenger.iverksett.utbetaling.util.vedtaksdetaljer
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.somUUID
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

fun Int.mai(år: Int) = LocalDate.of(år, 5, this)

fun lagIverksettingsdata(
    fagsystem: Fagsystem = Fagsystem.DAGPENGER,
    sakId: UUID? = null,
    behandlingId: UUID? = null,
    forrigeBehandlingId: UUID? = null,
    andelsdatoer: List<LocalDate> = emptyList(),
    beløp: Int = 100,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
): Iverksetting {
    return opprettIverksett(
        fagsystem = fagsystem,
        sakId = sakId,
        behandlingsdetaljer =
            behandlingsdetaljer(
                behandlingId = behandlingId ?: UUID.randomUUID(),
                forrigeBehandlingId = forrigeBehandlingId,
            ),
        vedtaksdetaljer =
            vedtaksdetaljer(
                andeler =
                    andelsdatoer.map {
                        lagAndelTilkjentYtelse(beløp = beløp, fraOgMed = it, tilOgMed = it)
                    },
                vedtakstidspunkt = vedtakstidspunkt,
            ),
    )
}

fun lagIverksettingEntitet(iverksettingData: Iverksetting) =
    IverksettingEntitet(
        iverksettingData.behandling.behandlingId.somUUID,
        iverksettingData,
    )
