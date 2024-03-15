package no.nav.dagpenger.iverksett.utbetaling.util

import no.nav.dagpenger.kontrakter.felles.Personident
import no.nav.dagpenger.kontrakter.felles.Satstype
import no.nav.dagpenger.kontrakter.felles.StønadTypeTilleggsstønader
import no.nav.dagpenger.kontrakter.iverksett.IverksettTilleggsstønaderDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingTilleggsstønaderDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerTilleggsstønaderDto
import java.time.LocalDate
import java.time.LocalDateTime

fun enIverksettTilleggsstønaderDto(
    behandlingId: String = "TEST123",
    sakId: String = "TEST456",
    andelsbeløp: Int = 500,
    iverksettingId: String? = null,
) = IverksettTilleggsstønaderDto(
    behandlingId = behandlingId,
    iverksettingId = iverksettingId,
    sakId = sakId,
    personident = Personident("15507600333"),
    vedtak =
        VedtaksdetaljerTilleggsstønaderDto(
            vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
            saksbehandlerId = "A12345",
            beslutterId = "B23456",
            utbetalinger =
                listOf(
                    enUtbetalingTilleggsstønaderDto(
                        beløp = andelsbeløp,
                    ),
                ),
        ),
)

fun enUtbetalingTilleggsstønaderDto(
    beløp: Int,
    fraOgMed: LocalDate = LocalDate.of(2024, 1, 1),
    tilOgMed: LocalDate = LocalDate.of(2024, 1, 31),
) = UtbetalingTilleggsstønaderDto(
    beløp = beløp,
    satstype = Satstype.MÅNEDLIG,
    fraOgMedDato = fraOgMed,
    tilOgMedDato = tilOgMed,
    stønadstype = StønadTypeTilleggsstønader.TILSYN_BARN_ENSLIG_FORSØRGER,
)
