package no.nav.dagpenger.iverksett.api.domene

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.MappedCollection
import java.util.UUID

data class Iverksett(
    @Id
    val behandlingId: UUID,
    val data: IverksettDagpenger,
    val eksternId: Long,
    @MappedCollection(idColumn = "behandling_id")
    val brev: Brev? = null,
)
