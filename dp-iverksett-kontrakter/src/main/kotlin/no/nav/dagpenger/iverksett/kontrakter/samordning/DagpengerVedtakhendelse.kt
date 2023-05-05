package no.nav.dagpenger.iverksett.kontrakter.samordning

import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import java.util.UUID

data class DagpengerVedtakhendelse(
    val behandlingId: UUID,
    val personIdent: String,
    val stønadType: StønadType
)
