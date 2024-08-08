package no.nav.utsjekk.iverksetting.domene.transformer

import no.nav.utsjekk.iverksetting.domene.AndelTilkjentYtelse
import no.nav.utsjekk.iverksetting.domene.Periode
import no.nav.utsjekk.iverksetting.domene.Stønadsdata
import no.nav.utsjekk.iverksetting.domene.StønadsdataDagpenger
import no.nav.utsjekk.iverksetting.domene.StønadsdataTiltakspenger
import no.nav.utsjekk.kontrakter.felles.BrukersNavKontor
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataDagpengerDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTiltakspengerDto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingDto

fun UtbetalingDto.toDomain(brukersNavKontor: String?): AndelTilkjentYtelse {
    return AndelTilkjentYtelse(
        beløp = this.beløpPerDag,
        periode = Periode(this.fraOgMedDato, this.tilOgMedDato),
        stønadsdata = this.stønadsdata.toDomain(brukersNavKontor),
    )
}

fun StønadsdataDto.toDomain(brukersNavKontor: String?): Stønadsdata =
    if (this is StønadsdataDagpengerDto) {
        StønadsdataDagpenger(this.stønadstype, this.ferietillegg)
    } else {
        val stønadsdataTiltakspenger = this as StønadsdataTiltakspengerDto
        StønadsdataTiltakspenger(
            stønadstype = stønadsdataTiltakspenger.stønadstype,
            barnetillegg = stønadsdataTiltakspenger.barnetillegg,
            brukersNavKontor = BrukersNavKontor(brukersNavKontor!!)
        )
    }
