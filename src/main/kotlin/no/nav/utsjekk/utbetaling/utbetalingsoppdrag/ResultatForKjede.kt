package no.nav.utsjekk.utbetaling.utbetalingsoppdrag

import no.nav.utsjekk.utbetaling.utbetalingsoppdrag.domene.AndelData
import java.time.LocalDate

internal data class ResultatForKjede(
        val beståendeAndeler: List<AndelData>,
        val nyeAndeler: List<AndelData>,
        val opphørsandel: Pair<AndelData, LocalDate>?,
        val sistePeriodeId: Long,
)
