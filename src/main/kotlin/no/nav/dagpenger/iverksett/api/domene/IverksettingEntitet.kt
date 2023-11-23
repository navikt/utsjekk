package no.nav.dagpenger.iverksett.api.domene

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("iverksetting")
data class IverksettingEntitet(
    @Id
    val behandlingId: UUID,
    val data: Iverksetting,
)
