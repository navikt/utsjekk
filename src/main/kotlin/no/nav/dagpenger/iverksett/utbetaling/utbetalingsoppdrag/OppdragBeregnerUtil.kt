package no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag

import no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.domene.AndelData
import no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.domene.Behandlingsinformasjon

internal object OppdragBeregnerUtil {

    fun validerAndeler(
            forrige: List<AndelData>,
            nye: List<AndelData>,
    ) {
        if ((forrige + nye).any { it.beløp == 0 }) {
            error("Andeler inneholder 0-beløp")
        }

        val idn = forrige.map { it.id } + nye.map { it.id }
        if (idn.size != idn.toSet().size) {
            error("Inneholder duplikat av id'er")
        }

        forrige.find { it.periodeId == null }
            ?.let { error("Tidligere andel=${it.id} mangler periodeId") }

        nye.find { it.periodeId != null || it.forrigePeriodeId != null }
            ?.let { error("Ny andel=${it.id} inneholder periodeId/forrigePeriodeId") }
    }
}
