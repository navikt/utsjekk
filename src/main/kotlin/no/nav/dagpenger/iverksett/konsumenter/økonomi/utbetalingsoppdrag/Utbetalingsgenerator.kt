package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag

import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.BeståendeAndelerBeregner.finnBeståendeAndeler
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.OppdragBeregnerUtil.validerAndeler
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.AndelData
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.AndelMedPeriodeId
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.Behandlingsinformasjon
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.BeregnetUtbetalingsoppdrag
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.StønadTypeOgFerietillegg
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.uten0beløp
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsperiode
import java.time.LocalDate

object Utbetalingsgenerator {

    /**
     * Generer utbetalingsoppdrag som sendes til oppdrag
     *
     * @param sisteAndelPerKjede må sende inn siste andelen per kjede for å peke/opphøre riktig forrige andel
     * Siste andelen er første andelen med høyeste periodeId, per ident/type, dvs hvis man har avkortet en periode,
     * og fått et nytt tom, så skal man bruke den opprinnelige perioden for det periodeId'et
     * ex
     * SELECT * FROM (SELECT aty.id,
     *        row_number() OVER (PARTITION BY aty.type, aty.fk_aktoer_id ORDER BY aty.periode_offset DESC, x.opprettet_tid ASC) rn
     * FROM andel_tilkjent_ytelse aty) WHERE rn = 1
     *
     * [sisteAndelPerKjede] brukes også for å utlede om utbetalingsoppdraget settes til NY eller ENDR
     *
     * @return [BeregnetUtbetalingsoppdrag] som inneholder både utbetalingsoppdraget og [BeregnetUtbetalingsoppdrag.andeler]
     * som inneholder periodeId/forrigePeriodeId for å kunne oppdatere andeler i basen
     */
    fun lagUtbetalingsoppdrag(
        behandlingsinformasjon: Behandlingsinformasjon,
        nyeAndeler: List<AndelData>,
        forrigeAndeler: List<AndelData>,
        sisteAndelPerKjede: Map<StønadTypeOgFerietillegg, AndelData>,
    ): BeregnetUtbetalingsoppdrag {
        validerAndeler(forrigeAndeler, nyeAndeler)
        val nyeAndelerGruppert = nyeAndeler.groupByStønadTypeOgFerietillegg()
        val forrigeKjeder = forrigeAndeler.groupByStønadTypeOgFerietillegg()

        return lagUtbetalingsoppdrag(
            nyeAndeler = nyeAndelerGruppert,
            forrigeKjeder = forrigeKjeder,
            sisteAndelPerKjede = sisteAndelPerKjede,
            behandlingsinformasjon = behandlingsinformasjon,
        )
    }

    private fun lagUtbetalingsoppdrag(
        nyeAndeler: Map<StønadTypeOgFerietillegg, List<AndelData>>,
        forrigeKjeder: Map<StønadTypeOgFerietillegg, List<AndelData>>,
        sisteAndelPerKjede: Map<StønadTypeOgFerietillegg, AndelData>,
        behandlingsinformasjon: Behandlingsinformasjon,
    ): BeregnetUtbetalingsoppdrag {
        val nyeKjeder = lagNyeKjeder(nyeAndeler, forrigeKjeder, sisteAndelPerKjede)
        val fagsystem = forrigeKjeder.keys.union(nyeAndeler.keys).first().stønadstype.tilFagsystem()

        val utbetalingsoppdrag = Utbetalingsoppdrag(
            saksbehandlerId = behandlingsinformasjon.saksbehandlerId,
            kodeEndring = kodeEndring(sisteAndelPerKjede),
            saksnummer = behandlingsinformasjon.fagsakId,
            fagSystem = fagsystem,
            saksreferanse = behandlingsinformasjon.saksreferanse,
            aktoer = behandlingsinformasjon.personIdent,
            enhet = behandlingsinformasjon.enhet,
            utbetalingsperiode = utbetalingsperioder(behandlingsinformasjon, nyeKjeder),
            gOmregning = behandlingsinformasjon.erGOmregning,
        )

        return BeregnetUtbetalingsoppdrag(
            utbetalingsoppdrag,
            lagAndelerMedPeriodeId(nyeKjeder),
        )
    }

    private fun lagNyeKjeder(
        nyeKjeder: Map<StønadTypeOgFerietillegg, List<AndelData>>,
        forrigeKjeder: Map<StønadTypeOgFerietillegg, List<AndelData>>,
        sisteAndelPerKjede: Map<StønadTypeOgFerietillegg, AndelData>,
    ): List<ResultatForKjede> {
        val alleStønadTypeOgFerietillegg = nyeKjeder.keys + forrigeKjeder.keys
        var sistePeriodeId = sisteAndelPerKjede.values.mapNotNull { it.periodeId }.maxOrNull() ?: -1
        return alleStønadTypeOgFerietillegg.map { stønadTypeOgFerietillegg ->
            val forrigeAndeler = forrigeKjeder[stønadTypeOgFerietillegg] ?: emptyList()
            val nyeAndeler = nyeKjeder[stønadTypeOgFerietillegg] ?: emptyList()
            val sisteAndel = sisteAndelPerKjede[stønadTypeOgFerietillegg]
            val opphørsdato = finnOpphørsdato(forrigeAndeler, nyeAndeler)

            val nyKjede = beregnNyKjede(
                forrigeAndeler.uten0beløp(),
                nyeAndeler.uten0beløp(),
                sisteAndel,
                sistePeriodeId,
                opphørsdato,
            )
            sistePeriodeId = nyKjede.sistePeriodeId
            nyKjede
        }
    }

    /**
     * For å unngå unøvendig 0-sjekk senere, så sjekkes det for om man
     * må opphøre alle andeler pga nye 0-andeler som har startdato før forrige første periode
     */
    private fun finnOpphørsdato(forrigeAndeler: List<AndelData>, nyeAndeler: List<AndelData>): LocalDate? {
        val forrigeFørsteAndel = forrigeAndeler.firstOrNull()
        val nyFørsteAndel = nyeAndeler.firstOrNull()
        if (
            forrigeFørsteAndel != null && nyFørsteAndel != null &&
            nyFørsteAndel.beløp == 0 && nyFørsteAndel.fom < forrigeFørsteAndel.fom
        ) {
            return nyFørsteAndel.fom
        }
        return null
    }

    private fun utbetalingsperioder(
        behandlingsinformasjon: Behandlingsinformasjon,
        nyeKjeder: List<ResultatForKjede>,
    ): List<Utbetalingsperiode> {
        val opphørsperioder = lagOpphørsperioder(behandlingsinformasjon, nyeKjeder.mapNotNull { it.opphørsandel })
        val nyePerioder = lagNyePerioder(behandlingsinformasjon, nyeKjeder.flatMap { it.nyeAndeler })
        return opphørsperioder + nyePerioder
    }

    private fun lagAndelerMedPeriodeId(
        nyeKjeder: List<ResultatForKjede>,
    ): List<AndelMedPeriodeId> = nyeKjeder.flatMap { nyKjede ->
        nyKjede.beståendeAndeler.map { AndelMedPeriodeId(it) } + nyKjede.nyeAndeler.map {
            AndelMedPeriodeId(it)
        }
    }

    // Hos økonomi skiller man på endring på oppdragsnivå 110 og på linjenivå 150 (periodenivå).
    // Da de har opplevd å motta
    // UEND på oppdrag som skulle vært ENDR anbefaler de at kun ENDR brukes når sak
    // ikke er ny, så man slipper å forholde seg til om det er endring over 150-nivå eller ikke.
    private fun kodeEndring(sisteAndelMedPeriodeId: Map<StønadTypeOgFerietillegg, AndelData>) =
        if (sisteAndelMedPeriodeId.isEmpty()) Utbetalingsoppdrag.KodeEndring.NY else Utbetalingsoppdrag.KodeEndring.ENDR

    private fun beregnNyKjede(
        forrige: List<AndelData>,
        nye: List<AndelData>,
        sisteAndel: AndelData?,
        periodeId: Long,
        opphørsdato: LocalDate?,
    ): ResultatForKjede {
        val beståendeAndeler = finnBeståendeAndeler(forrige, nye, opphørsdato)
        val nyeAndeler = nye.subList(beståendeAndeler.andeler.size, nye.size)

        val (nyeAndelerMedPeriodeId, gjeldendePeriodeId) = nyeAndelerMedPeriodeId(nyeAndeler, periodeId, sisteAndel)
        return ResultatForKjede(
            beståendeAndeler = beståendeAndeler.andeler,
            nyeAndeler = nyeAndelerMedPeriodeId,
            opphørsandel = beståendeAndeler.opphørFra?.let {
                Pair(sisteAndel ?: error("Må ha siste andel for å kunne opphøre"), it)
            },
            sistePeriodeId = gjeldendePeriodeId,
        )
    }

    private fun nyeAndelerMedPeriodeId(
        nyeAndeler: List<AndelData>,
        periodeId: Long,
        sisteAndel: AndelData?,
    ): Pair<List<AndelData>, Long> {
        var gjeldendePeriodeId = periodeId
        var forrigePeriodeId = sisteAndel?.periodeId
        val nyeAndelerMedPeriodeId = nyeAndeler.mapIndexed { _, andelData ->
            gjeldendePeriodeId += 1
            val nyAndel = andelData.copy(periodeId = gjeldendePeriodeId, forrigePeriodeId = forrigePeriodeId)
            forrigePeriodeId = gjeldendePeriodeId
            nyAndel
        }
        return Pair(nyeAndelerMedPeriodeId, gjeldendePeriodeId)
    }

    private fun List<AndelData>.groupByStønadTypeOgFerietillegg(): Map<StønadTypeOgFerietillegg, List<AndelData>> =
        groupBy { it.type }.mapValues { andel -> andel.value.sortedBy { it.fom } }

    private fun lagOpphørsperioder(
        behandlingsinformasjon: Behandlingsinformasjon,
        andeler: List<Pair<AndelData, LocalDate>>,
    ): List<Utbetalingsperiode> {
        val utbetalingsperiodeMal = UtbetalingsperiodeMal(
            behandlingsinformasjon = behandlingsinformasjon,
            erEndringPåEksisterendePeriode = true,
        )

        return andeler.map {
            utbetalingsperiodeMal.lagPeriodeFraAndel(it.first, opphørKjedeFom = it.second)
        }
    }

    private fun lagNyePerioder(
        behandlingsinformasjon: Behandlingsinformasjon,
        andeler: List<AndelData>,
    ): List<Utbetalingsperiode> {
        val utbetalingsperiodeMal = UtbetalingsperiodeMal(behandlingsinformasjon = behandlingsinformasjon)
        return andeler.map { utbetalingsperiodeMal.lagPeriodeFraAndel(it) }
    }
}
