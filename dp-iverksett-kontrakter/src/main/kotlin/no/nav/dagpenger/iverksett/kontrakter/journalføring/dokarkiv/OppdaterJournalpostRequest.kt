package no.nav.dagpenger.iverksett.kontrakter.journalføring.dokarkiv

import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.dagpenger.iverksett.kontrakter.felles.Behandlingstema
import no.nav.dagpenger.iverksett.kontrakter.felles.Tema

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OppdaterJournalpostRequest(
    val avsenderMottaker: AvsenderMottaker? = null,
    val bruker: DokarkivBruker? = null,
    val tema: Tema? = null,
    val behandlingstema: Behandlingstema? = null,
    val tittel: String? = null,
    val journalfoerendeEnhet: String? = null,
    val sak: Sak? = null,
    val dokumenter: List<DokumentInfo>? = null
)
