package no.nav.dagpenger.iverksett.kontrakter.journalføring.dokarkiv

class Dokument(
    val dokument: ByteArray,
    val filtype: Filtype,
    val filnavn: String? = null,
    val tittel: String? = null,
    val dokumenttype: Dokumenttype
)
