package no.nav.dagpenger.iverksett.util

import no.nav.dagpenger.iverksett.brev.domain.Brevmottakere
import no.nav.dagpenger.iverksett.brev.domain.DistribuerBrevResultat
import no.nav.dagpenger.iverksett.brev.domain.DistribuerBrevResultatMap
import no.nav.dagpenger.iverksett.brev.domain.JournalpostResultat
import no.nav.dagpenger.iverksett.brev.domain.JournalpostResultatMap
import no.nav.dagpenger.iverksett.iverksetting.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.iverksetting.domene.Barn
import no.nav.dagpenger.iverksett.iverksetting.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.iverksetting.domene.Brev
import no.nav.dagpenger.iverksett.iverksetting.domene.Delvilkårsvurdering
import no.nav.dagpenger.iverksett.iverksetting.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.iverksetting.domene.IverksettBarnetilsyn
import no.nav.dagpenger.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.dagpenger.iverksett.iverksetting.domene.IverksettResultat
import no.nav.dagpenger.iverksett.iverksetting.domene.OppdragResultat
import no.nav.dagpenger.iverksett.iverksetting.domene.PeriodeMedBeløp
import no.nav.dagpenger.iverksett.iverksetting.domene.Søker
import no.nav.dagpenger.iverksett.iverksetting.domene.TilbakekrevingMedVarsel
import no.nav.dagpenger.iverksett.iverksetting.domene.TilbakekrevingResultat
import no.nav.dagpenger.iverksett.iverksetting.domene.Tilbakekrevingsdetaljer
import no.nav.dagpenger.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.iverksetting.domene.TilkjentYtelseMedMetaData
import no.nav.dagpenger.iverksett.iverksetting.domene.VedtaksdetaljerBarnetilsyn
import no.nav.dagpenger.iverksett.iverksetting.domene.VedtaksdetaljerOvergangsstønad
import no.nav.dagpenger.iverksett.iverksetting.domene.VedtaksperiodeBarnetilsyn
import no.nav.dagpenger.iverksett.iverksetting.domene.VedtaksperiodeOvergangsstønad
import no.nav.dagpenger.iverksett.iverksetting.domene.Vilkårsvurdering
import no.nav.dagpenger.iverksett.iverksetting.domene.Vurdering
import no.nav.dagpenger.iverksett.iverksetting.domene.ÅrsakRevurdering
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingType
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.Datoperiode
import no.nav.dagpenger.iverksett.kontrakter.felles.FrittståendeBrevDto
import no.nav.dagpenger.iverksett.kontrakter.felles.FrittståendeBrevType
import no.nav.dagpenger.iverksett.kontrakter.felles.Månedsperiode
import no.nav.dagpenger.iverksett.kontrakter.felles.OpphørÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.Opplysningskilde
import no.nav.dagpenger.iverksett.kontrakter.felles.RegelId
import no.nav.dagpenger.iverksett.kontrakter.felles.Revurderingsårsak
import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import no.nav.dagpenger.iverksett.kontrakter.felles.SvarId
import no.nav.dagpenger.iverksett.kontrakter.felles.TilkjentYtelseStatus
import no.nav.dagpenger.iverksett.kontrakter.felles.Vedtaksresultat
import no.nav.dagpenger.iverksett.kontrakter.felles.VilkårType
import no.nav.dagpenger.iverksett.kontrakter.felles.Vilkårsresultat
import no.nav.dagpenger.iverksett.kontrakter.iverksett.AdressebeskyttelseGradering
import no.nav.dagpenger.iverksett.kontrakter.iverksett.AktivitetType
import no.nav.dagpenger.iverksett.kontrakter.iverksett.BehandlingsdetaljerDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.DelvilkårsvurderingDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.FagsakdetaljerDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.IverksettOvergangsstønadDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.SøkerDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.TilkjentYtelseDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VedtaksdetaljerOvergangsstønadDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VedtaksperiodeType
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VilkårsvurderingDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VurderingDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.ÅrsakRevurderingDto
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Tilbakekrevingsvalg
import no.nav.dagpenger.iverksett.økonomi.lagAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.økonomi.lagAndelTilkjentYtelseDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.Random
import java.util.UUID

fun opprettIverksettDto(
    behandlingId: UUID,
    behandlingÅrsak: BehandlingÅrsak = BehandlingÅrsak.SØKNAD,
    andelsbeløp: Int = 5000,
    stønadType: StønadType = StønadType.OVERGANGSSTØNAD,
): IverksettOvergangsstønadDto {
    val andelTilkjentYtelse = lagAndelTilkjentYtelseDto(
        beløp = andelsbeløp,
        fraOgMed = LocalDate.of(2021, 1, 1),
        tilOgMed = LocalDate.of(2021, 12, 31),
        kildeBehandlingId = UUID.randomUUID(),
    )
    val tilkjentYtelse = TilkjentYtelseDto(
        andelerTilkjentYtelse = listOf(andelTilkjentYtelse),
        startdato = andelTilkjentYtelse.periode.fomDato,
    )

    return IverksettOvergangsstønadDto(
        fagsak = FagsakdetaljerDto(fagsakId = UUID.randomUUID(), eksternId = 1L, stønadstype = stønadType),
        behandling = BehandlingsdetaljerDto(
            behandlingId = behandlingId,
            forrigeBehandlingId = null,
            eksternId = 9L,
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            behandlingÅrsak = behandlingÅrsak,
            vilkårsvurderinger = listOf(
                VilkårsvurderingDto(
                    vilkårType = VilkårType.SAGT_OPP_ELLER_REDUSERT,
                    resultat = Vilkårsresultat.OPPFYLT,
                    delvilkårsvurderinger = listOf(
                        DelvilkårsvurderingDto(
                            resultat = Vilkårsresultat.OPPFYLT,
                            vurderinger = listOf(
                                VurderingDto(
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
            årsakRevurdering = ÅrsakRevurderingDto(Opplysningskilde.MELDING_MODIA, Revurderingsårsak.ENDRING_INNTEKT),
        ),
        søker = SøkerDto(
            personIdent = "12345678910",
            barn = emptyList(),
            tilhørendeEnhet = "4489",
            adressebeskyttelse = AdressebeskyttelseGradering.UGRADERT,
        ),
        vedtak = VedtaksdetaljerOvergangsstønadDto(
            resultat = Vedtaksresultat.INNVILGET,
            vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
            opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
            saksbehandlerId = "A12345",
            beslutterId = "B23456",
            tilkjentYtelse = tilkjentYtelse,
            vedtaksperioder = emptyList(),
        ),
    )
}

fun opprettAndelTilkjentYtelse(
    beløp: Int = 5000,
    fra: YearMonth = YearMonth.of(2021, 1),
    til: YearMonth = YearMonth.of(2021, 12),
) = lagAndelTilkjentYtelse(
    beløp = beløp,
    fraOgMed = fra,
    tilOgMed = til,
    inntekt = 100,
    samordningsfradrag = 2,
    inntektsreduksjon = 5,
)

private val eksternIdGenerator = Random()

fun opprettTilkjentYtelseMedMetadata(
    behandlingId: UUID = UUID.randomUUID(),
    eksternId: Long = eksternIdGenerator.nextLong(10_000),
    tilkjentYtelse: TilkjentYtelse = opprettTilkjentYtelse(behandlingId),
): TilkjentYtelseMedMetaData {
    return TilkjentYtelseMedMetaData(
        tilkjentYtelse = tilkjentYtelse,
        saksbehandlerId = "saksbehandlerId",
        eksternBehandlingId = eksternId,
        stønadstype = StønadType.OVERGANGSSTØNAD,
        eksternFagsakId = 0,
        personIdent = "12345678910",
        behandlingId = behandlingId,
        vedtaksdato = LocalDate.of(2021, 1, 1),
    )
}

fun opprettTilkjentYtelse(
    behandlingId: UUID = UUID.randomUUID(),
    andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
    startmåned: YearMonth = startmåned(andeler),
    sisteAndelIKjede: AndelTilkjentYtelse? = null,
): TilkjentYtelse {
    return TilkjentYtelse(
        id = behandlingId,
        utbetalingsoppdrag = null,
        andelerTilkjentYtelse = andeler,
        startmåned = startmåned,
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
        eksternId = 9L,
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

fun vedtaksperioderOvergangsstønad() =
    VedtaksperiodeOvergangsstønad(
        periode = lagMånedsperiode(YearMonth.now()),
        aktivitet = AktivitetType.BARNET_ER_SYKT,
        periodeType = VedtaksperiodeType.HOVEDPERIODE,
    )

fun vedtaksperioderBarnetilsyn() =
    VedtaksperiodeBarnetilsyn(
        periode = lagMånedsperiode(YearMonth.now()),
        utgifter = 1,
        antallBarn = 10,
    )

fun vedtaksdetaljerOvergangsstønad(
    vedtaksresultat: Vedtaksresultat = Vedtaksresultat.INNVILGET,
    andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
    tilbakekreving: Tilbakekrevingsdetaljer? = null,
    startdato: YearMonth = startmåned(andeler),
    vedtaksperioder: List<VedtaksperiodeOvergangsstønad> = listOf(vedtaksperioderOvergangsstønad()),
): VedtaksdetaljerOvergangsstønad {
    val tilkjentYtelse = lagTilkjentYtelse(andeler, startdato)
    return VedtaksdetaljerOvergangsstønad(
        vedtaksresultat = vedtaksresultat,
        vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
        opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
        saksbehandlerId = "A12345",
        beslutterId = "B23456",
        tilkjentYtelse = tilkjentYtelse,
        vedtaksperioder = vedtaksperioder,
        tilbakekreving = tilbakekreving,
        brevmottakere = Brevmottakere(emptyList()),
    )
}

fun vedtaksdetaljerBarnetilsyn(
    vedtaksresultat: Vedtaksresultat = Vedtaksresultat.INNVILGET,
    andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
    tilbakekreving: Tilbakekrevingsdetaljer? = null,
    startdato: YearMonth = startmåned(andeler),
    vedtaksperioder: List<VedtaksperiodeBarnetilsyn> = listOf(vedtaksperioderBarnetilsyn()),
): VedtaksdetaljerBarnetilsyn {
    val tilkjentYtelse = lagTilkjentYtelse(andeler, startdato)
    return VedtaksdetaljerBarnetilsyn(
        vedtaksresultat = vedtaksresultat,
        vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
        opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
        saksbehandlerId = "A12345",
        beslutterId = "B23456",
        tilkjentYtelse = tilkjentYtelse,
        vedtaksperioder = vedtaksperioder,
        tilbakekreving = tilbakekreving,
        brevmottakere = Brevmottakere(emptyList()),
        kontantstøtte = listOf(
            PeriodeMedBeløp(
                periode = Månedsperiode(YearMonth.of(2022, 1), YearMonth.of(2022, 3)),
                beløp = 10,
            ),
        ),
        tilleggsstønad = listOf(PeriodeMedBeløp(periode = Månedsperiode(YearMonth.of(2022, 2), YearMonth.of(2022, 3)), beløp = 5)),
    )
}

private fun lagTilkjentYtelse(
    andeler: List<AndelTilkjentYtelse>,
    startmåned: YearMonth,
): TilkjentYtelse =
    TilkjentYtelse(
        id = UUID.randomUUID(),
        utbetalingsoppdrag = null,
        status = TilkjentYtelseStatus.AKTIV,
        andelerTilkjentYtelse = andeler,
        startmåned = startmåned,
    )

fun opprettIverksettBarnetilsyn(
    behandlingsdetaljer: Behandlingsdetaljer = behandlingsdetaljer(),
    vedtaksdetaljer: VedtaksdetaljerBarnetilsyn = vedtaksdetaljerBarnetilsyn(),
) =
    IverksettBarnetilsyn(
        fagsak = Fagsakdetaljer(fagsakId = UUID.randomUUID(), eksternId = 1L, stønadstype = StønadType.OVERGANGSSTØNAD),
        behandling = behandlingsdetaljer,
        søker = Søker(
            personIdent = "12345678910",
            barn = emptyList(),
            tilhørendeEnhet = "4489",
            adressebeskyttelse = AdressebeskyttelseGradering.UGRADERT,
        ),
        vedtak = vedtaksdetaljer,
    )

fun opprettIverksettOvergangsstønad(
    behandlingsdetaljer: Behandlingsdetaljer = behandlingsdetaljer(),
    vedtaksdetaljer: VedtaksdetaljerOvergangsstønad = vedtaksdetaljerOvergangsstønad(),
) =
    IverksettOvergangsstønad(
        fagsak = Fagsakdetaljer(fagsakId = UUID.randomUUID(), eksternId = 1L, stønadstype = StønadType.OVERGANGSSTØNAD),
        behandling = behandlingsdetaljer,
        søker = Søker(
            personIdent = "12345678910",
            barn = emptyList(),
            tilhørendeEnhet = "4489",
            adressebeskyttelse = AdressebeskyttelseGradering.UGRADERT,
        ),
        vedtak = vedtaksdetaljer,
    )

fun opprettIverksettOvergangsstønad(
    behandlingId: UUID = UUID.randomUUID(),
    forrigeBehandlingId: UUID? = null,
    andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
    tilbakekreving: Tilbakekrevingsdetaljer? = null,
    startmåned: YearMonth = startmåned(andeler),
): IverksettOvergangsstønad {
    val behandlingType = forrigeBehandlingId?.let { BehandlingType.REVURDERING } ?: BehandlingType.FØRSTEGANGSBEHANDLING
    return IverksettOvergangsstønad(
        fagsak = Fagsakdetaljer(fagsakId = UUID.randomUUID(), eksternId = 1L, stønadstype = StønadType.OVERGANGSSTØNAD),
        behandling = behandlingsdetaljer(behandlingId, forrigeBehandlingId, behandlingType),
        søker = Søker(
            personIdent = "12345678910",
            barn = listOf(Barn("01010199999"), Barn(null, LocalDate.of(2023, 1, 1))),
            tilhørendeEnhet = "4489",
            adressebeskyttelse = AdressebeskyttelseGradering.UGRADERT,
        ),
        vedtak = vedtaksdetaljerOvergangsstønad(Vedtaksresultat.INNVILGET, andeler, tilbakekreving, startmåned),
    )
}

fun startmåned(andeler: List<AndelTilkjentYtelse>) =
    andeler.minOfOrNull { it.periode.fom } ?: error("Trenger å sette startdato hvs det ikke finnes andeler")

fun opprettBrev(): Brev {
    return Brev(ByteArray(256))
}

fun opprettFrittståendeBrevDto(): FrittståendeBrevDto {
    return FrittståendeBrevDto(
        personIdent = "12345678910",
        eksternFagsakId = 1,
        brevtype = FrittståendeBrevType.INFORMASJONSBREV,
        fil = "fil.pdf".toByteArray(),
        journalførendeEnhet = "4489",
        saksbehandlerIdent = "saksbehandlerIdent",
        stønadType = StønadType.OVERGANGSSTØNAD,
    )
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
    val oppdragResultat: OppdragResultat,
    val journalpostResultat: Map<String, JournalpostResultat>,
    val vedtaksbrevResultat: Map<String, DistribuerBrevResultat>,
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
                        this.journalpostResultat!!.entries.first().value.journalpostId to DistribuerBrevResultat(
                            bestillingId = behandlingId.toString(),
                        ),
                    )
            }

        fun tilbakekrevingResultat(tilbakekrevingResultat: TilbakekrevingResultat?) =
            apply { this.tilbakekrevingResultat = tilbakekrevingResultat }

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

fun lagMånedsperiode(måned: YearMonth) = Månedsperiode(måned, måned)
