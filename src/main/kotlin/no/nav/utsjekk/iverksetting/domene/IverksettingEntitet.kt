package no.nav.utsjekk.iverksetting.domene

import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("iverksetting")
data class IverksettingEntitet(
    val behandlingId: String,
    val data: Iverksetting,
    val mottattTidspunkt: LocalDateTime,
)
