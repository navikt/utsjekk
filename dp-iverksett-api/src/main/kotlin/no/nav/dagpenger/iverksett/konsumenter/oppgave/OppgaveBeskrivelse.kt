package no.nav.dagpenger.iverksett.konsumenter.oppgave

import no.nav.dagpenger.iverksett.kontrakter.iverksett.VedtaksperiodeDagpenger
import no.nav.dagpenger.iverksett.kontrakter.iverksett.AktivitetType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object OppgaveBeskrivelse {

    fun beskrivelseFørstegangsbehandlingInnvilget(
        periode: Pair<LocalDate, LocalDate>,
        vedtak: VedtaksperiodeDagpenger,
    ): String {
        return "Dagpenger er innvilget fra ${periode.vedtaksPeriodeToString()}. " +
            "Aktivitet: ${vedtak.aktivitet.beskrivelse()}."
    }

    fun beskrivelseFørstegangsbehandlingAvslått(vedtaksdato: LocalDate): String {
        return "Søknad om dagpenger er avslått i vedtak datert ${vedtaksdato.toReadable()}."
    }

    fun beskrivelseRevurderingInnvilget(
        vedtaksPeriode: Pair<LocalDate, LocalDate>,
        gjeldendeVedtak: VedtaksperiodeDagpenger,
    ): String {
        return "Dagpenger revurdert. Periode ${vedtaksPeriode.vedtaksPeriodeToString()}. " +
            "Aktivitet: ${gjeldendeVedtak.aktivitet.beskrivelse()}."
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

    private fun AktivitetType.beskrivelse(): String {
        return when (this) {
            AktivitetType.MIGRERING -> error("Skal ikke opprette oppfølgningsoppgave for migrering")
            AktivitetType.IKKE_AKTIVITETSPLIKT -> ""
            AktivitetType.BARN_UNDER_ETT_ÅR -> "Barn er under 1 år"
            AktivitetType.FORSØRGER_I_ARBEID -> "Forsørger er i arbeid (§15-6 første ledd)"
            AktivitetType.FORSØRGER_I_UTDANNING -> "Forsørger er i utdanning (§15-6 første ledd)"
            AktivitetType.FORSØRGER_REELL_ARBEIDSSØKER -> "Forsørger er reell arbeidssøker (§15-6 første ledd)"
            AktivitetType.FORSØRGER_ETABLERER_VIRKSOMHET -> "Forsørger etablerer egen virksomhet (§15-6 første ledd)"
            AktivitetType.BARNET_SÆRLIG_TILSYNSKREVENDE -> "Barnet er særlig tilsynskrevende (§15-6 fjerde ledd)"
            AktivitetType.FORSØRGER_MANGLER_TILSYNSORDNING -> "Forsørger mangler tilsynsordning (§15-6 femte ledd)"
            AktivitetType.FORSØRGER_ER_SYK -> "Forsørger er syk (§15-6 femte ledd)"
            AktivitetType.BARNET_ER_SYKT -> "Barnet er sykt (§15-6 femte ledd)"
            AktivitetType.UTVIDELSE_BARNET_SÆRLIG_TILSYNSKREVENDE -> "Barnet er særlig tilsynskrevende (§15-8 tredje ledd)"
            AktivitetType.UTVIDELSE_FORSØRGER_I_UTDANNING -> "Forsørgeren er i utdanning (§15-8 andre ledd)"
            AktivitetType.FORLENGELSE_MIDLERTIDIG_SYKDOM -> "Forsørger eller barnet har en midlertidig sykdom (§15-8 fjerde ledd)"
            AktivitetType.FORLENGELSE_STØNAD_UT_SKOLEÅRET -> "Stønad ut skoleåret (§15-8 andre ledd)"
            AktivitetType.FORLENGELSE_STØNAD_PÅVENTE_ARBEID -> "Stønad i påvente av arbeid (§15-8 femte ledd)"
            AktivitetType.FORLENGELSE_STØNAD_PÅVENTE_UTDANNING -> "Stønad i påvente av utdanning (§15-8 femte ledd)"
            AktivitetType.FORLENGELSE_STØNAD_PÅVENTE_ARBEID_REELL_ARBEIDSSØKER ->
                "Stønad i påvente av arbeid - reell arnbeidssøker (§15-8 femte ledd)"

            AktivitetType.FORLENGELSE_STØNAD_PÅVENTE_OPPSTART_KVALIFISERINGSPROGRAM ->
                "Stønad i påvente av oppstart kvalifiseringsprogram"

            AktivitetType.FORLENGELSE_STØNAD_PÅVENTE_TILSYNSORDNING -> "Stønad i påvente av tilsynsordning (§15-8 femte ledd)"
        }
    }
}
