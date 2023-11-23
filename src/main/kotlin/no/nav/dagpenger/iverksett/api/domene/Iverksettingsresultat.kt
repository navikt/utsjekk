package no.nav.dagpenger.iverksett.api.domene

import java.time.LocalDateTime
import java.util.UUID
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("iverksett_resultat")
data class Iverksettingsresultat(
    @Id
    val behandlingId: UUID,
    @Column("tilkjentytelseforutbetaling")
    val tilkjentYtelseForUtbetaling: TilkjentYtelse? = null,
    @Column("oppdragresultat")
    val oppdragResultat: OppdragResultat? = null,
)

data class OppdragResultat(val oppdragStatus: OppdragStatus, val oppdragStatusOppdatert: LocalDateTime = LocalDateTime.now())
