package no.nav.dagpenger.iverksett.utbetaling.domene

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("iverksetting")
data class IverksettingEntitet(
    @Id
    val behandlingId: UUID,
    val data: Iverksetting,
)
