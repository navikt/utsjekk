package no.nav.utsjekk.iverksetting.util

import no.nav.utsjekk.iverksetting.domene.AndelTilkjentYtelse
import no.nav.utsjekk.iverksetting.domene.Periode
import no.nav.utsjekk.iverksetting.domene.StønadsdataDagpenger
import no.nav.utsjekk.iverksetting.domene.StønadsdataTiltakspenger
import no.nav.utsjekk.kontrakter.felles.StønadType
import no.nav.utsjekk.kontrakter.felles.StønadTypeDagpenger
import no.nav.utsjekk.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.utsjekk.kontrakter.iverksett.Ferietillegg
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataDagpengerDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataDto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingDto
import java.time.LocalDate

fun lagAndelTilkjentYtelse(
    beløp: Int,
    fraOgMed: LocalDate,
    tilOgMed: LocalDate,
    periodeId: Long? = null,
    forrigePeriodeId: Long? = null,
    stønadstype: StønadType = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
    ferietillegg: Ferietillegg? = null,
) = if (stønadstype is StønadTypeDagpenger) {
    AndelTilkjentYtelse(
        beløp = beløp,
        periode = Periode(fraOgMed, tilOgMed),
        periodeId = periodeId,
        forrigePeriodeId = forrigePeriodeId,
        stønadsdata = StønadsdataDagpenger(stønadstype, ferietillegg),
    )
} else {
    AndelTilkjentYtelse(
        beløp = beløp,
        periode = Periode(fraOgMed, tilOgMed),
        periodeId = periodeId,
        forrigePeriodeId = forrigePeriodeId,
        stønadsdata = StønadsdataTiltakspenger(stønadstype as StønadTypeTiltakspenger),
    )
}

fun lagUtbetalingDto(
    beløp: Int,
    fraOgMed: LocalDate = LocalDate.of(2021, 1, 1),
    tilOgMed: LocalDate = LocalDate.of(2021, 1, 31),
    stønadsdata: StønadsdataDto = StønadsdataDagpengerDto(stønadstype = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR),
): UtbetalingDto {
    return UtbetalingDto(
        beløpPerDag = beløp,
        fraOgMedDato = fraOgMed,
        tilOgMedDato = tilOgMed,
        stønadsdata = stønadsdata,
    )
}
