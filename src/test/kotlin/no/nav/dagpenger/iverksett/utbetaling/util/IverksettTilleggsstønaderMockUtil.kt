package no.nav.dagpenger.iverksett.utbetaling.util

import no.nav.dagpenger.kontrakter.felles.GeneriskId
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomString
import no.nav.dagpenger.kontrakter.felles.Personident
import no.nav.dagpenger.kontrakter.felles.Satstype
import no.nav.dagpenger.kontrakter.felles.StønadTypeTilleggsstønader
import no.nav.dagpenger.kontrakter.iverksett.IverksettTilleggsstønaderDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingTilleggsstønaderDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerTilleggsstønaderDto
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
        satstype = Satstype.MÅNEDLIG,
        fraOgMedDato = fraOgMed,
        tilOgMedDato = tilOgMed,
        stønadstype = StønadTypeTilleggsstønader.TILSYN_BARN_ENSLIG_FORSØRGER,
    )
}
