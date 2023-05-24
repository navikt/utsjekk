package no.nav.dagpenger.iverksett.util

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
import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagAndelTilkjentYtelseDto
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingType
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.Datoperiode
import no.nav.dagpenger.iverksett.kontrakter.felles.FrittståendeBrevDto
import no.nav.dagpenger.iverksett.kontrakter.felles.FrittståendeBrevType
import no.nav.dagpenger.iverksett.kontrakter.felles.OpphørÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.Opplysningskilde
import no.nav.dagpenger.iverksett.kontrakter.felles.RegelId
import no.nav.dagpenger.iverksett.kontrakter.felles.Revurderingsårsak
import no.nav.dagpenger.iverksett.kontrakter.felles.SvarId
import no.nav.dagpenger.iverksett.kontrakter.felles.TilkjentYtelseStatus
import no.nav.dagpenger.iverksett.kontrakter.felles.VedtakType
import no.nav.dagpenger.iverksett.kontrakter.felles.Vedtaksresultat
import no.nav.dagpenger.iverksett.kontrakter.felles.VilkårType
import no.nav.dagpenger.iverksett.kontrakter.felles.Vilkårsresultat
import no.nav.dagpenger.iverksett.kontrakter.iverksett.AdressebeskyttelseGradering
import no.nav.dagpenger.iverksett.kontrakter.iverksett.AktivitetType
import no.nav.dagpenger.iverksett.kontrakter.iverksett.BehandlingsdetaljerDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.DelvilkårsvurderingDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.FagsakdetaljerDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.IverksettDagpengerdDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.SøkerDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.TilkjentYtelseDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VedtaksdetaljerDagpengerDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VedtaksperiodeType
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VilkårsvurderingDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VurderingDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.ÅrsakRevurderingDto
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Tilbakekrevingsvalg
import no.nav.dagpenger.kontrakter.utbetaling.StønadType
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
    stønadType: StønadType = StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER,
): IverksettDagpengerdDto {
    val andelTilkjentYtelse = lagAndelTilkjentYtelseDto(
        beløp = andelsbeløp,
        fraOgMed = LocalDate.of(2021, 1, 1),
        tilOgMed = LocalDate.of(2021, 12, 31),
        kildeBehandlingId = UUID.randomUUID(),
    )
    val tilkjentYtelse = TilkjentYtelseDto(
        utbetalinger = listOf(andelTilkjentYtelse),
        startdato = andelTilkjentYtelse.fraOgMedDato
            ?: andelTilkjentYtelse.periode?.fom
            ?: throw IllegalStateException("Verken fraOgMedDato eller periode har verdi. En av dem, helst fraOgMedDato, må være satt"),
    )

    return IverksettDagpengerdDto(
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
        personIdent = "12345678910",
        søker = SøkerDto(
            personIdent = "12345678910",
            barn = emptyList(),
            tilhørendeEnhet = "4489",
            adressebeskyttelse = AdressebeskyttelseGradering.UGRADERT,
        ),
        vedtak = VedtaksdetaljerDagpengerDto(
            resultat = Vedtaksresultat.INNVILGET,
            vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
            opphørÅrsak = OpphørÅrsak.PERIODE_UTLØPT,
            saksbehandlerId = "A12345",
            beslutterId = "B23456",
            utbetalinger = tilkjentYtelse.utbetalinger,
            vedtaksperioder = emptyList(),
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
        aktivitet = AktivitetType.BARNET_ER_SYKT,
        periodeType = VedtaksperiodeType.HOVEDPERIODE,
    )

fun vedtaksdetaljerDagpenger(
    vedtaksresultat: Vedtaksresultat = Vedtaksresultat.INNVILGET,
    andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
    tilbakekreving: Tilbakekrevingsdetaljer? = null,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.of(2021, 5, 12, 0, 0),
    startdato: LocalDate = startdato(andeler),
    vedtaksperioder: List<VedtaksperiodeDagpenger> = listOf(vedtaksperioderDagpenger()),
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
        brevmottakere = Brevmottakere(emptyList()),
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
            adressebeskyttelse = AdressebeskyttelseGradering.UGRADERT,
        ),
        vedtak = vedtaksdetaljer,
    )

fun opprettIverksettDagpenger(
    behandlingId: UUID = UUID.randomUUID(),
    forrigeBehandlingId: UUID? = null,
    andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
    tilbakekreving: Tilbakekrevingsdetaljer? = null,
    startdato: LocalDate = startdato(andeler),
    forrigeVedtak: VedtaksdetaljerDagpenger? = null,
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
            adressebeskyttelse = AdressebeskyttelseGradering.UGRADERT,
        ),
        vedtak = vedtaksdetaljerDagpenger(
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            andeler = andeler,
            tilbakekreving = tilbakekreving,
            startdato = startdato,
        ),
        forrigeVedtak = forrigeVedtak,
    )
}

fun startdato(andeler: List<AndelTilkjentYtelse>) =
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
        stønadType = StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER,
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
