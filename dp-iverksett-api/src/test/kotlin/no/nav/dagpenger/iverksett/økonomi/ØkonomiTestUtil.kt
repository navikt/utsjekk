package no.nav.dagpenger.iverksett.økonomi

import no.nav.dagpenger.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.kontrakter.felles.Månedsperiode
import no.nav.dagpenger.iverksett.kontrakter.iverksett.AndelTilkjentYtelseDto
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

fun lagAndelTilkjentYtelse(
    beløp: Int,
    fraOgMed: YearMonth,
    tilOgMed: YearMonth,
    periodeId: Long? = null,
    forrigePeriodeId: Long? = null,
    kildeBehandlingId: UUID? = UUID.randomUUID(),
    inntekt: Int = 0,
    samordningsfradrag: Int = 0,
    inntektsreduksjon: Int = 0,
) =
    AndelTilkjentYtelse(
        beløp = beløp,
        periode = Månedsperiode(fraOgMed, tilOgMed),
        inntekt = inntekt,
        samordningsfradrag = samordningsfradrag,
        inntektsreduksjon = inntektsreduksjon,
        periodeId = periodeId,
        forrigePeriodeId = forrigePeriodeId,
        kildeBehandlingId = kildeBehandlingId,
    )

fun lagAndelTilkjentYtelseDto(
    beløp: Int,
    fraOgMed: LocalDate = LocalDate.of(2021, 1, 1),
    tilOgMed: LocalDate = LocalDate.of(2021, 1, 31),
    kildeBehandlingId: UUID = UUID.randomUUID(),
    inntekt: Int = 0,
    samordningsfradrag: Int = 0,
    inntektsreduksjon: Int = 0,
) =
    AndelTilkjentYtelseDto(
        beløp = beløp,
        fraOgMed = fraOgMed,
        tilOgMed = tilOgMed,
        inntekt = inntekt,
        samordningsfradrag = samordningsfradrag,
        inntektsreduksjon = inntektsreduksjon,
        kildeBehandlingId = kildeBehandlingId,
    )
