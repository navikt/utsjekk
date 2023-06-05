package no.nav.dagpenger.iverksett.konsumenter.oppgave

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object OppgaveBeskrivelse {

    fun beskrivelseFørstegangsbehandlingInnvilget(periode: Pair<LocalDate, LocalDate>): String {
        return "Dagpenger er innvilget fra ${periode.vedtaksPeriodeToString()}."
    }

    fun beskrivelseFørstegangsbehandlingAvslått(vedtaksdato: LocalDate): String {
        return "Søknad om dagpenger er avslått i vedtak datert ${vedtaksdato.toReadable()}."
    }

    fun beskrivelseRevurderingInnvilget(vedtaksPeriode: Pair<LocalDate, LocalDate>): String {
        return "Dagpenger revurdert. Periode ${vedtaksPeriode.vedtaksPeriodeToString()}. "
    }

    fun beskrivelseRevurderingOpphørt(opphørsdato: LocalDate?): String {
        return opphørsdato?.let {
            "Dagpenger er stanset fra ${opphørsdato.toReadable()}."
        } ?: "Dagpenger er stanset"
    }

    private fun LocalDate.toReadable(): String {
        return this.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

    private fun Pair<LocalDate, LocalDate>.vedtaksPeriodeToString(): String {
        return this.first.toReadable() + " - " + this.second.toReadable()
    }

    fun LocalDate.tilTekst(): String {
        val datoSomTekst = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("nb"))
        return this.format(datoSomTekst)
    }
}
