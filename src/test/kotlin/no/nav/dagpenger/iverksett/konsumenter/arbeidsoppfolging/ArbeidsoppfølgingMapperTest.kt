package no.nav.dagpenger.iverksett.konsumenter.arbeidsoppfolging

import no.nav.dagpenger.iverksett.util.opprettIverksettDagpenger
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.iverksett.arbeidsoppfølging.Aktivitetstype
import no.nav.dagpenger.kontrakter.iverksett.arbeidsoppfølging.Barn
import no.nav.dagpenger.kontrakter.iverksett.arbeidsoppfølging.Periode
import no.nav.dagpenger.kontrakter.iverksett.arbeidsoppfølging.Periodetype
import no.nav.dagpenger.kontrakter.iverksett.arbeidsoppfølging.Vedtaksresultat
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class ArbeidsoppfølgingMapperTest {

    @Test
    fun mapTilVedtakDagpengerTilArbeidsoppfølging() {
        val iverksett = opprettIverksettDagpenger(UUID.randomUUID())

        val vedtakTilArbeidsoppfølging = ArbeidsoppfølgingMapper.mapTilVedtakDagpengerTilArbeidsoppfølging(iverksett)

        assertThat(vedtakTilArbeidsoppfølging.behanlingId).isEqualTo(iverksett.behandling.behandlingId)
        assertThat(vedtakTilArbeidsoppfølging.stønadstype).isEqualTo(StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER)
        assertThat(vedtakTilArbeidsoppfølging.personIdent).isEqualTo(iverksett.søker.personIdent)
        assertThat(vedtakTilArbeidsoppfølging.vedtaksresultat).isEqualTo(Vedtaksresultat.INNVILGET)
        assertThat(vedtakTilArbeidsoppfølging.personIdent).isEqualTo(iverksett.søker.personIdent)

        val forventetPeriode = Periode(
            YearMonth.now().atDay(1),
            YearMonth.now().atEndOfMonth(),
            Periodetype.HOVEDPERIODE,
            Aktivitetstype.BARNET_ER_SYKT,
        )

        assertThat(vedtakTilArbeidsoppfølging.periode).isEqualTo(listOf(forventetPeriode))

        val forventetBarnList = listOf(Barn("01010199999"), Barn(null, LocalDate.of(2023, 1, 1)))
        assertThat(vedtakTilArbeidsoppfølging.barn).isEqualTo(listOf(forventetBarnList))
    }
}
