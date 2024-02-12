package no.nav.dagpenger.iverksett.utbetaling.domene

import no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.domene.AndelData
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.felles.Satstype
import java.util.UUID

data class AndelTilkjentYtelse(
    val beløp: Int,
    val satstype: Satstype = Satstype.DAGLIG,
    val periode: Datoperiode,
    val stønadsdata: Stønadsdata,
    val periodeId: Long? = null,
    val forrigePeriodeId: Long? = null,
) {
    var id: UUID = UUID.randomUUID()
}

fun AndelTilkjentYtelse.tilAndelData() =
    AndelData(
        id = this.id.toString(),
        fom = this.periode.fom,
        tom = this.periode.tom,
        beløp = this.beløp,
        satstype = this.satstype,
        stønadsdata = this.stønadsdata,
        periodeId = this.periodeId,
        forrigePeriodeId = this.forrigePeriodeId,
    )
