package no.nav.dagpenger.iverksett.kontrakter.oppdrag

data class OppdragId(
    val fagsystem: String,
    val personIdent: String,
    val behandlingsId: String
) {
    override fun toString(): String = "OppdragId(fagsystem=$fagsystem, behandlingsId=$behandlingsId)"
}
