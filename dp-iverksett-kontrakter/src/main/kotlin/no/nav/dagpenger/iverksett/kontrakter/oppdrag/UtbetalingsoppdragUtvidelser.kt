package no.nav.dagpenger.iverksett.kontrakter.oppdrag

import no.nav.dagpenger.kontrakter.utbetaling.Utbetalingsoppdrag

fun Utbetalingsoppdrag.behandlingsIdForFørsteUtbetalingsperiode(): String {

    return utbetalingsperiode[0].behandlingId.toString()
}

