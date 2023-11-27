package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene

import no.nav.dagpenger.kontrakter.felles.SakIdentifikator
import java.time.LocalDate
import java.util.UUID
import no.nav.dagpenger.kontrakter.felles.BrukersNavKontor

/**
 * @param opphørFra Kan brukes når man ønsker å oppøre bak i tiden, før man selv var master,
 * eller ved simulering når (BA) ønsker å simulere alt på nytt
 * @param utbetalesTil I tilfeller der eks mottaker er institusjon, så kan man sende med en annen ident som beløpet utbetales til
 *
 * @param erGOmregning er flagg for overgangsstønad som setter et flagg på utbetalingsoppdraget
 */
data class Behandlingsinformasjon(
    val saksbehandlerId: String,
    val fagsakId: UUID? = null,
    val saksreferanse: String? = null,
    val behandlingId: String,
    val personident: String,
    val vedtaksdato: LocalDate,
    val brukersNavKontor: BrukersNavKontor? = null,
    val erGOmregning: Boolean = false,
) {
    init {
        SakIdentifikator.valider(fagsakId, saksreferanse)
    }
}
