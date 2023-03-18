package no.nav.dagpenger.iverksett.api.domene

import no.nav.dagpenger.iverksett.kontrakter.felles.Månedsperiode
import java.time.LocalDate
import java.util.UUID
import kotlin.math.roundToInt

data class AndelTilkjentYtelse(
    val beløp: Int,
    @Deprecated("Bruk periode.", ReplaceWith("periode.fom")) val fraOgMed: LocalDate? = null,
    @Deprecated("Bruk periode.", ReplaceWith("periode.tom")) val tilOgMed: LocalDate? = null,
    val periode: Månedsperiode = Månedsperiode(
        fraOgMed ?: error("Minst en av fraOgMed og periode.fom må ha verdi."),
        tilOgMed ?: error("Minst en av tilOgMed og periode.tom må ha verdi."),
    ),

    val inntekt: Int,
    val samordningsfradrag: Int,
    val inntektsreduksjon: Int,

    val periodeId: Long? = null,
    val forrigePeriodeId: Long? = null,
    val kildeBehandlingId: UUID? = null,
) {

    private fun erTilsvarendeForUtbetaling(other: AndelTilkjentYtelse): Boolean {
        return (
            this.periode == other.periode &&
                this.beløp == other.beløp
            )
    }

    fun erNull() = this.beløp == 0

    fun utbetalingsgrad(): Int =
        (100 * (this.beløp.toDouble() / (this.beløp + this.inntektsreduksjon + this.samordningsfradrag))).roundToInt()

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
