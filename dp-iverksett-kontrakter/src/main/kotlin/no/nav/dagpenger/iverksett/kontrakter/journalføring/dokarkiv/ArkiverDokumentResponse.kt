package no.nav.dagpenger.iverksett.kontrakter.journalføring.dokarkiv

data class ArkiverDokumentResponse(
    val journalpostId: String,
    val ferdigstilt: Boolean,
    val dokumenter: List<DokumentInfo>? = null
)
