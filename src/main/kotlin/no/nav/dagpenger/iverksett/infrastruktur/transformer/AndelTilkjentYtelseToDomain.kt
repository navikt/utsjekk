package no.nav.dagpenger.iverksett.infrastruktur.transformer

import no.nav.dagpenger.iverksett.api.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.Stønadsdata
import no.nav.dagpenger.iverksett.api.domene.StønadsdataDagpenger
import no.nav.dagpenger.iverksett.api.domene.StønadsdataTiltakspenger
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataDagpengerDto
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataDto
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataTiltakspengerDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto

fun UtbetalingDto.toDomain(): AndelTilkjentYtelse {
    return AndelTilkjentYtelse(
        beløp = this.belopPerDag,
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
