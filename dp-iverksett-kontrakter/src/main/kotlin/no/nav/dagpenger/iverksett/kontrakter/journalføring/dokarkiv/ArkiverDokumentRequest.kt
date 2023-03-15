package no.nav.dagpenger.iverksett.kontrakter.journalføring.dokarkiv

data class ArkiverDokumentRequest(
    val fnr: String,
    val hoveddokumentvarianter: List<Dokument>,
    val vedleggsdokumenter: List<Dokument> = emptyList(),
    val forsøkFerdigstill: Boolean,
    val fagsakId: String? = null,
    // Skal ikke settes for innkommende hvis Ruting gjøres av BRUT001
    val journalførendeEnhet: String? = null,
    // Ønsker du å generer forside når du tilskriver bruker kan du fylle ut denne
    val førsteside: Førsteside? = null,
    val eksternReferanseId: String? = null,
    val avsenderMottaker: AvsenderMottaker? = null
)
