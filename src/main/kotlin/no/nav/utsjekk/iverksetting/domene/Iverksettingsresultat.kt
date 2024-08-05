package no.nav.utsjekk.iverksetting.domene

import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.oppdrag.OppdragStatus
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

data class Iverksettingsresultat(
    val fagsystem: Fagsystem,
    val sakId: String,
    val behandlingId: String,
    val iverksettingId: String? = null,
    @Column("tilkjentytelseforutbetaling")
    val tilkjentYtelseForUtbetaling: TilkjentYtelse? = null,
    @Column("oppdragresultat")
    val oppdragResultat: OppdragResultat? = null,
)

data class OppdragResultat(
    val oppdragStatus: OppdragStatus,
    val oppdragStatusOppdatert: LocalDateTime = LocalDateTime.now(),
)

data class GammeltIverksettingsresultat(
    val fagsystem: Fagsystem,
    val sakId: String,
    val behandlingId: String,
    val iverksettingId: String? = null,
    @Column("tilkjentytelseforutbetaling")
    val tilkjentYtelseForUtbetaling: GammelTilkjentYtelse? = null,
    @Column("oppdragresultat")
    val oppdragResultat: OppdragResultat? = null,
) {
    fun tilNy(): Iverksettingsresultat =
        Iverksettingsresultat(
            fagsystem = this.fagsystem,
            sakId = this.sakId,
            behandlingId = this.behandlingId,
            iverksettingId = this.iverksettingId,
            tilkjentYtelseForUtbetaling = this.tilkjentYtelseForUtbetaling?.tilNy(),
            oppdragResultat = this.oppdragResultat,
        )
}
