package no.nav.dagpenger.iverksett.utbetaling.util

import java.time.LocalDate
import no.nav.dagpenger.iverksett.utbetaling.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.domene.StønadsdataDagpenger
import no.nav.dagpenger.iverksett.utbetaling.domene.StønadsdataTiltakspenger
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataDagpengerDto
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataTiltakspengerDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto

fun lagAndelTilkjentYtelse(
    beløp: Int,
    fraOgMed: LocalDate,
    tilOgMed: LocalDate,
    periodeId: Long? = null,
    forrigePeriodeId: Long? = null,
    stønadstype: StønadType = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
    ferietillegg: Ferietillegg? = null,
) =
        if (stønadstype is StønadTypeDagpenger) {
            AndelTilkjentYtelse(
                    beløp = beløp,
                    periode = Datoperiode(fraOgMed, tilOgMed),
                    periodeId = periodeId,
                    forrigePeriodeId = forrigePeriodeId,
                    stønadsdata = StønadsdataDagpenger(stønadstype, ferietillegg)
            )
        } else {
            AndelTilkjentYtelse(
                    beløp = beløp,
                    periode = Datoperiode(fraOgMed, tilOgMed),
                    periodeId = periodeId,
                    forrigePeriodeId = forrigePeriodeId,
                    stønadsdata = StønadsdataTiltakspenger(stønadstype as StønadTypeTiltakspenger)
            )
        }

fun lagUtbetalingDto(
    beløp: Int,
    fraOgMed: LocalDate = LocalDate.of(2021, 1, 1),
    tilOgMed: LocalDate = LocalDate.of(2021, 1, 31),
    stønadstype: StønadType = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
    ferietillegg: Ferietillegg? = null,
): UtbetalingDto {
    val stønadsdata = if (stønadstype is StønadTypeDagpenger) {
        StønadsdataDagpengerDto(stønadstype, ferietillegg)
    } else {
        StønadsdataTiltakspengerDto(stønadstype as StønadTypeTiltakspenger)
    }
    return UtbetalingDto(
        beløpPerDag = beløp,
        fraOgMedDato = fraOgMed,
        tilOgMedDato = tilOgMed,
        stønadsdata = stønadsdata
    )
}
