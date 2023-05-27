package no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk

import no.nav.dagpenger.kontrakter.felles.BehandlingType
import no.nav.dagpenger.kontrakter.felles.BehandlingÅrsak
import no.nav.dagpenger.kontrakter.felles.Vedtaksresultat
import no.nav.dagpenger.kontrakter.felles.VilkårType
import no.nav.dagpenger.kontrakter.felles.Vilkårsresultat
import no.nav.dagpenger.kontrakter.iverksett.AdressebeskyttelseGradering
import no.nav.dagpenger.kontrakter.iverksett.AktivitetType
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeType
import no.nav.dagpenger.kontrakter.iverksett.dvh.VedtakresultatDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.VilkårDVH
import org.junit.jupiter.api.Test
import no.nav.dagpenger.kontrakter.iverksett.arbeidsoppfølging.Aktivitetstype as AktivitetstypeEkstern
import no.nav.dagpenger.kontrakter.iverksett.arbeidsoppfølging.Periodetype as PeriodetypeEkstern
import no.nav.dagpenger.kontrakter.iverksett.arbeidsoppfølging.Vedtaksresultat as VedtaksresultatEkstern
import no.nav.dagpenger.kontrakter.iverksett.dvh.AdressebeskyttelseDVH as AdresseBeskyttelseEkstern
import no.nav.dagpenger.kontrakter.iverksett.dvh.AktivitetTypeDVH as AktivitetTypeEkstern
import no.nav.dagpenger.kontrakter.iverksett.dvh.BehandlingTypeDVH as BehandlingTypeEkstern
import no.nav.dagpenger.kontrakter.iverksett.dvh.BehandlingÅrsakDVH as BehandlingÅrsakEkstern
import no.nav.dagpenger.kontrakter.iverksett.dvh.VedtaksperiodeTypeDVH as VedtakPeriodeTypeEkstern
import no.nav.dagpenger.kontrakter.iverksett.dvh.VilkårsresultatDVH as VilkårsresultatEkstern

class FellesTilEksterneKontrakterEnumTest {

    @Test
    fun `for alle eksterne kontrakter enums, forvent fullstendig mapping fra dagpenge-kontrakter-enums`() {
        Vedtaksresultat.values().forEach { VedtakresultatDVH.valueOf(it.name) }
        BehandlingÅrsak.values().forEach { BehandlingÅrsakEkstern.valueOf(it.name) }
        BehandlingType.values().forEach { BehandlingTypeEkstern.valueOf(it.name) }
        Vilkårsresultat.values().forEach { VilkårsresultatEkstern.valueOf(it.name) }
        VilkårType.values().forEach { VilkårDVH.valueOf(it.name) }
        VedtaksperiodeType.values().forEach { VedtakPeriodeTypeEkstern.valueOf(it.name) }
        AktivitetType.values().forEach { AktivitetTypeEkstern.valueOf(it.name) }
        AdressebeskyttelseGradering.values().forEach { AdresseBeskyttelseEkstern.valueOf(it.name) }
    }

    @Test
    fun `for alle arbeidsoppfølging enums i eksterne kontrakter, forvent mapping fra domene`() {
        Vedtaksresultat.values().forEach { VedtaksresultatEkstern.valueOf(it.name) }
        VedtaksperiodeType.values().forEach { PeriodetypeEkstern.valueOf(it.name) }
        AktivitetType.values().forEach { AktivitetstypeEkstern.valueOf(it.name) }
    }
}
