package no.nav.utsjekk.iverksetting.util

import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.felles.Satstype
import no.nav.utsjekk.kontrakter.felles.StønadTypeTilleggsstønader
import no.nav.utsjekk.kontrakter.iverksett.IverksettV2Dto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTilleggsstønaderDto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingV2Dto
import no.nav.utsjekk.kontrakter.iverksett.VedtaksdetaljerV2Dto
import java.time.LocalDate
import java.time.LocalDateTime

fun enIverksettV2Dto(
    behandlingId: String = "TEST123",
    sakId: String = "TEST456",
    andelsbeløp: UInt = 500u,
    iverksettingId: String? = null,
) = IverksettV2Dto(
    behandlingId = behandlingId,
    iverksettingId = iverksettingId,
    sakId = sakId,
    personident = Personident("15507600333"),
    vedtak = VedtaksdetaljerV2Dto(
        vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
        saksbehandlerId = "A12345",
        beslutterId = "B23456",
        utbetalinger =
        listOf(
            enUtbetalingV2Dto(
                beløp = andelsbeløp,
            ),
        ),
    ),
)

fun enUtbetalingV2Dto(
    beløp: UInt,
    fraOgMed: LocalDate = LocalDate.of(2024, 1, 1),
    tilOgMed: LocalDate = LocalDate.of(2024, 1, 31),
) = UtbetalingV2Dto(
    beløp = beløp,
    satstype = Satstype.MÅNEDLIG,
    fraOgMedDato = fraOgMed,
    tilOgMedDato = tilOgMed,
    stønadsdata = StønadsdataTilleggsstønaderDto(StønadTypeTilleggsstønader.TILSYN_BARN_ENSLIG_FORSØRGER),
)
