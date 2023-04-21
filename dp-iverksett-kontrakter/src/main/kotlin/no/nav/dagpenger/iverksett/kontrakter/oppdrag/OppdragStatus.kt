package no.nav.dagpenger.iverksett.kontrakter.oppdrag

enum class OppdragStatus {
    LAGT_PAA_KOE,
    KVITTERT_OK,
    KVITTERT_MED_MANGLER,
    KVITTERT_FUNKSJONELL_FEIL,
    KVITTERT_TEKNISK_FEIL,
    KVITTERT_UKJENT
}
