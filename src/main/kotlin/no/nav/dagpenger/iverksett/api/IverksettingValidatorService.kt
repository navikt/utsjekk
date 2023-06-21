package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.api.domene.Brev
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.erKonsistentMed
import no.nav.dagpenger.iverksett.api.domene.personIdent
import no.nav.dagpenger.iverksett.api.domene.sakId
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.advice.ApiFeil
import no.nav.dagpenger.iverksett.infrastruktur.configuration.FeatureToggleConfig
import no.nav.dagpenger.iverksett.infrastruktur.featuretoggle.FeatureToggleService
import no.nav.dagpenger.iverksett.konsumenter.tilbakekreving.validerTilbakekreving
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
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
        /* Med DB-oppslag */
        validerAtBehandlingIkkeAlleredeErMottatt(iverksett)
        validerKonsistensMellomVedtak(iverksett)
        validerAtIverksettingErForSammeSakOgPersonSomForrige(iverksett)
        validerAtForrigeBehandlingErFerdigIverksatt(iverksett)

        validerTilbakekreving(iverksett)
    }

    fun validerDto(iverksettDto: IverksettDto) {
        /* Uten DB-oppslag */
        validerAtInnvilgetUtbetalingsvedtakHarUtbetalinger(iverksettDto)
        validerAtAvslåttVedtakIkkeHarUtbetalinger(iverksettDto)
        validerAtFraOgMedKommerFørTilOgMedIUtbetalingsperioder(iverksettDto)
        validerAtUtbetalingsperioderIkkeOverlapperITid(iverksettDto)
        validerAtUtbetalingerBareHarPositiveBeløp(iverksettDto)
        validerAtIngenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød(iverksettDto)
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

    internal fun validerAtIngenUtbetalingsperioderHarStønadstypeEØSOgFerietilleggTilAvdød(iverksettDto: IverksettDto) {
        val ugyldigKombinasjon = iverksettDto.vedtak.utbetalinger.any {
            it.stonadstype == StønadType.DAGPENGER_EOS && it.ferietillegg == Ferietillegg.AVDOD
        }

        if (ugyldigKombinasjon) {
            throw ApiFeil(
                "Ferietillegg til avdød er ikke tillatt for stønadstypen ${StønadType.DAGPENGER_EOS}",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun validerAtUtbetalingerBareHarPositiveBeløp(iverksettDto: IverksettDto) {
        val alleBeløpErPositive = iverksettDto.vedtak.utbetalinger.all {
            val belop = it.belopPerDag ?: it.beløp ?: throw IllegalArgumentException("Fant hverken belopPerDag eller beløp")
            belop > 0
        }

        if (!alleBeløpErPositive) {
            throw ApiFeil(
                "Det finnes utbetalinger ikke har positivt belopPerDag",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun validerAtUtbetalingsperioderIkkeOverlapperITid(iverksettDto: IverksettDto) {
        validerAtFraOgMedKommerFørTilOgMedIUtbetalingsperioder(iverksettDto)

        val allePerioderErUavhengige = iverksettDto.vedtak.utbetalinger
            .sortedBy { it.fraOgMedDato }
            .windowed(2, 1, false) {
                val førstePeriodeTom = it.first().tilOgMedDato
                val sistePeriodeFom = it.last().fraOgMedDato

                førstePeriodeTom.isBefore(sistePeriodeFom)
            }.all { it }

        if (!allePerioderErUavhengige) {
            throw ApiFeil(
                "Utbetalinger inneholder perioder som overlapper i tid",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun validerAtFraOgMedKommerFørTilOgMedIUtbetalingsperioder(iverksettDto: IverksettDto) {
        val alleErOk = iverksettDto.vedtak.utbetalinger.all {
            val fom = it.fraOgMedDato
            val tom = it.tilOgMedDato
            !tom.isBefore(fom)
        }

        if (!alleErOk) {
            throw ApiFeil(
                "Utbetalinger inneholder perioder der tilOgMedDato er før fraOgMedDato",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun validerAtInnvilgetUtbetalingsvedtakHarUtbetalinger(iverksettDto: IverksettDto) {
        if (iverksettDto.vedtak.utbetalinger.isEmpty() &&
            iverksettDto.vedtak.resultat == Vedtaksresultat.INNVILGET &&
            iverksettDto.vedtak.vedtakstype == VedtakType.UTBETALINGSVEDTAK
        ) {
            throw ApiFeil(
                "Kan ikke ha iverksetting av utbertaliingsvedtak uten utbetalinger",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun validerAtAvslåttVedtakIkkeHarUtbetalinger(iverksettDto: IverksettDto) {
        if (iverksettDto.vedtak.utbetalinger.isNotEmpty() &&
            iverksettDto.vedtak.resultat == Vedtaksresultat.AVSLÅTT
        ) {
            throw ApiFeil(
                "Kan ikke ha iverksetting med utbetalinger når vedtaket er avslått",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun validerKonsistensMellomVedtak(iverksett: IverksettDagpenger) {
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

    internal fun validerSkalHaBrev(iverksettData: IverksettDagpenger) {
        if (featureToggleService.isEnabled(FeatureToggleConfig.SKAL_SENDE_BREV, false) &&
            iverksettData.skalIkkeSendeBrev()
        ) {
            throw ApiFeil(
                "Kan ikke ha iverksetting med brev når det er migrering, g-omregning eller korrigering uten brev",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    internal fun validerAtIverksettingErForSammeSakOgPersonSomForrige(iverksett: IverksettDagpenger) {
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

    internal fun validerAtForrigeBehandlingErFerdigIverksatt(iverksett: IverksettDagpenger?) {
        iverksett?.behandling?.forrigeBehandlingId?.apply {
            if (iverksettingService.utledStatus(this) != IverksettStatus.OK) {
                throw ApiFeil("Forrige iverksetting  er ikke ferdig", HttpStatus.CONFLICT)
            }
        }
    }

    internal fun validerAtBehandlingIkkeAlleredeErMottatt(iverksett: IverksettDagpenger) {
        if (iverksettingService.hentIverksetting(iverksett.behandling.behandlingId) != null) {
            throw ApiFeil(
                "Behandling med id ${iverksett.behandling.behandlingId} er allerede mottattt",
                HttpStatus.CONFLICT,
            )
        }
    }

    internal fun validerTilbakekreving(iverksett: IverksettDagpenger) {
        if (!iverksett.vedtak.tilbakekreving.validerTilbakekreving()) {
            throw ApiFeil("Tilbakekreving er ikke gyldig", HttpStatus.BAD_REQUEST)
        }
    }
}
