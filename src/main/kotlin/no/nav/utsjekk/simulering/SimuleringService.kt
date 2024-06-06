package no.nav.utsjekk.simulering

import no.nav.utsjekk.iverksetting.domene.lagAndelData
import no.nav.utsjekk.iverksetting.domene.tilAndelData
import no.nav.utsjekk.iverksetting.tilstand.IverksettingsresultatService
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.Utbetalingsgenerator
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.simulering.client.SimuleringClient
import no.nav.utsjekk.simulering.client.dto.SimuleringResponse
import no.nav.utsjekk.simulering.domene.Simulering
import org.springframework.stereotype.Service

@Service
class SimuleringService(
    private val iverksettingsresultatService: IverksettingsresultatService,
    private val simuleringClient: SimuleringClient,
) {
    fun hentSimuleringsresultatMedOppsummering(simulering: Simulering): SimuleringResponse {
        val forrigeTilkjentYtelse =
            simulering.forrigeIverksetting?.let {
                val forrigeIverksetting =
                    iverksettingsresultatService.hentIverksettingsresultat(
                        fagsystem = Fagsystem.TILLEGGSSTØNADER,
                        sakId = simulering.behandlingsinformasjon.fagsakId,
                        behandlingId = it.behandlingId,
                        iverksettingId = it.iverksettingId,
                    )
                        ?: error(
                            "Kunne ikke finne iverksettingsresultat for behandling ${it.behandlingId}/iverksetting ${it.iverksettingId}",
                        )
                forrigeIverksetting.tilkjentYtelseForUtbetaling
            }
        val sisteAndelPerKjede =
            forrigeTilkjentYtelse?.sisteAndelPerKjede
                ?.mapValues { it.value.tilAndelData() }
                ?: emptyMap()

        val utbetalingsoppdrag =
            Utbetalingsgenerator.lagUtbetalingsoppdrag(
                behandlingsinformasjon = simulering.behandlingsinformasjon,
                nyeAndeler = simulering.nyTilkjentYtelse.lagAndelData(),
                forrigeAndeler = forrigeTilkjentYtelse?.lagAndelData() ?: emptyList(),
                sisteAndelPerKjede = sisteAndelPerKjede,
            ).utbetalingsoppdrag

        return simuleringClient.hentSimulering(utbetalingsoppdrag)
    }
}
