package no.nav.dagpenger.iverksett.konsumenter.tilbakekreving

import no.nav.dagpenger.iverksett.api.domene.IverksettData
import no.nav.dagpenger.iverksett.api.domene.Tilbakekrevingsdetaljer
import no.nav.dagpenger.iverksett.konsumenter.økonomi.simulering.hentSammenhengendePerioderMedFeilutbetaling
import no.nav.dagpenger.iverksett.kontrakter.simulering.BeriketSimuleringsresultat
import no.nav.dagpenger.iverksett.kontrakter.simulering.Simuleringsoppsummering
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Tilbakekrevingsvalg
import java.math.BigDecimal

private val TILBAKEKREVING_UTEN_VARSEL =
    Tilbakekrevingsdetaljer(
        tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_UTEN_VARSEL,
        tilbakekrevingMedVarsel = null,
    )

fun IverksettData.oppfriskTilbakekreving(beriketSimuleringsresultat: BeriketSimuleringsresultat): IverksettData {
    val tilbakekreving = this.vedtak.tilbakekreving
    val simuleringsoppsummering = beriketSimuleringsresultat.oppsummering

    val nyTilbakekreving: Tilbakekrevingsdetaljer? =
        if (tilbakekreving != null && !simuleringsoppsummering.harFeilutbetaling) {
            null
        } else if (harAvvikIVarsel(tilbakekreving, simuleringsoppsummering)) {
            tilbakekreving?.oppdaterVarsel(simuleringsoppsummering)
        } else {
            tilbakekreving
        }

    return this.medNyTilbakekreving(nyTilbakekreving)
}

private fun harAvvikIVarsel(
    tilbakekrevingsdetaljer: Tilbakekrevingsdetaljer?,
    simuleringsoppsummering: Simuleringsoppsummering,
): Boolean {
    // Sjekker ikke periodene fordi de kan være ulikt konsolidert
    // Er jo et spørsmål om evt konsolideringslogikk heller burde ligge her i ef-iverksett i stedet for ef-sak
    // slik at de kan sammenliknes konsistent
    val varsel = tilbakekrevingsdetaljer?.tilbakekrevingMedVarsel
    return varsel != null && simuleringsoppsummering.feilutbetaling != varsel.sumFeilutbetaling
}

val Tilbakekrevingsdetaljer?.skalTilbakekreves: Boolean
    get() = this != null && this.tilbakekrevingsvalg != Tilbakekrevingsvalg.IGNORER_TILBAKEKREVING

val Simuleringsoppsummering.harFeilutbetaling: Boolean
    get() = this.feilutbetaling > BigDecimal.ZERO

fun Tilbakekrevingsdetaljer.oppdaterVarsel(simuleringsoppsummering: Simuleringsoppsummering): Tilbakekrevingsdetaljer {
    return this.copy(
        tilbakekrevingMedVarsel = this.tilbakekrevingMedVarsel
            ?.copy(
                sumFeilutbetaling = simuleringsoppsummering.feilutbetaling,
                perioder = simuleringsoppsummering.hentSammenhengendePerioderMedFeilutbetaling(),
            ),
    )
}
