package no.nav.dagpenger.iverksett.kontrakter.tilbakekreving

import java.math.BigDecimal

data class Varsel(
    val varseltekst: String,
    val sumFeilutbetaling: BigDecimal,
    val perioder: List<Periode>,
)
