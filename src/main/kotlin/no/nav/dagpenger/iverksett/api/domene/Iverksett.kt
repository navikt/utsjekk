package no.nav.dagpenger.iverksett.api.domene

import java.util.UUID
import org.springframework.data.annotation.Id

data class Iverksett(
    @Id
    val behandlingId: UUID,
    val data: IverksettDagpenger,
)
