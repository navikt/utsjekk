package no.nav.dagpenger.iverksett.infrastruktur.transformer

import no.nav.dagpenger.iverksett.iverksetting.domene.Simulering
import no.nav.dagpenger.iverksett.kontrakter.iverksett.SimuleringDto

fun SimuleringDto.toDomain(): Simulering {
    return Simulering(
        nyTilkjentYtelseMedMetaData = this.nyTilkjentYtelseMedMetaData.toDomain(),
        forrigeBehandlingId = this.forrigeBehandlingId,
    )
}
