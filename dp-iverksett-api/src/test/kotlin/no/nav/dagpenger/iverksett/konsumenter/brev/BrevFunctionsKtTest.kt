package no.nav.dagpenger.iverksett.konsumenter.brev

import no.nav.dagpenger.iverksett.lagIverksettData
import no.nav.dagpenger.kontrakter.iverksett.felles.BehandlingType.FØRSTEGANGSBEHANDLING
import no.nav.dagpenger.kontrakter.iverksett.felles.BehandlingType.REVURDERING
import no.nav.dagpenger.kontrakter.iverksett.felles.BehandlingÅrsak.NYE_OPPLYSNINGER
import no.nav.dagpenger.kontrakter.iverksett.felles.BehandlingÅrsak.SØKNAD
import no.nav.dagpenger.kontrakter.iverksett.felles.Vedtaksresultat.AVSLÅTT
import no.nav.dagpenger.kontrakter.iverksett.felles.Vedtaksresultat.INNVILGET
import no.nav.dagpenger.kontrakter.iverksett.felles.Vedtaksresultat.OPPHØRT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BrevFunctionsKtTest {

    private val iverksettFørsteGangsbehandlingInnvilget =
        lagIverksettData(behandlingType = FØRSTEGANGSBEHANDLING, vedtaksresultat = INNVILGET)
    private val iverksettFørsteGangsbehandlingAvslått =
        lagIverksettData(behandlingType = FØRSTEGANGSBEHANDLING, vedtaksresultat = AVSLÅTT)
    private val iverksettRevurderingAvslått =
        lagIverksettData(behandlingType = REVURDERING, vedtaksresultat = AVSLÅTT)
    private val iverksettRevurderingInnvilgetMedSøknad =
        lagIverksettData(behandlingType = REVURDERING, vedtaksresultat = INNVILGET, årsak = SØKNAD)
    private val iverksettRevurderingInnvilgetUtenSøknad =
        lagIverksettData(behandlingType = REVURDERING, vedtaksresultat = INNVILGET, årsak = NYE_OPPLYSNINGER)
    private val iverksettRevurderingOpphørt =
        lagIverksettData(behandlingType = REVURDERING, vedtaksresultat = OPPHØRT)

    @Test
    internal fun `skal lage riktig brevtekst for riktig vedtak og behandlingstype`() {
        assertThat(lagVedtakstekst(iverksettFørsteGangsbehandlingInnvilget)).isEqualTo("Vedtak om innvilget ")
        assertThat(lagVedtakstekst(iverksettFørsteGangsbehandlingAvslått)).isEqualTo("Vedtak om avslått ")
        assertThat(lagVedtakstekst(iverksettRevurderingAvslått)).isEqualTo("Vedtak om avslått ")
        assertThat(lagVedtakstekst(iverksettRevurderingInnvilgetMedSøknad)).isEqualTo("Vedtak om innvilget ")
        assertThat(lagVedtakstekst(iverksettRevurderingInnvilgetUtenSøknad)).isEqualTo("Vedtak om revurdert ")
        assertThat(lagVedtakstekst(iverksettRevurderingOpphørt)).isEqualTo("Vedtak om opphørt ")
    }
}
