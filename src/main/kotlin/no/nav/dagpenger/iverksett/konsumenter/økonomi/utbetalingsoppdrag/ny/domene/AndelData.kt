package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene

import java.time.LocalDate

/**
 * ID her burde ikke brukes til noe spesielt. EF har ikke et ID på andeler som sendes til utbetalingsgeneratorn
 */
data class AndelData(
    val id: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val beløp: Int,
    val personIdent: String,
    val type: StønadTypeOgFerietillegg,
    val periodeId: Long?,
    val forrigePeriodeId: Long?,
)

internal fun List<AndelData>.uten0beløp(): List<AndelData> = this.filter { it.beløp != 0 }
