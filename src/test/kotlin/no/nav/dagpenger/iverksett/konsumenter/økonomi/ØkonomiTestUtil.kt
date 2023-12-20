package no.nav.dagpenger.iverksett.konsumenter.økonomi

import java.time.LocalDate
import no.nav.dagpenger.iverksett.api.domene.AndelTilkjentYtelse
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataDagpenger
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataTiltakspenger
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto

fun lagAndelTilkjentYtelse(
    beløp: Int,
    fraOgMed: LocalDate,
    tilOgMed: LocalDate,
    periodeId: Long? = null,
    forrigePeriodeId: Long? = null,
    stønadstype: StønadType = StønadTypeDagpenger.DAGPENGER_ARBEIDSSOKER_ORDINAER,
    ferietillegg: Ferietillegg? = null,
) =
    AndelTilkjentYtelse(
        beløp = beløp,
        periode = Datoperiode(fraOgMed, tilOgMed),
        periodeId = periodeId,
        forrigePeriodeId = forrigePeriodeId,
        stønadsdata = StønadsdataDagpenger(stønadstype as StønadTypeDagpenger, ferietillegg)
    )

fun lagUtbetalingDto(
    beløp: Int,
    fraOgMed: LocalDate = LocalDate.of(2021, 1, 1),
    tilOgMed: LocalDate = LocalDate.of(2021, 1, 31),
    stønadstype: StønadType = StønadTypeDagpenger.DAGPENGER_ARBEIDSSOKER_ORDINAER,
    ferietillegg: Ferietillegg? = null,
): UtbetalingDto {
    val stønadsdata = if (stønadstype is StønadTypeDagpenger) {
        StønadsdataDagpenger(stønadstype, ferietillegg)
    } else {
        StønadsdataTiltakspenger(stønadstype as StønadTypeTiltakspenger)
    }
    return UtbetalingDto(
        belopPerDag = beløp,
        fraOgMedDato = fraOgMed,
        tilOgMedDato = tilOgMed,
        stønadsdata = stønadsdata
    )
}
