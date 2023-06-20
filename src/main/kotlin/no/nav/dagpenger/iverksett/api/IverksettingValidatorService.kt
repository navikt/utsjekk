package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.api.domene.Brev
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.erKonsistentMed
import no.nav.dagpenger.iverksett.api.domene.personIdent
import no.nav.dagpenger.iverksett.api.domene.sakId
import no.nav.dagpenger.iverksett.api.domene.tilKlassifisering
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.dagpenger.iverksett.infrastruktur.configuration.FeatureToggleConfig
import no.nav.dagpenger.iverksett.infrastruktur.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.konsumenter.tilbakekreving.validerTilbakekreving
import no.nav.dagpenger.kontrakter.iverksett.IverksettStatus
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

@Service
class IverksettingValidatorService(
    private val iverksettResultatService: IverksettResultatService,
    private val iverksettingService: IverksettingService,
    private val featureToggleService: FeatureToggleService,
) {

    fun valider(iverksett: IverksettDagpenger) {
        /* Uten DB-oppslag */
        validerAtInnvilgetUtbetalingsvedtakHarUtbetalinger(iverksett)
        validerAtAvslåttVedtakIkkeHarUtbetalinger(iverksett)
        validerAtFraOgMedKommerFørTilOgMedIUtbetalingsperioder(iverksett)
        validerAtUtbetalingsperioderIkkeOverlapperITid(iverksett)
        validerAtUtbetalingerBareHarPositiveBeløp(iverksett)
        validerAtDetFinnesKlassifiseringForStønadstypeOgFerietillegg(iverksett)

        /* Med DB-oppslag */
        validerAtBehandlingIkkeAlleredeErMottatt(iverksett)
        validerKonsistensMellomVedtak(iverksett)
        validerAtIverksettingErForSammeSakOgPersonSomForrige(iverksett)
        validerAtForrigeBehandlingErFerdigIverksatt(iverksett)

        validerTilbakekreving(iverksett)
    }

    private fun validerAtDetFinnesKlassifiseringForStønadstypeOgFerietillegg(iverksett: IverksettDagpenger) {
        val alleHarGyldigKlassifisering = iverksett.vedtak.tilkjentYtelse?.andelerTilkjentYtelse?.all {
            try {
                it.tilKlassifisering()
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        } ?: true

        if (!alleHarGyldigKlassifisering) {
            throw ApiFeil(
                "Klarte ikke å finne klassifisering for kombinasjonen av stønadstype og ferietillegg",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    private fun validerAtUtbetalingerBareHarPositiveBeløp(iverksett: IverksettDagpenger) {
        val alleBeløpErPositive = iverksett.vedtak.tilkjentYtelse?.andelerTilkjentYtelse
            ?.all { it.beløp > 0 } ?: true

        if (!alleBeløpErPositive) {
            throw ApiFeil(
                "Det finnes utbetalinger ikke har positivt belopPerDag",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    private fun validerAtUtbetalingsperioderIkkeOverlapperITid(iverksett: IverksettDagpenger) {
        validerAtFraOgMedKommerFørTilOgMedIUtbetalingsperioder(iverksett)

        val allePerioderErUavhengige = iverksett.vedtak.tilkjentYtelse?.andelerTilkjentYtelse
            ?.sortedBy { it.periode.fom }
            ?.windowed(2, 1, false) {
                val førstePeriode = it.first().periode
                val sistePeriode = it.last().periode

                førstePeriode.tom.isBefore(sistePeriode.fom)
            }?.all { it } ?: true

        if (!allePerioderErUavhengige) {
            throw ApiFeil(
                "Utbetalinger inneholder perioder som overlapper i tid",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    private fun validerAtFraOgMedKommerFørTilOgMedIUtbetalingsperioder(iverksett: IverksettDagpenger) {
        val alleErOk = iverksett.vedtak.tilkjentYtelse?.andelerTilkjentYtelse?.all {
            val fom = it.periode.fom
            val tom = it.periode.tom
            !tom.isBefore(fom)
        } ?: true

        if (!alleErOk) {
            throw ApiFeil(
                "Utbetalinger inneholder perioder der tilOgMedDato er før fraOgMedDato",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    fun validerBrev(iverksettData: IverksettDagpenger, brev: Brev?) {
        when (brev) {
            null -> validerUtenBrev(iverksettData)
            else -> validerSkalHaBrev(iverksettData)
        }
    }

    fun validerUtenBrev(iverksettData: IverksettDagpenger) {
        if (featureToggleService.isEnabled(FeatureToggleConfig.SKAL_SENDE_BREV, false) &&
            !iverksettData.skalIkkeSendeBrev()
        ) {
            throw ApiFeil(
                "Kan ikke ha iverksetting uten brev når det ikke er en migrering, " +
                    "g-omregning eller korrigering uten brev ",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    private fun validerAtInnvilgetUtbetalingsvedtakHarUtbetalinger(iverksett: IverksettDagpenger) {
        if (iverksett.vedtak.tilkjentYtelse == null &&
            iverksett.vedtak.vedtaksresultat == Vedtaksresultat.INNVILGET &&
            iverksett.vedtak.vedtakstype == VedtakType.UTBETALINGSVEDTAK
        ) {
            throw ApiFeil(
                "Kan ikke ha iverksetting av utbertaliingsvedtak uten utbetalinger",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    private fun validerAtAvslåttVedtakIkkeHarUtbetalinger(iverksett: IverksettDagpenger) {
        if (iverksett.vedtak.tilkjentYtelse != null &&
            iverksett.vedtak.vedtaksresultat == Vedtaksresultat.AVSLÅTT
        ) {
            throw ApiFeil(
                "Kan ikke ha iverksetting med utbetalinger når vedtaket er avslått",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    private fun validerTilbakekreving(iverksett: IverksettDagpenger) {
        if (!iverksett.vedtak.tilbakekreving.validerTilbakekreving()) {
            throw ApiFeil("Tilbakekreving er ikke gyldig", HttpStatus.BAD_REQUEST)
        }
    }

    private fun validerKonsistensMellomVedtak(iverksett: IverksettDagpenger) {
        val forrigeIverksettResultat = iverksett.behandling.forrigeBehandlingId?.let {
            iverksettResultatService.hentIverksettResultat(it) ?: throw ApiFeil(
                "Forrige behandling med id $it er ikke mottatt for iverksetting",
                HttpStatus.BAD_REQUEST,
            )
        }

        if (!forrigeIverksettResultat.erKonsistentMed(iverksett.forrigeIverksetting)) {
            throw ApiFeil(
                "Mottatt og faktisk forrige iverksatting er ikke konsistente",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    private fun validerSkalHaBrev(iverksettData: IverksettDagpenger) {
        if (featureToggleService.isEnabled(FeatureToggleConfig.SKAL_SENDE_BREV, false) &&
            iverksettData.skalIkkeSendeBrev()
        ) {
            throw ApiFeil(
                "Kan ikke ha iverksetting med brev når det er migrering, g-omregning eller korrigering uten brev",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    private fun validerAtIverksettingErForSammeSakOgPersonSomForrige(iverksett: IverksettDagpenger) {
        val forrigeIverksett = try {
            iverksettingService.hentForrigeIverksett(iverksett)
        } catch (e: IllegalStateException) {
            throw ApiFeil(e.message ?: "Fant ikke forrige iverksetting", HttpStatus.CONFLICT)
        }

        val forrigeSakId = forrigeIverksett?.sakId
        if (forrigeSakId != null && forrigeSakId != iverksett.sakId) {
            throw ApiFeil(
                "Forrige behandling er knyttet til en annen sak enn denne iverksettingen gjelder",
                HttpStatus.BAD_REQUEST,
            )
        }

        val forrigePersonident = forrigeIverksett?.personIdent
        if (forrigePersonident != null && forrigePersonident != iverksett.personIdent) {
            throw ApiFeil(
                "Forrige behandling er knyttet til en annen person enn denne iverksettingen gjelder",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    private fun validerAtForrigeBehandlingErFerdigIverksatt(iverksett: IverksettDagpenger?) {
        iverksett?.behandling?.forrigeBehandlingId?.apply {
            if (iverksettingService.utledStatus(this) != IverksettStatus.OK) {
                throw ApiFeil("Forrige iverksetting  er ikke ferdig", HttpStatus.CONFLICT)
            }
        }
    }

    private fun validerAtBehandlingIkkeAlleredeErMottatt(iverksett: IverksettDagpenger) {
        if (iverksettingService.hentIverksetting(iverksett.behandling.behandlingId) != null) {
            throw ApiFeil(
                "Behandling med id ${iverksett.behandling.behandlingId} er allerede mottattt",
                HttpStatus.CONFLICT,
            )
        }
    }
}
