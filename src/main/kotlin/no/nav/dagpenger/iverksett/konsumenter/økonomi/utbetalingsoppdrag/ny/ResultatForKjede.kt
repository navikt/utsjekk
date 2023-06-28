package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny

import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.AndelData
import java.time.LocalDate

internal data class ResultatForKjede(
    val beståendeAndeler: List<AndelData>,
    val nyeAndeler: List<AndelData>,
    val opphørsandel: Pair<AndelData, LocalDate>?,
    val sistePeriodeId: Long,
)
