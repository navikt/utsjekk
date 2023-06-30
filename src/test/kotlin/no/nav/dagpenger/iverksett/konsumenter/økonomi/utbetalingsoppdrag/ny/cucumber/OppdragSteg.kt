package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber

import io.cucumber.datatable.DataTable
import io.cucumber.java.no.Gitt
import io.cucumber.java.no.Når
import io.cucumber.java.no.Så
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.Utbetalingsgenerator
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.ValideringUtil.assertSjekkBehandlingIder
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.domeneparser.Domenebegrep
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.domeneparser.DomenebegrepBehandlingsinformasjon
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.domeneparser.DomenebegrepUtbetalingsoppdrag
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.domeneparser.DomeneparserUtil.groupByBehandlingId
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.domeneparser.ForventetUtbetalingsoppdrag
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.domeneparser.ForventetUtbetalingsperiode
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.domeneparser.OppdragParser
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.domeneparser.OppdragParser.mapAndeler
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.domeneparser.parseLong
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.domeneparser.parseString
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.domeneparser.parseValgfriDato
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.domeneparser.parseValgfriEnum
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.cucumber.domeneparser.parseValgfriLong
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.AndelData
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.AndelMedPeriodeId
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.Behandlingsinformasjon
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.BeregnetUtbetalingsoppdrag
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.StønadTypeOgFerietillegg
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.tilKlassifisering
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.ny.domene.uten0beløp
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsperiode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.UUID

val FAGSAK_ID = UUID.randomUUID().toString()

class OppdragSteg {

    private val logger = LoggerFactory.getLogger(javaClass)

    private var behandlingsinformasjon = mutableMapOf<Long, Behandlingsinformasjon>()
    private var andelerPerBehandlingId = mapOf<Long, List<AndelData>>()
    private var beregnetUtbetalingsoppdrag = mutableMapOf<Long, BeregnetUtbetalingsoppdrag>()

    @Gitt("følgende behandlingsinformasjon")
    fun følgendeBehandlinger(dataTable: DataTable) {
        opprettBehandlingsinformasjon(dataTable)
    }

    @Gitt("følgende tilkjente ytelser")
    fun følgendeTilkjenteYtelser(dataTable: DataTable) {
        genererBehandlingsinformasjonForDeSomMangler(dataTable)
        andelerPerBehandlingId = mapAndeler(dataTable)
        if (
            andelerPerBehandlingId.flatMap { it.value }
                .any { it.periodeId != null || it.forrigePeriodeId != null }
        ) {
            error("Kildebehandling/periodeId/forrigePeriodeId skal ikke settes på input, denne settes fra utbetalingsgeneratorn")
        }
    }

    @Når("beregner utbetalingsoppdrag")
    fun `beregner utbetalingsoppdrag`() {
        andelerPerBehandlingId.entries.fold(emptyList<Pair<Long, List<AndelData>>>()) { acc, andelPåBehandlingId ->
            val behandlingId = andelPåBehandlingId.key
            try {
                val beregnUtbetalingsoppdrag = beregnUtbetalingsoppdrag(acc, andelPåBehandlingId)
                beregnetUtbetalingsoppdrag[behandlingId] = beregnUtbetalingsoppdrag
                val oppdaterteAndeler = oppdaterAndelerMedPeriodeId(beregnUtbetalingsoppdrag, andelPåBehandlingId)

                acc + (behandlingId to oppdaterteAndeler)
            } catch (e: Throwable) {
                logger.error("Feilet beregning av oppdrag for behandling=$behandlingId")
                throw e
            }
        }
    }

    @Når("beregner utbetalingsoppdrag kjøres kastes exception")
    fun `lagTilkjentYtelseMedUtbetalingsoppdrag kjøres kastes exception`(dataTable: DataTable) {
        val throwable = catchThrowable { `beregner utbetalingsoppdrag`() }
        dataTable.asMaps().let { rader ->
            if (rader.size > 1) {
                error("Kan maks inneholde en rad")
            }
            rader.firstOrNull()?.let { rad ->
                rad["Exception"]?.let { assertThat(throwable::class.java.simpleName).isEqualTo(it) }
                rad["Melding"]?.let { assertThat(throwable.message).contains(it) }
            }
        }
    }

    @Så("forvent følgende utbetalingsoppdrag 2")
    fun `forvent følgende utbetalingsoppdrag 2`(dataTable: DataTable) {
        validerForventetUtbetalingsoppdrag(dataTable, beregnetUtbetalingsoppdrag)
        assertSjekkBehandlingIder(dataTable, beregnetUtbetalingsoppdrag)
    }

    @Så("forvent følgende andeler med periodeId")
    fun `forvent følgende andeler med periodeId`(dataTable: DataTable) {
        val groupByBehandlingId = dataTable.groupByBehandlingId()
        groupByBehandlingId.forEach { (behandlingId, rader) ->
            val beregnedeAndeler = beregnetUtbetalingsoppdrag.getValue(behandlingId).andeler
            val forventedeAndeler = rader.map { rad ->
                AndelMedPeriodeId(
                    id = parseString(Domenebegrep.ID, rad),
                    periodeId = parseLong(DomenebegrepUtbetalingsoppdrag.PERIODE_ID, rad),
                    forrigePeriodeId = parseValgfriLong(DomenebegrepUtbetalingsoppdrag.FORRIGE_PERIODE_ID, rad),
                )
            }
            assertThat(beregnedeAndeler).containsExactlyElementsOf(forventedeAndeler)
        }
        assertThat(beregnetUtbetalingsoppdrag.values.map { it.andeler }.filter { it.isNotEmpty() })
            .hasSize(groupByBehandlingId.size)
    }

    private fun opprettBehandlingsinformasjon(dataTable: DataTable) {
        dataTable.groupByBehandlingId().map { (behandlingId, rader) ->
            val rad = rader.single()

            behandlingsinformasjon[behandlingId] = lagBehandlingsinformasjon(
                behandlingId = behandlingId.toString(),
                opphørFra = parseValgfriDato(DomenebegrepBehandlingsinformasjon.OPPHØR_FRA, rad),
                ytelse = parseValgfriEnum<StønadType>(DomenebegrepBehandlingsinformasjon.YTELSE, rad),
            )
        }
    }

    private fun genererBehandlingsinformasjonForDeSomMangler(dataTable: DataTable) {
        dataTable.groupByBehandlingId().forEach { (behandlingId, _) ->
            if (!behandlingsinformasjon.containsKey(behandlingId)) {
                behandlingsinformasjon[behandlingId] = lagBehandlingsinformasjon(behandlingId.toString())
            }
        }
    }

    private fun lagBehandlingsinformasjon(
        behandlingId: String,
        opphørFra: LocalDate? = null,
        ytelse: StønadType? = null,
    ) = Behandlingsinformasjon(
        fagsakId = FAGSAK_ID,
        saksbehandlerId = "saksbehandlerId",
        behandlingId = behandlingId,
        personIdent = "1",
        vedtaksdato = LocalDate.now(),
        opphørFra = opphørFra,
    )

    private fun beregnUtbetalingsoppdrag(
        acc: List<Pair<Long, List<AndelData>>>,
        andeler: Map.Entry<Long, List<AndelData>>,
    ): BeregnetUtbetalingsoppdrag {
        val forrigeKjeder = acc.lastOrNull()?.second ?: emptyList()
        val behandlingId = andeler.key
        val sisteOffsetPerIdent = gjeldendeForrigeOffsetForKjede(acc)
        val behandlingsinformasjon1 = behandlingsinformasjon.getValue(behandlingId)
        return Utbetalingsgenerator.lagUtbetalingsoppdrag(
            behandlingsinformasjon = behandlingsinformasjon1,
            nyeAndeler = andeler.value,
            forrigeAndeler = forrigeKjeder,
            sisteAndelPerKjede = sisteOffsetPerIdent,
        )
    }

    /**
     * Når vi henter forrige offset for en kjede så må vi hente max periodeId, men den første hendelsen av den typen
     * Dette då vi i noen tilfeller opphører en peride, som beholder den samme periodeId'n
     */
    private fun gjeldendeForrigeOffsetForKjede(forrigeKjeder: List<Pair<Long, List<AndelData>>>): Map<StønadTypeOgFerietillegg, AndelData> {
        return forrigeKjeder.flatMap { it.second }
            .uten0beløp()
            .groupBy { it.type }
            .mapValues { it.value.sortedWith(compareByDescending<AndelData> { it.periodeId!! }.thenBy { it.id }).first() }
    }

    private fun oppdaterAndelerMedPeriodeId(
        beregnUtbetalingsoppdrag: BeregnetUtbetalingsoppdrag,
        andelPåBehandlingId: Map.Entry<Long, List<AndelData>>,
    ): List<AndelData> {
        val andelerPerId = beregnUtbetalingsoppdrag.andeler.associateBy { it.id }
        return andelPåBehandlingId.value.map {
            if (it.beløp == 0) {
                it
            } else {
                val andelMedPeriodeId = andelerPerId[it.id]!!
                it.copy(
                    periodeId = andelMedPeriodeId.periodeId,
                    forrigePeriodeId = andelMedPeriodeId.forrigePeriodeId,
                )
            }
        }
    }

    private fun validerForventetUtbetalingsoppdrag(
        dataTable: DataTable,
        beregnetUtbetalingsoppdrag: MutableMap<Long, BeregnetUtbetalingsoppdrag>,
    ) {
        val forventedeUtbetalingsoppdrag = OppdragParser.mapForventetUtbetalingsoppdrag(dataTable)
        forventedeUtbetalingsoppdrag.forEach { forventetUtbetalingsoppdrag ->
            val behandlingId = forventetUtbetalingsoppdrag.behandlingId
            val utbetalingsoppdrag = beregnetUtbetalingsoppdrag[behandlingId]
                ?: error("Mangler utbetalingsoppdrag for $behandlingId")
            try {
                assertUtbetalingsoppdrag(forventetUtbetalingsoppdrag, utbetalingsoppdrag.utbetalingsoppdrag)
            } catch (e: Throwable) {
                logger.error("Feilet validering av behandling $behandlingId")
                throw e
            }
        }
    }

    private fun assertUtbetalingsoppdrag(
        forventetUtbetalingsoppdrag: ForventetUtbetalingsoppdrag,
        utbetalingsoppdrag: Utbetalingsoppdrag,
    ) {
        assertThat(utbetalingsoppdrag.kodeEndring).isEqualTo(forventetUtbetalingsoppdrag.kodeEndring)
        forventetUtbetalingsoppdrag.utbetalingsperiode.forEachIndexed { index, forventetUtbetalingsperiode ->
            val utbetalingsperiode = utbetalingsoppdrag.utbetalingsperiode[index]
            try {
                assertUtbetalingsperiode(utbetalingsperiode, forventetUtbetalingsperiode)
            } catch (e: Throwable) {
                logger.error("Feilet validering av rad $index for oppdrag=${forventetUtbetalingsoppdrag.behandlingId}")
                throw e
            }
        }
        assertThat(utbetalingsoppdrag.utbetalingsperiode).hasSize(forventetUtbetalingsoppdrag.utbetalingsperiode.size)
    }
}

private fun assertUtbetalingsperiode(
    utbetalingsperiode: Utbetalingsperiode,
    forventetUtbetalingsperiode: ForventetUtbetalingsperiode,
) {
    assertThat(utbetalingsperiode.erEndringPåEksisterendePeriode)
        .`as`("erEndringPåEksisterendePeriode")
        .isEqualTo(forventetUtbetalingsperiode.erEndringPåEksisterendePeriode)
    assertThat(utbetalingsperiode.klassifisering)
        .`as`("klassifisering")
        .isEqualTo(StønadTypeOgFerietillegg(forventetUtbetalingsperiode.ytelse, null).tilKlassifisering())
    assertThat(utbetalingsperiode.periodeId)
        .`as`("periodeId")
        .isEqualTo(forventetUtbetalingsperiode.periodeId)
    assertThat(utbetalingsperiode.forrigePeriodeId)
        .`as`("forrigePeriodeId")
        .isEqualTo(forventetUtbetalingsperiode.forrigePeriodeId)
    assertThat(utbetalingsperiode.sats.toInt())
        .`as`("sats")
        .isEqualTo(forventetUtbetalingsperiode.sats)
    assertThat(utbetalingsperiode.satsType)
        .`as`("satsType")
        .isEqualTo(forventetUtbetalingsperiode.satstype)
    assertThat(utbetalingsperiode.vedtakdatoFom)
        .`as`("fom")
        .isEqualTo(forventetUtbetalingsperiode.fom)
    assertThat(utbetalingsperiode.vedtakdatoTom)
        .`as`("tom")
        .isEqualTo(forventetUtbetalingsperiode.tom)
    assertThat(utbetalingsperiode.opphør?.opphørDatoFom)
        .`as`("opphør")
        .isEqualTo(forventetUtbetalingsperiode.opphør)
    forventetUtbetalingsperiode.kildebehandlingId?.let {
        assertThat(utbetalingsperiode.behandlingId)
            .`as`("kildebehandlingId")
            .isEqualTo(forventetUtbetalingsperiode.kildebehandlingId)
    }
}
