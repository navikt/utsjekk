package no.nav.utsjekk.simulering.domene

import no.nav.utsjekk.iverksetting.domene.TilkjentYtelse
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.domene.Behandlingsinformasjon

data class Simulering(
    val behandlingsinformasjon: Behandlingsinformasjon,
    val nyTilkjentYtelse: TilkjentYtelse,
    val forrigeIverksetting: ForrigeIverksetting?,
)

data class ForrigeIverksetting(
    val behandlingId: String,
    val iverksettingId: String?,
)
