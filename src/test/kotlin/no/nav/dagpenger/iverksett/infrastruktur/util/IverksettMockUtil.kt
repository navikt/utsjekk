package no.nav.dagpenger.iverksett.infrastruktur.util

import no.nav.dagpenger.iverksett.api.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.Barn
import no.nav.dagpenger.iverksett.api.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.Brev
import no.nav.dagpenger.iverksett.api.domene.Delvilkårsvurdering
import no.nav.dagpenger.iverksett.api.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.IverksettResultat
import no.nav.dagpenger.iverksett.api.domene.OppdragResultat
import no.nav.dagpenger.iverksett.api.domene.Søker
import no.nav.dagpenger.iverksett.api.domene.TilbakekrevingMedVarsel
import no.nav.dagpenger.iverksett.api.domene.TilbakekrevingResultat
import no.nav.dagpenger.iverksett.api.domene.Tilbakekrevingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelseMedMetaData
import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerDagpenger
import no.nav.dagpenger.iverksett.api.domene.VedtaksperiodeDagpenger
import no.nav.dagpenger.iverksett.api.domene.Vilkårsvurdering
import no.nav.dagpenger.iverksett.api.domene.Vurdering
import no.nav.dagpenger.iverksett.api.domene.ÅrsakRevurdering
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottakere
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.DistribuerBrevResultat
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.DistribuerBrevResultatMap
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.JournalpostResultat
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.JournalpostResultatMap
import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagUtbetalingDto
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.felles.Tilbakekrevingsvalg
import no.nav.dagpenger.kontrakter.iverksett.BehandlingType
import no.nav.dagpenger.kontrakter.iverksett.BehandlingÅrsak
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg
import no.nav.dagpenger.kontrakter.iverksett.ForrigeIverksettingDto
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.OpphørÅrsak
import no.nav.dagpenger.kontrakter.iverksett.Opplysningskilde
import no.nav.dagpenger.kontrakter.iverksett.RegelId
import no.nav.dagpenger.kontrakter.iverksett.Revurderingsårsak
import no.nav.dagpenger.kontrakter.iverksett.SvarId
import no.nav.dagpenger.kontrakter.iverksett.TilkjentYtelseDto
import no.nav.dagpenger.kontrakter.iverksett.TilkjentYtelseStatus
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeType
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import no.nav.dagpenger.kontrakter.iverksett.VedtaksstatusDto
import no.nav.dagpenger.kontrakter.iverksett.VilkårType
import no.nav.dagpenger.kontrakter.iverksett.Vilkårsresultat
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.Random
import java.util.UUID

fun opprettIverksettDto(
    behandlingId: UUID = UUID.randomUUID(),
    sakId: UUID = UUID.randomUUID(),
    andelsbeløp: Int = 5000,
    vedtaksresultat: Vedtaksresultat = Vedtaksresultat.INNVILGET,
    stønadType: StønadType = StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER,
    ferietillegg: Ferietillegg? = null,
    vedtaksperioder: List<VedtaksperiodeDto> = emptyList(),
): IverksettDto {
    val andelTilkjentYtelse = lagUtbetalingDto(
        beløp = andelsbeløp,
        fraOgMed = LocalDate.of(2021, 1, 1),
        tilOgMed = LocalDate.of(2021, 12, 31),
        stønadstype = stønadType,
        ferietillegg = ferietillegg,
    )
    val tilkjentYtelse = TilkjentYtelseDto(
        utbetalinger = listOf(andelTilkjentYtelse),
        startdato = andelTilkjentYtelse.fraOgMedDato,
    )

    return IverksettDto(
        behandlingId = behandlingId,
        sakId = sakId,
        personIdent = "12345678910",
        vedtak = VedtaksdetaljerDto(
            resultat = vedtaksresultat,
            vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
            saksbehandlerId = "A12345",
            beslutterId = "B23456",
            utbetalinger = tilkjentYtelse.utbetalinger,
            vedtaksperioder = vedtaksperioder,
        ),
    )
}

fun opprettAndelTilkjentYtelse(
    beløp: Int = 5000,
    fra: LocalDate = LocalDate.of(2021, 1, 1),
    til: LocalDate = LocalDate.of(2021, 12, 31),
) = lagAndelTilkjentYtelse(
    beløp = beløp,
    fraOgMed = fra,
    tilOgMed = til,
)

private val eksternIdGenerator = Random()

fun opprettTilkjentYtelseMedMetadata(
    behandlingId: UUID = UUID.randomUUID(),
    tilkjentYtelse: TilkjentYtelse = opprettTilkjentYtelse(behandlingId),
): TilkjentYtelseMedMetaData {
    return TilkjentYtelseMedMetaData(
        tilkjentYtelse = tilkjentYtelse,
        saksbehandlerId = "saksbehandlerId",
        stønadstype = StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER,
        sakId = UUID.randomUUID(),
        personIdent = "12345678910",
        behandlingId = behandlingId,
        vedtaksdato = LocalDate.of(2021, 1, 1),
    )
}

fun opprettTilkjentYtelse(
    behandlingId: UUID = UUID.randomUUID(),
    andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
    startdato: LocalDate = startdato(andeler),
    sisteAndelIKjede: AndelTilkjentYtelse? = null,
): TilkjentYtelse {
    return TilkjentYtelse(
        id = behandlingId,
        utbetalingsoppdrag = null,
        andelerTilkjentYtelse = andeler,
        startdato = startdato,
        sisteAndelIKjede = sisteAndelIKjede,
    )
}

fun behandlingsdetaljer(
    behandlingId: UUID = UUID.randomUUID(),
    forrigeBehandlingId: UUID? = null,
    behandlingType: BehandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
    behandlingÅrsak: BehandlingÅrsak = BehandlingÅrsak.SØKNAD,
): Behandlingsdetaljer {
    return Behandlingsdetaljer(
        behandlingId = behandlingId,
        forrigeBehandlingId = forrigeBehandlingId,
        behandlingType = behandlingType,
        behandlingÅrsak = behandlingÅrsak,
        relatertBehandlingId = null,
        vilkårsvurderinger = listOf(
            Vilkårsvurdering(
                vilkårType = VilkårType.SAGT_OPP_ELLER_REDUSERT,
                resultat = Vilkårsresultat.OPPFYLT,
                delvilkårsvurderinger = listOf(
                    Delvilkårsvurdering(
                        resultat = Vilkårsresultat.OPPFYLT,
                        vurderinger = listOf(
                            Vurdering(
                                regelId = RegelId.SAGT_OPP_ELLER_REDUSERT,
                                svar = SvarId.JA,
                                begrunnelse = "Nei",
                            ),
                        ),
                    ),
                ),
            ),
        ),
        kravMottatt = LocalDate.of(2021, 3, 3),
        årsakRevurdering = ÅrsakRevurdering(Opplysningskilde.MELDING_MODIA, Revurderingsårsak.ENDRING_INNTEKT),

    )
}

fun vedtaksperioderDagpenger() =
    VedtaksperiodeDagpenger(
        periode = Datoperiode(YearMonth.now().atDay(1), YearMonth.now().atEndOfMonth()),
        periodeType = VedtaksperiodeType.HOVEDPERIODE,
    )

fun vedtaksdetaljerDagpenger(
    vedtaksresultat: Vedtaksresultat = Vedtaksresultat.INNVILGET,
    andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
    tilbakekreving: Tilbakekrevingsdetaljer? = null,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.of(2021, 5, 12, 0, 0),
    startdato: LocalDate = startdato(andeler),
    vedtaksperioder: List<VedtaksperiodeDagpenger> = listOf(vedtaksperioderDagpenger()),
    brevmottakere: Brevmottakere = Brevmottakere(emptyList()),
): VedtaksdetaljerDagpenger {
    val tilkjentYtelse = lagTilkjentYtelse(andeler, startdato)
    return VedtaksdetaljerDagpenger(
        vedtakstype = VedtakType.UTBETALINGSVEDTAK,
        vedtaksresultat = vedtaksresultat,
        vedtakstidspunkt = vedtakstidspunkt,
        opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
        saksbehandlerId = "A12345",
        beslutterId = "B23456",
        tilkjentYtelse = tilkjentYtelse,
        vedtaksperioder = vedtaksperioder,
        tilbakekreving = tilbakekreving,
        brevmottakere = brevmottakere,
    )
}

fun vedtaksstatusDto(
    vedtakstype: VedtakType = VedtakType.RAMMEVEDTAK,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.of(2021, 5, 12, 0, 0),
    resultat: Vedtaksresultat = Vedtaksresultat.INNVILGET,
    vedtaksperioder: List<VedtaksperiodeDto> = listOf(
        VedtaksperiodeDto(LocalDate.now(), LocalDate.now().plusDays(7), VedtaksperiodeType.HOVEDPERIODE),
    ),
): VedtaksstatusDto {
    return VedtaksstatusDto(
        vedtakstype = vedtakstype,
        vedtakstidspunkt = vedtakstidspunkt,
        resultat = resultat,
        vedtaksperioder = vedtaksperioder,
    )
}

private fun lagTilkjentYtelse(
    andeler: List<AndelTilkjentYtelse>,
    startdato: LocalDate,
): TilkjentYtelse =
    TilkjentYtelse(
        id = UUID.randomUUID(),
        utbetalingsoppdrag = null,
        status = TilkjentYtelseStatus.AKTIV,
        andelerTilkjentYtelse = andeler,
        startdato = startdato,
    )

fun opprettIverksettDagpenger(
    behandlingsdetaljer: Behandlingsdetaljer = behandlingsdetaljer(),
    vedtaksdetaljer: VedtaksdetaljerDagpenger = vedtaksdetaljerDagpenger(),
) =
    IverksettDagpenger(
        fagsak = Fagsakdetaljer(fagsakId = UUID.randomUUID(), stønadstype = StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER),
        behandling = behandlingsdetaljer,
        søker = Søker(
            personIdent = "12345678910",
            barn = emptyList(),
            tilhørendeEnhet = "4489",
        ),
        vedtak = vedtaksdetaljer,
    )

fun opprettIverksettDagpenger(
    behandlingId: UUID = UUID.randomUUID(),
    forrigeBehandlingId: UUID? = null,
    andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
    tilbakekreving: Tilbakekrevingsdetaljer? = null,
    startdato: LocalDate = startdato(andeler),
    forrigeIverksetting: IverksettDagpenger? = null,
    fagsakId: UUID = UUID.randomUUID(),
): IverksettDagpenger {
    val behandlingType = forrigeBehandlingId?.let { BehandlingType.REVURDERING } ?: BehandlingType.FØRSTEGANGSBEHANDLING
    return IverksettDagpenger(
        fagsak = Fagsakdetaljer(fagsakId = fagsakId, stønadstype = StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER),
        behandling = behandlingsdetaljer(behandlingId, forrigeBehandlingId, behandlingType),
        søker = Søker(
            personIdent = "12345678910",
            barn = listOf(Barn("01010199999"), Barn(null, LocalDate.of(2023, 1, 1))),
            tilhørendeEnhet = "4489",
        ),
        vedtak = vedtaksdetaljerDagpenger(
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            andeler = andeler,
            tilbakekreving = tilbakekreving,
            startdato = startdato,
        ),
        forrigeIverksetting = forrigeIverksetting,
    )
}

fun startdato(andeler: List<AndelTilkjentYtelse>) =
    andeler.minOfOrNull { it.periode.fom } ?: error("Trenger å sette startdato hvs det ikke finnes andeler")

fun opprettBrev(): Brev {
    return Brev(ByteArray(256))
}

fun opprettTilbakekrevingsdetaljer(): Tilbakekrevingsdetaljer =
    Tilbakekrevingsdetaljer(
        tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
        tilbakekrevingMedVarsel = opprettTilbakekrevingMedVarsel(),
    )

fun opprettTilbakekrevingMedVarsel(
    sumFeilutbetaling: BigDecimal = BigDecimal.valueOf(100),
    perioder: List<Datoperiode> = listOf(
        Datoperiode(
            fom = LocalDate.of(2021, 5, 1),
            tom = LocalDate.of(2021, 6, 30),
        ),
    ),
) = TilbakekrevingMedVarsel(
    varseltekst = "varseltekst",
    sumFeilutbetaling = sumFeilutbetaling,
    perioder = perioder,
)

class IverksettResultatMockBuilder private constructor(
    val tilkjentYtelse: TilkjentYtelse,
) {

    data class Builder(
        var oppdragResultat: OppdragResultat? = null,
        var journalpostResultat: Map<String, JournalpostResultat> = mapOf(),
        var vedtaksbrevResultat: Map<String, DistribuerBrevResultat> = mapOf(),
        var tilbakekrevingResultat: TilbakekrevingResultat? = null,
    ) {

        fun oppdragResultat(oppdragResultat: OppdragResultat) = apply { this.oppdragResultat = oppdragResultat }
        fun journalPostResultat() = apply {
            this.journalpostResultat = mapOf("123456789" to JournalpostResultat(UUID.randomUUID().toString()))
        }

        fun vedtaksbrevResultat(behandlingId: UUID) =
            apply {
                this.vedtaksbrevResultat =
                    mapOf(
                        this.journalpostResultat.entries.first().value.journalpostId to DistribuerBrevResultat(
                            bestillingId = behandlingId.toString(),
                        ),
                    )
            }

        fun build(behandlingId: UUID, tilkjentYtelse: TilkjentYtelse?) =
            IverksettResultat(
                behandlingId,
                tilkjentYtelse,
                oppdragResultat,
                JournalpostResultatMap(journalpostResultat),
                DistribuerBrevResultatMap(vedtaksbrevResultat),
                tilbakekrevingResultat,
            )
    }
}

fun lagForrigeIverksetting(
    forrigeBehandlingId: UUID = UUID.randomUUID(),
    belopPerDag: Int = 400,
    fraOgMedDato: LocalDate = LocalDate.now(),
    tilOgMedDato: LocalDate = fraOgMedDato.plusDays(14),
    utbetalinger: List<UtbetalingDto> = listOf(
        UtbetalingDto(
            fraOgMedDato = fraOgMedDato,
            tilOgMedDato = tilOgMedDato,
            belopPerDag = belopPerDag,
        ),
    ),
) = ForrigeIverksettingDto(
    behandlingId = forrigeBehandlingId,
    utbetalinger = utbetalinger,
)
