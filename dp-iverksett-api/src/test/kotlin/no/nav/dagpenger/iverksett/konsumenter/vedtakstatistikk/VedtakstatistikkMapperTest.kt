package no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk

import no.nav.dagpenger.iverksett.api.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.Barn
import no.nav.dagpenger.iverksett.api.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.Delvilkårsvurdering
import no.nav.dagpenger.iverksett.api.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.api.domene.IverksettOvergangsstønad
import no.nav.dagpenger.iverksett.api.domene.Søker
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerOvergangsstønad
import no.nav.dagpenger.iverksett.api.domene.VedtaksperiodeOvergangsstønad
import no.nav.dagpenger.iverksett.api.domene.Vilkårsvurdering
import no.nav.dagpenger.iverksett.api.domene.Vurdering
import no.nav.dagpenger.iverksett.api.domene.ÅrsakRevurdering
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottakere
import no.nav.dagpenger.iverksett.kontrakter.dvh.Vedtak
import no.nav.dagpenger.iverksett.kontrakter.felles.AvslagÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingType
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.Månedsperiode
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
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VedtaksperiodeType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID

internal class VedtakstatistikkMapperTest {

    private val fagsakId: UUID = UUID.randomUUID()
    private val behandlingId: UUID = UUID.randomUUID()
    private val forrigeBehandlingEksternId = 11L
    private val eksternFagsakId = 12L
    private val eksternBehandlingId = 13L
    private val vedtakstidspunkt = LocalDateTime.now()
    private val søker = "01010172272"
    private val barnFnr = "24101576627"
    private val termindato: LocalDate? = LocalDate.now().plusDays(40)

    @Test
    internal fun `skal mappe iverksett til VedtakOvergangsstønadDVH - sjekk alle felter`() {
        val vedtakOvergangsstønadDVH = VedtakstatistikkMapper.mapTilVedtakOvergangsstønadDVH(
            iverksettOvergangsstønad(),
            forrigeBehandlingEksternId,
        )
        assertThat(vedtakOvergangsstønadDVH.aktivitetskrav.harSagtOppArbeidsforhold).isFalse()
        assertThat(vedtakOvergangsstønadDVH.aktivitetskrav.aktivitetspliktInntrefferDato).isNull()
        assertThat(vedtakOvergangsstønadDVH.barn).hasSize(2)
        assertThat(vedtakOvergangsstønadDVH.barn.first().personIdent).isEqualTo(barnFnr)
        assertThat(vedtakOvergangsstønadDVH.barn.first().termindato).isEqualTo(termindato)
        assertThat(vedtakOvergangsstønadDVH.behandlingId).isEqualTo(eksternBehandlingId)
        assertThat(vedtakOvergangsstønadDVH.behandlingType.name).isEqualTo(BehandlingType.REVURDERING.name)
        assertThat(vedtakOvergangsstønadDVH.behandlingÅrsak.name).isEqualTo(BehandlingÅrsak.SØKNAD.name)
        assertThat(vedtakOvergangsstønadDVH.fagsakId).isEqualTo(eksternFagsakId)
        assertThat(vedtakOvergangsstønadDVH.funksjonellId).isEqualTo(eksternBehandlingId)
        assertThat(vedtakOvergangsstønadDVH.person.personIdent).isEqualTo(søker)
        assertThat(vedtakOvergangsstønadDVH.relatertBehandlingId).isEqualTo(forrigeBehandlingEksternId)
        assertThat(vedtakOvergangsstønadDVH.stønadstype.name).isEqualTo(StønadType.OVERGANGSSTØNAD.name)
        assertThat(vedtakOvergangsstønadDVH.tidspunktVedtak).isEqualTo(vedtakstidspunkt.atZone(ZoneId.of("Europe/Oslo")))
        assertThat(vedtakOvergangsstønadDVH.utbetalinger).hasSize(2)
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().fraOgMed).isEqualTo(LocalDate.of(2021, 1, 1))
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().tilOgMed).isEqualTo(LocalDate.of(2021, 5, 31))
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().inntekt).isEqualTo(300000)
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().inntektsreduksjon).isEqualTo(11000)
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().samordningsfradrag).isEqualTo(1000)
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().beløp).isEqualTo(9000)
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().utbetalingsdetalj.delytelseId).isEqualTo("121")
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().utbetalingsdetalj.klassekode).isEqualTo("EFOG")
        assertThat(vedtakOvergangsstønadDVH.utbetalinger.first().utbetalingsdetalj.gjelderPerson.personIdent).isEqualTo(søker)
        assertThat(vedtakOvergangsstønadDVH.vedtak).isEqualTo(Vedtak.INNVILGET)
        assertThat(vedtakOvergangsstønadDVH.vedtaksperioder).hasSize(2)
        assertThat(vedtakOvergangsstønadDVH.vedtaksperioder.first().fraOgMed).isEqualTo(LocalDate.of(2021, 2, 1))
        assertThat(vedtakOvergangsstønadDVH.vedtaksperioder.first().tilOgMed).isEqualTo(LocalDate.of(2021, 3, 31))
        assertThat(vedtakOvergangsstønadDVH.vedtaksperioder.first().aktivitet.name).isEqualTo(AktivitetType.IKKE_AKTIVITETSPLIKT.name)
        assertThat(vedtakOvergangsstønadDVH.vedtaksperioder.first().periodeType.name).isEqualTo(VedtaksperiodeType.PERIODE_FØR_FØDSEL.name)
        assertThat(vedtakOvergangsstønadDVH.vilkårsvurderinger).hasSize(12)
        assertThat(vedtakOvergangsstønadDVH.vilkårsvurderinger.first().vilkår.name).isEqualTo(VilkårType.FORUTGÅENDE_MEDLEMSKAP.name)
        assertThat(vedtakOvergangsstønadDVH.vilkårsvurderinger.first().resultat.name).isEqualTo(Vilkårsresultat.OPPFYLT.name)

        assertThat(vedtakOvergangsstønadDVH.kravMottatt).isEqualTo(LocalDate.of(2021, 3, 1))
        assertThat(vedtakOvergangsstønadDVH.årsakRevurdering?.opplysningskilde).isEqualTo(Opplysningskilde.MELDING_MODIA.name)
        assertThat(vedtakOvergangsstønadDVH.årsakRevurdering?.årsak).isEqualTo(Revurderingsårsak.ENDRING_INNTEKT.name)
    }

    @Test
    internal fun `skal mappe iverksett med avslagsårsak`() {
        val vedtakOvergangsstønadDVH = VedtakstatistikkMapper.mapTilVedtakOvergangsstønadDVH(
            iverksettOvergangsstønad().copy(
                vedtak = vedtaksdetaljerOvergangsstønad(
                    Vedtaksresultat.AVSLÅTT,
                    AvslagÅrsak.MINDRE_INNTEKTSENDRINGER,
                ),
            ),
            forrigeBehandlingEksternId,
        )
        assertThat(vedtakOvergangsstønadDVH.vedtak).isEqualTo(Vedtak.AVSLÅTT)
        assertThat(vedtakOvergangsstønadDVH.avslagÅrsak).isEqualTo("MINDRE_INNTEKTSENDRINGER")
    }

    private fun iverksettOvergangsstønad() = IverksettOvergangsstønad(
        fagsak = fagsakdetaljer(),
        behandling = behandlingsdetaljer(),
        søker = Søker(
            personIdent = søker,
            barn = listOf(
                Barn(personIdent = barnFnr, termindato = termindato),
                Barn(termindato = termindato),
            ),
            tilhørendeEnhet = "4489",
            adressebeskyttelse = AdressebeskyttelseGradering.STRENGT_FORTROLIG,
        ),
        vedtak = vedtaksdetaljerOvergangsstønad(),
    )

    fun fagsakdetaljer(stønadstype: StønadType = StønadType.OVERGANGSSTØNAD): Fagsakdetaljer =
        Fagsakdetaljer(
            fagsakId = fagsakId,
            eksternId = eksternFagsakId,
            stønadstype = stønadstype,
        )

    fun behandlingsdetaljer(): Behandlingsdetaljer =
        Behandlingsdetaljer(
            forrigeBehandlingId = null,
            behandlingId = behandlingId,
            eksternId = eksternBehandlingId,
            behandlingType = BehandlingType.REVURDERING,
            behandlingÅrsak = BehandlingÅrsak.SØKNAD,
            relatertBehandlingId = null,
            vilkårsvurderinger = lagVilkårsvurderinger(),
            aktivitetspliktInntrefferDato = null,
            kravMottatt = LocalDate.of(2021, 3, 1),
            årsakRevurdering = ÅrsakRevurdering(Opplysningskilde.MELDING_MODIA, Revurderingsårsak.ENDRING_INNTEKT),
        )

    fun tilkjentYtelse(): TilkjentYtelse =
        TilkjentYtelse(
            id = UUID.randomUUID(),
            utbetalingsoppdrag = null,
            status = TilkjentYtelseStatus.OPPRETTET,
            andelerTilkjentYtelse = listOf(
                AndelTilkjentYtelse(
                    beløp = 9000,
                    periode = Månedsperiode(YearMonth.of(2021, 1), YearMonth.of(2021, 5)),
                    inntekt = 300000,
                    samordningsfradrag = 1000,
                    inntektsreduksjon = 11000,
                    periodeId = 1,
                    forrigePeriodeId = null,
                    kildeBehandlingId = behandlingId,
                ),
                AndelTilkjentYtelse(
                    beløp = 10000,
                    periode = Månedsperiode(YearMonth.of(2021, 6), YearMonth.of(2021, 10)),
                    inntekt = 300000,
                    samordningsfradrag = 0,
                    inntektsreduksjon = 11000,
                    periodeId = 2,
                    forrigePeriodeId = 1,
                    kildeBehandlingId = behandlingId,
                ),
            ),
            startmåned = YearMonth.now(),
        )

    fun vedtaksdetaljerOvergangsstønad(
        resultat: Vedtaksresultat = Vedtaksresultat.INNVILGET,
        avslagÅrsak: AvslagÅrsak? = null,
    ): VedtaksdetaljerOvergangsstønad {
        return VedtaksdetaljerOvergangsstønad(
            vedtaksresultat = resultat,
            avslagÅrsak = avslagÅrsak,
            vedtakstidspunkt = vedtakstidspunkt,
            opphørÅrsak = null,
            saksbehandlerId = "A123456",
            beslutterId = "B123456",
            tilkjentYtelse = tilkjentYtelse(),
            vedtaksperioder = listOf(
                VedtaksperiodeOvergangsstønad(
                    aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT,
                    periode = Månedsperiode(YearMonth.of(2021, 2), YearMonth.of(2021, 3)),
                    periodeType = VedtaksperiodeType.PERIODE_FØR_FØDSEL,
                ),
                VedtaksperiodeOvergangsstønad(
                    aktivitet = AktivitetType.FORSØRGER_I_ARBEID,
                    periode = Månedsperiode(YearMonth.of(2021, 6), YearMonth.of(2021, 10)),
                    periodeType = VedtaksperiodeType.HOVEDPERIODE,
                ),
            ),
            brevmottakere = Brevmottakere(emptyList()),
        )
    }

    private fun lagVilkårsvurderinger(): List<Vilkårsvurdering> = listOf(
        Vilkårsvurdering(
            vilkårType = VilkårType.FORUTGÅENDE_MEDLEMSKAP,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            RegelId.SØKER_MEDLEM_I_FOLKETRYGDEN,
                            SvarId.JA,
                            null,
                        ),
                    ),
                ),
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            RegelId.MEDLEMSKAP_UNNTAK,
                            SvarId.NEI,
                            null,
                        ),
                    ),
                ),
            ),
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.LOVLIG_OPPHOLD,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            RegelId.BOR_OG_OPPHOLDER_SEG_I_NORGE,
                            SvarId.JA,
                            null,
                        ),
                    ),
                ),
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            RegelId.OPPHOLD_UNNTAK,
                            SvarId.NEI,
                            null,
                        ),
                    ),
                ),
            ),
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.MOR_ELLER_FAR,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.OMSORG_FOR_EGNE_ELLER_ADOPTERTE_BARN,
                            SvarId.JA,
                            null,
                        ),
                    ),
                ),
            ),
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.SIVILSTAND,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.KRAV_SIVILSTAND_UTEN_PÅKREVD_BEGRUNNELSE,
                            SvarId.JA,
                            null,
                        ),
                    ),
                ),
            ),
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.SAMLIV,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.LEVER_IKKE_I_EKTESKAPLIGNENDE_FORHOLD,
                            SvarId.JA,
                            null,
                        ),
                    ),
                ),
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.LEVER_IKKE_MED_ANNEN_FORELDER,
                            SvarId.JA,
                            null,
                        ),
                    ),
                ),
            ),
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.ALENEOMSORG,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            RegelId.SKRIFTLIG_AVTALE_OM_DELT_BOSTED,
                            SvarId.NEI,
                            null,
                        ),
                    ),
                ),
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            regelId = RegelId.NÆRE_BOFORHOLD,
                            svar = SvarId.NEI,
                            begrunnelse = null,
                        ),
                    ),
                ),
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            regelId = RegelId.MER_AV_DAGLIG_OMSORG,
                            svar = SvarId.JA,
                            begrunnelse = null,
                        ),
                    ),
                ),
            ),
        ),
        Vilkårsvurdering(
            vilkårType = VilkårType.ALENEOMSORG,
            resultat = Vilkårsresultat.SKAL_IKKE_VURDERES,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.SKAL_IKKE_VURDERES,
                    vurderinger = listOf(),
                ),
            ),
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.NYTT_BARN_SAMME_PARTNER,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.HAR_FÅTT_ELLER_VENTER_NYTT_BARN_MED_SAMME_PARTNER,
                            SvarId.NEI,
                            null,
                        ),
                    ),
                ),
            ),
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.SAGT_OPP_ELLER_REDUSERT,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            regelId = RegelId.SAGT_OPP_ELLER_REDUSERT,
                            svar = SvarId.NEI,
                            begrunnelse = null,
                        ),
                    ),
                ),
            ),
        ),

        Vilkårsvurdering(
            vilkårType = VilkårType.AKTIVITET,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    resultat = Vilkårsresultat.OPPFYLT,
                    vurderinger = listOf(
                        Vurdering(
                            RegelId.FYLLER_BRUKER_AKTIVITETSPLIKT,
                            SvarId.JA,
                            null,
                        ),
                    ),
                ),
            ),
        ),
        Vilkårsvurdering(
            vilkårType = VilkårType.TIDLIGERE_VEDTAKSPERIODER,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.HAR_TIDLIGERE_ANDRE_STØNADER_SOM_HAR_BETYDNING,
                            SvarId.NEI,
                            null,
                        ),
                    ),
                ),
                Delvilkårsvurdering(
                    Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.HAR_TIDLIGERE_MOTTATT_OVERGANSSTØNAD,
                            SvarId.NEI,
                            null,
                        ),
                    ),
                ),
            ),
        ),
        Vilkårsvurdering(
            vilkårType = VilkårType.AKTIVITET_ARBEID,
            resultat = Vilkårsresultat.OPPFYLT,
            delvilkårsvurderinger = listOf(
                Delvilkårsvurdering(
                    Vilkårsresultat.OPPFYLT,
                    listOf(
                        Vurdering(
                            RegelId.ER_I_ARBEID_ELLER_FORBIGÅENDE_SYKDOM,
                            SvarId.ER_I_ARBEID,
                            null,
                        ),
                    ),
                ),
            ),
        ),
    )
}
