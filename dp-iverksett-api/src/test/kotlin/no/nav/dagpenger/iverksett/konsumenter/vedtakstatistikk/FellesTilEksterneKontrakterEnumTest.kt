package no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk

import no.nav.dagpenger.iverksett.kontrakter.dvh.Vedtak
import no.nav.dagpenger.iverksett.kontrakter.dvh.Vilkår
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingType
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.Vedtaksresultat
import no.nav.dagpenger.iverksett.kontrakter.felles.VilkårType
import no.nav.dagpenger.iverksett.kontrakter.felles.Vilkårsresultat
import no.nav.dagpenger.iverksett.kontrakter.iverksett.AdressebeskyttelseGradering
import no.nav.dagpenger.iverksett.kontrakter.iverksett.AktivitetType
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VedtaksperiodeType
import no.nav.familie.kontrakter.felles.ef.StønadType
import org.junit.jupiter.api.Test
import no.nav.dagpenger.iverksett.kontrakter.arbeidsoppfølging.Aktivitetstype as AktivitetstypeEkstern
import no.nav.dagpenger.iverksett.kontrakter.arbeidsoppfølging.Periodetype as PeriodetypeEkstern
import no.nav.dagpenger.iverksett.kontrakter.arbeidsoppfølging.Stønadstype as StønadstypeEkstern
import no.nav.dagpenger.iverksett.kontrakter.arbeidsoppfølging.Vedtaksresultat as VedtaksresultatEkstern
import no.nav.dagpenger.iverksett.kontrakter.dvh.Adressebeskyttelse as AdresseBeskyttelseEkstern
import no.nav.dagpenger.iverksett.kontrakter.dvh.AktivitetType as AktivitetTypeEkstern
import no.nav.dagpenger.iverksett.kontrakter.dvh.BehandlingType as BehandlingTypeEkstern
import no.nav.dagpenger.iverksett.kontrakter.dvh.BehandlingÅrsak as BehandlingÅrsakEkstern
import no.nav.dagpenger.iverksett.kontrakter.dvh.StønadType as StønadTypeEkstern
import no.nav.dagpenger.iverksett.kontrakter.dvh.VedtaksperiodeType as VedtakPeriodeTypeEkstern
import no.nav.dagpenger.iverksett.kontrakter.dvh.Vilkårsresultat as VilkårsresultatEkstern

class FellesTilEksterneKontrakterEnumTest {

    @Test
    fun `for alle eksterne kontrakter enums, forvent fullstendig mapping fra familie kontrakter enums`() {
        Vedtaksresultat.values().forEach { Vedtak.valueOf(it.name) }
        BehandlingÅrsak.values().forEach { BehandlingÅrsakEkstern.valueOf(it.name) }
        BehandlingType.values().forEach { BehandlingTypeEkstern.valueOf(it.name) }
        Vilkårsresultat.values().forEach { VilkårsresultatEkstern.valueOf(it.name) }
        VilkårType.values().forEach { Vilkår.valueOf(it.name) }
        VedtaksperiodeType.values().forEach { VedtakPeriodeTypeEkstern.valueOf(it.name) }
        AktivitetType.values().forEach { AktivitetTypeEkstern.valueOf(it.name) }
        AdressebeskyttelseGradering.values().forEach { AdresseBeskyttelseEkstern.valueOf(it.name) }
        StønadType.values().forEach { StønadTypeEkstern.valueOf(it.name) }
    }

    @Test
    fun `for alle arbeidsoppfølging enums i eksterne kontrakter, forvent mapping fra domene`() {
        Vedtaksresultat.values().forEach { VedtaksresultatEkstern.valueOf(it.name) }
        VedtaksperiodeType.values().forEach { PeriodetypeEkstern.valueOf(it.name) }
        AktivitetType.values().forEach { AktivitetstypeEkstern.valueOf(it.name) }
        StønadType.values().forEach { StønadstypeEkstern.valueOf(it.name) }
    }
}
