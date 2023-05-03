package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag

import no.nav.dagpenger.iverksett.api.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelseMedMetaData
import no.nav.dagpenger.iverksett.infrastruktur.util.tilFagsystem
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ØkonomiUtils.andelerTilOpprettelse
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ØkonomiUtils.andelerUtenNullVerdier
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ØkonomiUtils.beståendeAndeler
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ØkonomiUtils.utbetalingsperiodeForOpphør
import no.nav.dagpenger.iverksett.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.dagpenger.iverksett.kontrakter.oppdrag.Utbetalingsoppdrag.KodeEndring.ENDR
import no.nav.dagpenger.iverksett.kontrakter.oppdrag.Utbetalingsoppdrag.KodeEndring.NY
import no.nav.dagpenger.iverksett.kontrakter.oppdrag.Utbetalingsperiode
import java.time.LocalDate
import java.util.UUID

object UtbetalingsoppdragGenerator {

    /**
     * Lager utbetalingsoppdrag med kjedede perioder av andeler.
     * Ved opphør sendes @param[nyTilkjentYtelseMedMetaData] uten andeler.
     *
     * @param[nyTilkjentYtelseMedMetaData] Den nye tilkjente ytelsen, med fullstending sett av andeler
     * @param[forrigeTilkjentYtelse] Forrige tilkjent ytelse, med fullstendig sett av andeler med id
     * @return Ny tilkjent ytelse med andeler med id'er, samt utbetalingsoppdrag
     */
    fun lagTilkjentYtelseMedUtbetalingsoppdrag(
        nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData,
        forrigeTilkjentYtelse: TilkjentYtelse? = null,
        erGOmregning: Boolean = false,
    ): TilkjentYtelse {
        val nyTilkjentYtelse = nyTilkjentYtelseMedMetaData.tilkjentYtelse
        val andelerNyTilkjentYtelse = andelerUtenNullVerdier(nyTilkjentYtelse)
        val andelerForrigeTilkjentYtelse = andelerUtenNullVerdier(forrigeTilkjentYtelse)
        val sistePeriodeIdIForrigeKjede = sistePeriodeId(forrigeTilkjentYtelse)

        val beståendeAndeler = beståendeAndeler(andelerForrigeTilkjentYtelse, andelerNyTilkjentYtelse)
        val andelerTilOpprettelse = andelerTilOpprettelse(andelerNyTilkjentYtelse, beståendeAndeler)

        val andelerTilOpprettelseMedPeriodeId = lagAndelerMedPeriodeId(
            andelerTilOpprettelse,
            sistePeriodeIdIForrigeKjede,
            nyTilkjentYtelseMedMetaData.behandlingId,
        )

        val utbetalingsperioderSomOpprettes = lagUtbetalingsperioderForOpprettelse(
            andeler = andelerTilOpprettelseMedPeriodeId,
            tilkjentYtelse = nyTilkjentYtelseMedMetaData,
        )

        val utbetalingsperiodeSomOpphøres = utbetalingsperiodeForOpphør(forrigeTilkjentYtelse, nyTilkjentYtelseMedMetaData)

        val utbetalingsperioder = (utbetalingsperioderSomOpprettes + utbetalingsperiodeSomOpphøres)
            .filterNotNull()
            .sortedBy { it.periodeId }
        val utbetalingsoppdrag =
            Utbetalingsoppdrag(
                saksbehandlerId = nyTilkjentYtelseMedMetaData.saksbehandlerId,
                kodeEndring = if (erIkkeTidligereIverksattMotOppdrag(forrigeTilkjentYtelse)) NY else ENDR,
                fagSystem = nyTilkjentYtelseMedMetaData.stønadstype.tilFagsystem(),
                saksnummer = nyTilkjentYtelseMedMetaData.eksternFagsakId.toString(),
                aktoer = nyTilkjentYtelseMedMetaData.personIdent,
                utbetalingsperiode = utbetalingsperioder,
                gOmregning = erGOmregning,
            )

        val gjeldendeAndeler = (beståendeAndeler + andelerTilOpprettelseMedPeriodeId)
            .ellerNullAndel(nyTilkjentYtelseMedMetaData, sistePeriodeIdIForrigeKjede)

        val sisteAndelIKjede = sisteAndelIKjede(gjeldendeAndeler, forrigeTilkjentYtelse)

        return nyTilkjentYtelse.copy(
            utbetalingsoppdrag = utbetalingsoppdrag,
            andelerTilkjentYtelse = gjeldendeAndeler,
            sisteAndelIKjede = sisteAndelIKjede,
        )
        // TODO legge til startperiode, sluttperiode, opphørsdato. Se i BA-sak - legges på i konsistensavstemming?
    }

    private fun sisteAndelIKjede(
        gjeldendeAndeler: List<AndelTilkjentYtelse>,
        forrigeTilkjentYtelse: TilkjentYtelse?,
    ) =
        (gjeldendeAndeler + listOfNotNull(forrigeTilkjentYtelse?.sisteAndelIKjede))
            .filter { it.periodeId != null }
            .filter { it.periode.fom != LocalDate.MIN }
            .maxByOrNull { it.periodeId ?: error("Mangler periodeId") }

    private fun erIkkeTidligereIverksattMotOppdrag(forrigeTilkjentYtelse: TilkjentYtelse?) =
        forrigeTilkjentYtelse == null || (forrigeTilkjentYtelseManglerPeriodeOgErNy(forrigeTilkjentYtelse))

    private fun forrigeTilkjentYtelseManglerPeriodeOgErNy(forrigeTilkjentYtelse: TilkjentYtelse): Boolean {
        val utbetalingsoppdrag = forrigeTilkjentYtelse.utbetalingsoppdrag
            ?: error("Mangler utbetalingsoppdrag for tilkjentYtelse=${forrigeTilkjentYtelse.id}")
        return utbetalingsoppdrag.utbetalingsperiode.isEmpty() && utbetalingsoppdrag.kodeEndring == NY
    }

    /**
     * Hvis det ikke er noen andeler igjen, må vi opprette en "null-andel" som tar vare på periodeId'en for ytelsestypen
     */
    private fun List<AndelTilkjentYtelse>.ellerNullAndel(
        nyTilkjentYtelseMedMetaData: TilkjentYtelseMedMetaData,
        sistePeriodeIdIForrigeKjede: PeriodeId?,
    ): List<AndelTilkjentYtelse> {
        return this.ifEmpty {
            listOf(nullAndelTilkjentYtelse(nyTilkjentYtelseMedMetaData.behandlingId, sistePeriodeIdIForrigeKjede))
        }
    }

    private fun lagUtbetalingsperioderForOpprettelse(
        andeler: List<AndelTilkjentYtelse>,
        tilkjentYtelse: TilkjentYtelseMedMetaData,
    ): List<Utbetalingsperiode> {
        return andeler.map {
            lagPeriodeFraAndel(
                andel = it,
                type = tilkjentYtelse.stønadstype,
                eksternBehandlingId = tilkjentYtelse.eksternBehandlingId,
                vedtaksdato = tilkjentYtelse.vedtaksdato,
                personIdent = tilkjentYtelse.personIdent,
            )
        }
    }

    private fun lagAndelerMedPeriodeId(
        andeler: List<AndelTilkjentYtelse>,
        sisteOffsetIKjedeOversikt: PeriodeId?,
        kildeBehandlingId: UUID,
    ): List<AndelTilkjentYtelse> {
        val forrigePeriodeIdIKjede: Long? = sisteOffsetIKjedeOversikt?.gjeldende
        val nestePeriodeIdIKjede = forrigePeriodeIdIKjede?.plus(1) ?: 1

        return andeler.sortedBy { it.periode }.mapIndexed { index, andel ->
            andel.copy(
                periodeId = nestePeriodeIdIKjede + index,
                kildeBehandlingId = kildeBehandlingId,
                forrigePeriodeId = if (index == 0) forrigePeriodeIdIKjede else nestePeriodeIdIKjede + index - 1,
            )
        }
    }

    private fun sistePeriodeId(tilkjentYtelse: TilkjentYtelse?): PeriodeId? {
        return tilkjentYtelse?.let { ytelse ->
            ytelse.sisteAndelIKjede?.tilPeriodeId()
                // TODO denne kan fjernes når den er patchet
                ?: ytelse.andelerTilkjentYtelse.filter { it.periodeId != null }.maxByOrNull { it.periodeId!! }?.tilPeriodeId()
        }
    }
}
