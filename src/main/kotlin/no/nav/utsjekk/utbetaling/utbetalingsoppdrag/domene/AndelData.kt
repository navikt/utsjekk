package no.nav.utsjekk.utbetaling.utbetalingsoppdrag.domene

import no.nav.utsjekk.kontrakter.felles.Satstype
import no.nav.utsjekk.utbetaling.domene.Stønadsdata
import java.time.LocalDate

/**
 * ID her burde ikke brukes til noe spesielt. EF har ikke et ID på andeler som sendes til utbetalingsgeneratorn
 */
data class AndelData(
    val id: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val beløp: Int,
    val satstype: Satstype = Satstype.DAGLIG,
    val stønadsdata: Stønadsdata,
    val periodeId: Long? = null,
    val forrigePeriodeId: Long? = null,
)

internal fun List<AndelData>.uten0beløp(): List<AndelData> = this.filter { it.beløp != 0 }
