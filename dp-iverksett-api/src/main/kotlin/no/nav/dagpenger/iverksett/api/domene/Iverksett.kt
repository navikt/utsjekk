package no.nav.dagpenger.iverksett.api.domene

import no.nav.dagpenger.iverksett.kontrakter.iverksett.IverksettDagpenger
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.MappedCollection
import java.util.UUID

data class Iverksett(
    @Id
    val behandlingId: UUID,
    val data: IverksettDagpenger,
    @MappedCollection(idColumn = "behandling_id")
    val brev: Brev? = null,
)
