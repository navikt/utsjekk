package no.nav.dagpenger.iverksett.kontrakter.iverksett

import no.nav.dagpenger.kontrakter.utbetaling.StønadType
import java.time.LocalDate
import java.util.UUID

data class OppgaveForBarn(
    val behandlingId: UUID,
    val eksternFagsakId: Long,
    val personIdent: String,
    val stønadType: StønadType,
    val beskrivelse: String,
    val aktivFra: LocalDate? = null,
)

data class OppgaverForBarnDto(val oppgaverForBarn: List<OppgaveForBarn>)
