package no.nav.dagpenger.iverksett.konsumenter.økonomi.simulering

import no.nav.dagpenger.iverksett.api.domene.Simulering
import no.nav.dagpenger.iverksett.api.domene.tilAndelData
import no.nav.dagpenger.iverksett.api.domene.tilBehandlingsinformasjon
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.dagpenger.iverksett.infrastruktur.configuration.FeatureToggleConfig
import no.nav.dagpenger.iverksett.infrastruktur.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.konsumenter.økonomi.OppdragClient
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.Utbetalingsgenerator
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.dagpenger.kontrakter.oppdrag.simulering.BeriketSimuleringsresultat
import no.nav.dagpenger.kontrakter.oppdrag.simulering.DetaljertSimuleringResultat
import no.nav.familie.http.client.RessursException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDate

@Service
class SimuleringService(
    private val oppdragKlient: OppdragClient,
    private val iverksettResultatService: IverksettResultatService,
    private val featureToggleService: FeatureToggleService,
) {

    fun hentDetaljertSimuleringResultat(simulering: Simulering): DetaljertSimuleringResultat {
        if (featureToggleService.isEnabled(FeatureToggleConfig.STOPP_IVERKSETTING)) {
            error("Kan ikke sende inn simmulere")
        }
        try {
            val forrigeTilkjentYtelse = simulering.forrigeBehandlingId?.let {
                iverksettResultatService.hentTilkjentYtelse(simulering.forrigeBehandlingId)
            }

            val beregnetUtbetalingsoppdrag = Utbetalingsgenerator.lagUtbetalingsoppdrag(
                behandlingsinformasjon = simulering.tilBehandlingsinformasjon(),
                nyeAndeler = simulering.andelerTilkjentYtelse.map { it.tilAndelData() },
                forrigeAndeler = forrigeTilkjentYtelse?.andelerTilkjentYtelse?.map { it.tilAndelData() } ?: emptyList(),
                sisteAndelPerKjede = simulering.tilkjentYtelse.sisteAndelPerKjede.mapValues { it.value.tilAndelData() },
            )
            val tilkjentYtelseMedUtbetalingsoppdrag = simulering.tilkjentYtelse.copy(
                utbetalingsoppdrag = beregnetUtbetalingsoppdrag.utbetalingsoppdrag,
            )

            val utbetalingsoppdrag = tilkjentYtelseMedUtbetalingsoppdrag.utbetalingsoppdrag
                ?: error("Utbetalingsoppdraget finnes ikke for tilkjent ytelse")

            if (utbetalingsoppdrag.utbetalingsperiode.isEmpty()) {
                return DetaljertSimuleringResultat(emptyList())
            }
            return hentSimuleringsresultatOgFiltrerPosteringer(
                utbetalingsoppdrag,
                simulering.stønadstype,
            )
        } catch (feil: Throwable) {
            val cause = feil.cause
            if (feil is RessursException && cause is HttpClientErrorException.BadRequest) {
                throw ApiFeil(feil.ressurs.melding, HttpStatus.BAD_REQUEST)
            }
            throw Exception("Henting av simuleringsresultat feilet", feil)
        }
    }

    fun hentBeriketSimulering(simulering: Simulering): BeriketSimuleringsresultat {
        if (featureToggleService.isEnabled(FeatureToggleConfig.STOPP_IVERKSETTING)) {
            error("Kan ikke sende inn simmulere")
        }
        val detaljertSimuleringResultat = hentDetaljertSimuleringResultat(simulering)
        val simuleringsresultatDto = lagSimuleringsoppsummering(detaljertSimuleringResultat, LocalDate.now())

        return BeriketSimuleringsresultat(
            detaljer = detaljertSimuleringResultat,
            oppsummering = simuleringsresultatDto,
        )
    }

    private fun hentSimuleringsresultatOgFiltrerPosteringer(
        utbetalingsoppdrag: Utbetalingsoppdrag,
        stønadType: StønadType,
    ): DetaljertSimuleringResultat {
        val fagOmrådeKoder = fagområdeKoderForPosteringer(stønadType)
        val simuleringsResultat = oppdragKlient.hentSimuleringsresultat(utbetalingsoppdrag)
        return simuleringsResultat.copy(
            simuleringsResultat.simuleringMottaker
                .map { mottaker ->
                    mottaker.copy(
                        simulertPostering = mottaker.simulertPostering.filter { postering ->
                            fagOmrådeKoder.contains(postering.fagOmrådeKode)
                        },
                    )
                },
        )
    }
}
