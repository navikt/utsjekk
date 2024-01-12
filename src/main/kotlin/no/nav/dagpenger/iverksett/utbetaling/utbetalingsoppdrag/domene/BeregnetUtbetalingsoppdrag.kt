package no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.domene

import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsoppdrag

/**
 * @param andeler er alle andeler med nye periodeId/forrigePeriodeId for å kunne oppdatere lagrede andeler
 */
data class BeregnetUtbetalingsoppdrag(
        val utbetalingsoppdrag: Utbetalingsoppdrag,
        val andeler: List<AndelMedPeriodeId>,
)

data class AndelMedPeriodeId(
    val id: String,
    val periodeId: Long,
    val forrigePeriodeId: Long?,
) {

    constructor(andel: AndelData) :
        this(
            id = andel.id,
            periodeId = andel.periodeId ?: error("Mangler offset på andel=${andel.id}"),
            forrigePeriodeId = andel.forrigePeriodeId,
        )
}
