package no.nav.dagpenger.iverksett.konsumenter.økonomi.simulering

import no.nav.dagpenger.iverksett.api.domene.Simulering
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.dagpenger.iverksett.infrastruktur.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.konsumenter.økonomi.OppdragClient
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.UtbetalingsoppdragGenerator
import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import no.nav.dagpenger.iverksett.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.dagpenger.iverksett.kontrakter.simulering.BeriketSimuleringsresultat
import no.nav.dagpenger.iverksett.kontrakter.simulering.DetaljertSimuleringResultat
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
        if (featureToggleService.isEnabled("familie.ef.iverksett.stopp-iverksetting")) {
            error("Kan ikke sende inn simmulere")
        }
        try {
            val forrigeTilkjentYtelse = simulering.forrigeBehandlingId?.let {
                iverksettResultatService.hentTilkjentYtelse(simulering.forrigeBehandlingId)
            }

            val tilkjentYtelseMedUtbetalingsoppdrag =
                UtbetalingsoppdragGenerator.lagTilkjentYtelseMedUtbetalingsoppdrag(
                    simulering.nyTilkjentYtelseMedMetaData,
                    forrigeTilkjentYtelse,
                )

            val utbetalingsoppdrag = tilkjentYtelseMedUtbetalingsoppdrag.utbetalingsoppdrag
                ?: error("Utbetalingsoppdraget finnes ikke for tilkjent ytelse")

            if (utbetalingsoppdrag.utbetalingsperiode.isEmpty()) {
                return DetaljertSimuleringResultat(emptyList())
            }
            return hentSimuleringsresultatOgFiltrerPosteringer(
                utbetalingsoppdrag,
                simulering.nyTilkjentYtelseMedMetaData.stønadstype,
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
        if (featureToggleService.isEnabled("familie.ef.iverksett.stopp-iverksetting")) {
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
