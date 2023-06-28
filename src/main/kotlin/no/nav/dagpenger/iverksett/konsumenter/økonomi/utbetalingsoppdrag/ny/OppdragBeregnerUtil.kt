package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny

import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.AndelData
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.Behandlingsinformasjon

internal object OppdragBeregnerUtil {

    fun validerAndeler(
        behandlingsinformasjon: Behandlingsinformasjon,
        forrige: List<AndelData>,
        nye: List<AndelData>,
    ) {
        val forrigeUtenNullbeløp = forrige.filter { it.beløp != 0 }
        val idn = forrigeUtenNullbeløp.map { it.id } + nye.map { it.id }
        if (idn.size != idn.toSet().size) {
            error("Inneholder duplikat av id'er")
        }

        forrigeUtenNullbeløp.find { it.periodeId == null }
            ?.let { error("Tidligere andel=${it.id} mangler periodeId") }

        nye.find { it.periodeId != null || it.forrigePeriodeId != null }
            ?.let { error("Ny andel=${it.id} inneholder periodeId/forrigePeriodeId") }

        behandlingsinformasjon.opphørFra?.let { opphørFra ->
            forrige.find { it.fom < opphørFra }
                ?.let { error("Ugyldig opphørFra=$opphørFra som er etter andel=${it.id} sitt fom=${it.fom}") }
        }
    }
}
