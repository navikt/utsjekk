package no.nav.utsjekk.simulering

import no.nav.utsjekk.felles.http.advice.ApiFeil
import no.nav.utsjekk.iverksetting.domene.lagAndelData
import no.nav.utsjekk.iverksetting.domene.tilAndelData
import no.nav.utsjekk.iverksetting.tilstand.IverksettingsresultatService
import no.nav.utsjekk.iverksetting.utbetalingsoppdrag.Utbetalingsgenerator
import no.nav.utsjekk.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.utsjekk.simulering.api.SimuleringResponsDto
import no.nav.utsjekk.simulering.client.SimuleringClient
import no.nav.utsjekk.simulering.client.dto.Mapper.tilSimuleringDetaljer
import no.nav.utsjekk.simulering.domene.OppsummeringGenerator
import no.nav.utsjekk.simulering.domene.Simulering
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class SimuleringService(
    private val iverksettingsresultatService: IverksettingsresultatService,
    private val simuleringClient: SimuleringClient,
) {
    fun hentSimuleringsresultatMedOppsummering(simulering: Simulering): SimuleringResponsDto {
        val utbetalingsoppdrag = hentUtbetalingsoppdrag(simulering)

        utbetalingsoppdrag.valider()

        val hentetSimulering = simuleringClient.hentSimulering(utbetalingsoppdrag)

        return OppsummeringGenerator.lagOppsummering(hentetSimulering.tilSimuleringDetaljer(simulering.behandlingsinformasjon.fagsystem))
    }

    private fun hentUtbetalingsoppdrag(simulering: Simulering): Utbetalingsoppdrag {
        val forrigeTilkjentYtelse =
            simulering.forrigeIverksetting?.let {
                val forrigeIverksetting =
                    iverksettingsresultatService.hentIverksettingsresultat(
                        fagsystem = simulering.behandlingsinformasjon.fagsystem,
                        sakId = simulering.behandlingsinformasjon.fagsakId,
                        behandlingId = it.behandlingId,
                        iverksettingId = it.iverksettingId,
                    )
                        ?: throw ApiFeil(
                            "Fant ikke iverksettingsresultat for fagsystem ${simulering.behandlingsinformasjon.fagsystem}/behandling ${it.behandlingId}/iverksetting ${it.iverksettingId}",
                            HttpStatus.BAD_REQUEST,
                        )
                forrigeIverksetting.tilkjentYtelseForUtbetaling
            }
        val sisteAndelPerKjede =
            forrigeTilkjentYtelse
                ?.sisteAndelPerKjede
                ?.mapValues { it.value.tilAndelData() }
                ?: emptyMap()

        return Utbetalingsgenerator
            .lagUtbetalingsoppdrag(
                behandlingsinformasjon = simulering.behandlingsinformasjon,
                nyeAndeler = simulering.nyTilkjentYtelse.lagAndelData(),
                forrigeAndeler = forrigeTilkjentYtelse?.lagAndelData() ?: emptyList(),
                sisteAndelPerKjede = sisteAndelPerKjede,
            ).utbetalingsoppdrag
    }

    private fun Utbetalingsoppdrag.valider() {
        if (erFørsteUtbetalingPåSak && utbetalingsperiode.isEmpty()) {
            throw ApiFeil("Kan ikke simulere en tom utbetaling", HttpStatus.UNPROCESSABLE_ENTITY)
        }
    }
}
