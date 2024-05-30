package no.nav.utsjekk.iverksetting.utbetalingsoppdrag.domene

import no.nav.utsjekk.iverksetting.domene.Stønadsdata
import no.nav.utsjekk.kontrakter.felles.Satstype
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
