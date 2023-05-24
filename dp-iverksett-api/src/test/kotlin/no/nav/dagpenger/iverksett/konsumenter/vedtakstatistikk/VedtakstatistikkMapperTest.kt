package no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk

import no.nav.dagpenger.iverksett.api.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.Barn
import no.nav.dagpenger.iverksett.api.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.Delvilkårsvurdering
import no.nav.dagpenger.iverksett.api.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.Søker
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerDagpenger
import no.nav.dagpenger.iverksett.api.domene.VedtaksperiodeDagpenger
import no.nav.dagpenger.iverksett.api.domene.Vilkårsvurdering
import no.nav.dagpenger.iverksett.api.domene.Vurdering
import no.nav.dagpenger.iverksett.api.domene.ÅrsakRevurdering
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottakere
import no.nav.dagpenger.iverksett.kontrakter.dvh.VedtakresultatDVH
import no.nav.dagpenger.iverksett.kontrakter.felles.AvslagÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingType
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.Datoperiode
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
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VedtaksperiodeType
import no.nav.dagpenger.kontrakter.utbetaling.StønadType
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
    private val forrigeBehandlingId = UUID.randomUUID()
    private val vedtakstidspunkt = LocalDateTime.now()
    private val søker = "01010172272"
    private val barnFnr = "24101576627"
    private val termindato: LocalDate? = LocalDate.now().plusDays(40)

    @Test
    internal fun `skal mappe iverksett til VedtakDagpengerDVH - sjekk alle felter`() {
        val vedtakDagpengerDVH = VedtakstatistikkMapper.mapTilVedtakDagpengerDVH(
            iverksettDagpenger(),
            forrigeBehandlingId,
        )
        assertThat(vedtakDagpengerDVH.aktivitetskrav.harSagtOppArbeidsforhold).isFalse()
        assertThat(vedtakDagpengerDVH.aktivitetskrav.aktivitetspliktInntrefferDato).isNull()
        assertThat(vedtakDagpengerDVH.barn).hasSize(2)
        assertThat(vedtakDagpengerDVH.barn.first().personIdent).isEqualTo(barnFnr)
        assertThat(vedtakDagpengerDVH.barn.first().termindato).isEqualTo(termindato)
        assertThat(vedtakDagpengerDVH.behandlingId).isEqualTo(behandlingId)
        assertThat(vedtakDagpengerDVH.behandlingType.name).isEqualTo(BehandlingType.REVURDERING.name)
        assertThat(vedtakDagpengerDVH.behandlingÅrsak.name).isEqualTo(BehandlingÅrsak.SØKNAD.name)
        assertThat(vedtakDagpengerDVH.fagsakId).isEqualTo(fagsakId)
        assertThat(vedtakDagpengerDVH.funksjonellId).isEqualTo(behandlingId)
        assertThat(vedtakDagpengerDVH.person.personIdent).isEqualTo(søker)
        assertThat(vedtakDagpengerDVH.relatertBehandlingId).isEqualTo(forrigeBehandlingId)
        assertThat(vedtakDagpengerDVH.stønadstype.name).isEqualTo("DAGPENGER")
        assertThat(vedtakDagpengerDVH.tidspunktVedtak).isEqualTo(vedtakstidspunkt.atZone(ZoneId.of("Europe/Oslo")))
        assertThat(vedtakDagpengerDVH.utbetalinger).hasSize(2)
        assertThat(vedtakDagpengerDVH.utbetalinger.first().fraOgMed).isEqualTo(LocalDate.of(2021, 1, 1))
        assertThat(vedtakDagpengerDVH.utbetalinger.first().tilOgMed).isEqualTo(LocalDate.of(2021, 5, 31))
        assertThat(vedtakDagpengerDVH.utbetalinger.first().inntekt).isEqualTo(300000)
        assertThat(vedtakDagpengerDVH.utbetalinger.first().inntektsreduksjon).isEqualTo(11000)
        assertThat(vedtakDagpengerDVH.utbetalinger.first().samordningsfradrag).isEqualTo(1000)
        assertThat(vedtakDagpengerDVH.utbetalinger.first().beløp).isEqualTo(9000)
        assertThat(vedtakDagpengerDVH.utbetalinger.first().utbetalingsdetalj.delytelseId).isEqualTo("${fagsakId}1")
        assertThat(vedtakDagpengerDVH.utbetalinger.first().utbetalingsdetalj.klassekode).isEqualTo("DPORAS")
        assertThat(vedtakDagpengerDVH.utbetalinger.first().utbetalingsdetalj.gjelderPerson.personIdent).isEqualTo(søker)
        assertThat(vedtakDagpengerDVH.vedtak).isEqualTo(VedtakresultatDVH.INNVILGET)
        assertThat(vedtakDagpengerDVH.vedtaksperioder).hasSize(2)
        assertThat(vedtakDagpengerDVH.vedtaksperioder.first().fraOgMed).isEqualTo(LocalDate.of(2021, 2, 1))
        assertThat(vedtakDagpengerDVH.vedtaksperioder.first().tilOgMed).isEqualTo(LocalDate.of(2021, 3, 31))
        assertThat(vedtakDagpengerDVH.vedtaksperioder.first().aktivitet.name).isEqualTo(AktivitetType.IKKE_AKTIVITETSPLIKT.name)
        assertThat(vedtakDagpengerDVH.vedtaksperioder.first().periodeType.name).isEqualTo(VedtaksperiodeType.SANKSJON.name)
        assertThat(vedtakDagpengerDVH.vilkårsvurderinger).hasSize(10)
        assertThat(vedtakDagpengerDVH.vilkårsvurderinger.first().vilkår.name).isEqualTo(VilkårType.FORUTGÅENDE_MEDLEMSKAP.name)
        assertThat(vedtakDagpengerDVH.vilkårsvurderinger.first().resultat.name).isEqualTo(Vilkårsresultat.OPPFYLT.name)

        assertThat(vedtakDagpengerDVH.kravMottatt).isEqualTo(LocalDate.of(2021, 3, 1))
        assertThat(vedtakDagpengerDVH.årsakRevurdering?.opplysningskilde).isEqualTo(Opplysningskilde.MELDING_MODIA.name)
        assertThat(vedtakDagpengerDVH.årsakRevurdering?.årsak).isEqualTo(Revurderingsårsak.ENDRING_INNTEKT.name)
    }

    @Test
    internal fun `skal mappe iverksett med avslagsårsak`() {
        val vedtakDagpengerDVH = VedtakstatistikkMapper.mapTilVedtakDagpengerDVH(
            iverksettDagpenger().copy(
                vedtak = vedtaksdetaljerDagpenger(
                    Vedtaksresultat.AVSLÅTT,
                    AvslagÅrsak.MINDRE_INNTEKTSENDRINGER,
                ),
            ),
            forrigeBehandlingId,
        )
        assertThat(vedtakDagpengerDVH.vedtak).isEqualTo(VedtakresultatDVH.AVSLÅTT)
        assertThat(vedtakDagpengerDVH.avslagÅrsak).isEqualTo("MINDRE_INNTEKTSENDRINGER")
    }

    private fun iverksettDagpenger() = IverksettDagpenger(
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
        vedtak = vedtaksdetaljerDagpenger(),
    )

    fun fagsakdetaljer(stønadstype: StønadType = StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER): Fagsakdetaljer =
        Fagsakdetaljer(
            fagsakId = fagsakId,
            stønadstype = stønadstype,
        )

    fun behandlingsdetaljer(): Behandlingsdetaljer =
        Behandlingsdetaljer(
            forrigeBehandlingId = null,
            behandlingId = behandlingId,
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
                    periode = Datoperiode(YearMonth.of(2021, 1), YearMonth.of(2021, 5)),
                    stønadstype = StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER,
                    inntekt = 300000,
                    samordningsfradrag = 1000,
                    inntektsreduksjon = 11000,
                    periodeId = 1,
                    forrigePeriodeId = null,
                    kildeBehandlingId = behandlingId,
                ),
                AndelTilkjentYtelse(
                    beløp = 10000,
                    periode = Datoperiode(YearMonth.of(2021, 6), YearMonth.of(2021, 10)),
                    stønadstype = StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER,
                    inntekt = 300000,
                    samordningsfradrag = 0,
                    inntektsreduksjon = 11000,
                    periodeId = 2,
                    forrigePeriodeId = 1,
                    kildeBehandlingId = behandlingId,
                ),
            ),
            startdato = LocalDate.now(),
        )

    fun vedtaksdetaljerDagpenger(
        resultat: Vedtaksresultat = Vedtaksresultat.INNVILGET,
        avslagÅrsak: AvslagÅrsak? = null,
    ): VedtaksdetaljerDagpenger {
        return VedtaksdetaljerDagpenger(
            vedtakstype = VedtakType.UTBETALINGSVEDTAK,
            vedtaksresultat = resultat,
            avslagÅrsak = avslagÅrsak,
            vedtakstidspunkt = vedtakstidspunkt,
            opphørÅrsak = null,
            saksbehandlerId = "A123456",
            beslutterId = "B123456",
            tilkjentYtelse = tilkjentYtelse(),
            vedtaksperioder = listOf(
                VedtaksperiodeDagpenger(
                    aktivitet = AktivitetType.IKKE_AKTIVITETSPLIKT,
                    periode = Datoperiode(YearMonth.of(2021, 2), YearMonth.of(2021, 3)),
                    periodeType = VedtaksperiodeType.SANKSJON,
                ),
                VedtaksperiodeDagpenger(
                    aktivitet = AktivitetType.FORSØRGER_I_ARBEID,
                    periode = Datoperiode(YearMonth.of(2021, 6), YearMonth.of(2021, 10)),
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
                            RegelId.HAR_TIDLIGERE_MOTTATT_DAGPENGER,
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
