package no.nav.dagpenger.iverksett.utbetaling.domene

import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("iverksetting")
data class IverksettingEntitet(
    val behandlingId: UUID,
    val data: Iverksetting,
)
