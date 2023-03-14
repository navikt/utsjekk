package no.nav.dagpenger.iverksett.kontrakter.oppgave

data class FinnOppgaveResponseDto(
    val antallTreffTotalt: Long,
    val oppgaver: List<Oppgave>,
)
