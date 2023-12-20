package no.nav.dagpenger.iverksett.api.domene

import java.util.UUID
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.AndelData
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.iverksett.Stønadsdata

data class AndelTilkjentYtelse(
    val beløp: Int,
    val periode: Datoperiode,
    val stønadsdata: Stønadsdata,
    val periodeId: Long? = null,
    val forrigePeriodeId: Long? = null,
) {

    var id: UUID = UUID.randomUUID()

    constructor(
        id: UUID,
        beløp: Int,
        periode: Datoperiode,
        stønadsdata: Stønadsdata,
        periodeId: Long? = null,
        forrigePeriodeId: Long? = null,
    ) : this(beløp, periode, stønadsdata, periodeId, forrigePeriodeId) {
        this.id = id
    }

    private fun erTilsvarendeForUtbetaling(other: AndelTilkjentYtelse): Boolean {
        return (
            this.periode == other.periode &&
                this.beløp == other.beløp
            )
    }

    fun erNull() = this.beløp == 0

    companion object {

        /**
         * Merk at det søkes snitt på visse attributter (erTilsvarendeForUtbetaling)
         * og man kun returnerer objekter fra receiver (ikke other)
         */
        fun Set<AndelTilkjentYtelse>.snittAndeler(other: Set<AndelTilkjentYtelse>): Set<AndelTilkjentYtelse> {
            val andelerKunIDenne = this.subtractAndeler(other)
            return this.subtractAndeler(andelerKunIDenne)
        }

        fun Set<AndelTilkjentYtelse>.disjunkteAndeler(other: Set<AndelTilkjentYtelse>): Set<AndelTilkjentYtelse> {
            val andelerKunIDenne = this.subtractAndeler(other)
            val andelerKunIAnnen = other.subtractAndeler(this)
            return andelerKunIDenne.union(andelerKunIAnnen)
        }

        private fun Set<AndelTilkjentYtelse>.subtractAndeler(other: Set<AndelTilkjentYtelse>): Set<AndelTilkjentYtelse> {
            return this.filter { a ->
                other.none { b -> a.erTilsvarendeForUtbetaling(b) }
            }.toSet()
        }
    }
}

fun AndelTilkjentYtelse.tilAndelData() = AndelData(
    id = this.id.toString(),
    fom = this.periode.fom,
    tom = this.periode.tom,
    beløp = this.beløp,
    stønadsdata = this.stønadsdata,
    periodeId = this.periodeId,
    forrigePeriodeId = this.forrigePeriodeId,
)
