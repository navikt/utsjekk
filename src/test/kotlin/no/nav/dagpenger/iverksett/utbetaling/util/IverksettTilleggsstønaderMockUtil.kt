package no.nav.dagpenger.iverksett.utbetaling.util

import no.nav.dagpenger.iverksett.utbetaling.api.IverksettTilleggsstønaderDto
import no.nav.dagpenger.iverksett.utbetaling.api.Satstype
import no.nav.dagpenger.iverksett.utbetaling.api.UtbetalingTilleggsstønaderDto
import no.nav.dagpenger.iverksett.utbetaling.api.VedtaksdetaljerTilleggsstønaderDto
import no.nav.dagpenger.kontrakter.felles.GeneriskId
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomString
import no.nav.dagpenger.kontrakter.felles.Personident
import java.time.LocalDate
import java.time.LocalDateTime

fun opprettIverksettTilleggsstønaderDto(
    behandlingId: GeneriskId = GeneriskIdSomString("TEST123"),
    sakId: GeneriskId = GeneriskIdSomString("TEST456"),
    andelsbeløp: Int = 500,
): IverksettTilleggsstønaderDto {
    val andelTilkjentYtelse =
        lagUtbetalingTilleggsstønaderDto(
            beløp = andelsbeløp,
        )

    return IverksettTilleggsstønaderDto(
        behandlingId = behandlingId,
        iverksettingId = null,
        sakId = sakId,
        personident = Personident("15507600333"),
        vedtak =
            VedtaksdetaljerTilleggsstønaderDto(
                vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
                saksbehandlerId = "A12345",
                beslutterId = "B23456",
                utbetalinger = listOf(andelTilkjentYtelse),
            ),
    )
}

fun lagUtbetalingTilleggsstønaderDto(
    beløp: Int,
    fraOgMed: LocalDate = LocalDate.of(2024, 1, 1),
    tilOgMed: LocalDate = LocalDate.of(2024, 1, 31),
): UtbetalingTilleggsstønaderDto {
    return UtbetalingTilleggsstønaderDto(
        beløp = beløp,
        satstype = Satstype.MND,
        fraOgMedDato = fraOgMed,
        tilOgMedDato = tilOgMed,
        klassifiseringskode = "TSTBASISP3-OP",
    )
}
