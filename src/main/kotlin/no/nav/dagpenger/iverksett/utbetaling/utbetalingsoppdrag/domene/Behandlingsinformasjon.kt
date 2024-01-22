package no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.domene

import no.nav.dagpenger.kontrakter.felles.BrukersNavKontor
import no.nav.dagpenger.kontrakter.felles.GeneriskId
import java.time.LocalDate

/**
 * @param opphørFra Kan brukes når man ønsker å oppøre bak i tiden, før man selv var master,
 * eller ved simulering når (BA) ønsker å simulere alt på nytt
 * @param utbetalesTil I tilfeller der eks mottaker er institusjon, så kan man sende med en annen ident som beløpet utbetales til
 *
 * @param erGOmregning er flagg for overgangsstønad som setter et flagg på utbetalingsoppdraget
 */
data class Behandlingsinformasjon(
    val saksbehandlerId: String,
    val fagsakId: GeneriskId,
    val behandlingId: GeneriskId,
    val personident: String,
    val vedtaksdato: LocalDate,
    val brukersNavKontor: BrukersNavKontor? = null,
)
