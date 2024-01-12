package no.nav.dagpenger.iverksett.utbetaling.domene.transformer

import no.nav.dagpenger.iverksett.utbetaling.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.domene.Stønadsdata
import no.nav.dagpenger.iverksett.utbetaling.domene.StønadsdataDagpenger
import no.nav.dagpenger.iverksett.utbetaling.domene.StønadsdataTiltakspenger
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataDagpengerDto
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataDto
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataTiltakspengerDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto

fun UtbetalingDto.toDomain(): AndelTilkjentYtelse {
    return AndelTilkjentYtelse(
        beløp = this.beløpPerDag,
        periode = Datoperiode(this.fraOgMedDato, this.tilOgMedDato),
        stønadsdata = this.stønadsdata.toDomain(),
    )
}

fun StønadsdataDto.toDomain(): Stønadsdata =
    if (this is StønadsdataDagpengerDto) {
        StønadsdataDagpenger(this.stønadstype, this.ferietillegg)
    } else {
        val stønadsdataTiltakspenger = this as StønadsdataTiltakspengerDto
        StønadsdataTiltakspenger(stønadsdataTiltakspenger.stønadstype, stønadsdataTiltakspenger.barnetillegg)
    }
