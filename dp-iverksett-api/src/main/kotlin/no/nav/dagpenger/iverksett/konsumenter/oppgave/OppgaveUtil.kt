package no.nav.dagpenger.iverksett.konsumenter.oppgave

import no.nav.dagpenger.iverksett.kontrakter.felles.Enhet
import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import no.nav.dagpenger.iverksett.kontrakter.felles.Tema
import no.nav.dagpenger.iverksett.kontrakter.oppgave.IdentGruppe
import no.nav.dagpenger.iverksett.kontrakter.oppgave.OppgaveIdentV2
import no.nav.dagpenger.iverksett.kontrakter.oppgave.Oppgavetype
import no.nav.dagpenger.iverksett.kontrakter.oppgave.OpprettOppgaveRequest
import no.nav.familie.kontrakter.felles.Behandlingstema
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.util.Locale

object OppgaveUtil {

    fun opprettBehandlingstema(stønadstype: StønadType): Behandlingstema {
        return Behandlingstema
            .fromValue(
                stønadstype.name.lowercase(Locale.getDefault())
                    .replaceFirstChar { it.uppercase() },
            )
    }

    fun opprettOppgaveRequest(
        eksternFagsakId: Long,
        personIdent: String,
        stønadstype: StønadType,
        enhetsnummer: Enhet,
        oppgavetype: Oppgavetype,
        beskrivelse: String,
        settBehandlesAvApplikasjon: Boolean,
        fristFerdigstillelse: LocalDate? = null,
    ): OpprettOppgaveRequest {
        return OpprettOppgaveRequest(
            ident = OppgaveIdentV2(ident = personIdent, gruppe = IdentGruppe.FOLKEREGISTERIDENT),
            saksId = eksternFagsakId.toString(),
            tema = Tema.ENF,
            oppgavetype = oppgavetype,
            fristFerdigstillelse = fristFerdigstillelse(fristFerdigstillelse),
            beskrivelse = beskrivelse,
            enhetsnummer = enhetsnummer.enhetId,
            behandlingstema = opprettBehandlingstema(stønadstype).value,
            tilordnetRessurs = null,
            behandlesAvApplikasjon = if (settBehandlesAvApplikasjon) "familie-ef-sak" else null,
        )
    }

    private fun fristFerdigstillelse(aktivFra: LocalDate?, daysToAdd: Long = 0): LocalDate {
        var date = (aktivFra?.atTime(LocalTime.now()) ?: LocalDateTime.now()).plusDays(daysToAdd)

        if (date.hour >= 14) {
            date = date.plusDays(1)
        }

        when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> date = date.plusDays(2)
            DayOfWeek.SUNDAY -> date = date.plusDays(1)
            else -> {
            }
        }

        when {
            date.dayOfMonth == 1 && date.month == Month.JANUARY -> date = date.plusDays(1)
            date.dayOfMonth == 1 && date.month == Month.MAY -> date = date.plusDays(1)
            date.dayOfMonth == 17 && date.month == Month.MAY -> date = date.plusDays(1)
            date.dayOfMonth == 25 && date.month == Month.DECEMBER -> date = date.plusDays(2)
            date.dayOfMonth == 26 && date.month == Month.DECEMBER -> date = date.plusDays(1)
        }

        when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> date = date.plusDays(2)
            DayOfWeek.SUNDAY -> date = date.plusDays(1)
            else -> {
            }
        }

        return date.toLocalDate()
    }
}
