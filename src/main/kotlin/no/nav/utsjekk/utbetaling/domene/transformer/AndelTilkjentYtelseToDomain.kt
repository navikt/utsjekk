package no.nav.utsjekk.utbetaling.domene.transformer

import no.nav.utsjekk.kontrakter.iverksett.StønadsdataDagpengerDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTiltakspengerDto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingDto
import no.nav.utsjekk.utbetaling.domene.AndelTilkjentYtelse
import no.nav.utsjekk.utbetaling.domene.Periode
import no.nav.utsjekk.utbetaling.domene.Stønadsdata
import no.nav.utsjekk.utbetaling.domene.StønadsdataDagpenger
import no.nav.utsjekk.utbetaling.domene.StønadsdataTiltakspenger

fun UtbetalingDto.toDomain(): AndelTilkjentYtelse {
    return AndelTilkjentYtelse(
        beløp = this.beløpPerDag,
        periode = Periode(this.fraOgMedDato, this.tilOgMedDato),
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
