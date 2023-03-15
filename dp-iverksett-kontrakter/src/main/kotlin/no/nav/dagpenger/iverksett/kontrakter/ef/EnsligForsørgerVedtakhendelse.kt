package no.nav.dagpenger.iverksett.kontrakter.ef

import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType

data class EnsligForsørgerVedtakhendelse(
    val behandlingId: Long,
    val personIdent: String,
    val stønadType: StønadType
)
