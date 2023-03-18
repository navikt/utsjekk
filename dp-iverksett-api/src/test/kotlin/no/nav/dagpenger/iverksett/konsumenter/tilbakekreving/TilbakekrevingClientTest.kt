package no.nav.dagpenger.iverksett.konsumenter.tilbakekreving

import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.kontrakter.felles.Fagsystem
import no.nav.dagpenger.iverksett.kontrakter.felles.Språkkode
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Behandling
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.FeilutbetaltePerioderDto
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.ForhåndsvisVarselbrevRequest
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Periode
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Ytelsestype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

internal class TilbakekrevingClientTest : ServerTest() {

    @Autowired
    private lateinit var tilbakekrevingClient: TilbakekrevingClient

    @Test
    fun `hentForhåndsvisningVarselbrev returnere en byteArray med data fra server`() {
        val forhåndsvisVarselbrevRequest =
            ForhåndsvisVarselbrevRequest(
                ytelsestype = Ytelsestype.DAGPENGER,
                behandlendeEnhetsNavn = "Oslo",
                språkkode = Språkkode.NB,
                feilutbetaltePerioderDto = FeilutbetaltePerioderDto(
                    654654L,
                    listOf(
                        Periode(
                            LocalDate.MIN,
                            LocalDate.MAX,
                        ),
                    ),
                ),
                fagsystem = Fagsystem.DP,
                ident = "32165498721",
                eksternFagsakId = "654654",
            )
        val hentForhåndsvisningVarselbrev = tilbakekrevingClient.hentForhåndsvisningVarselbrev(forhåndsvisVarselbrevRequest)

        assertThat(hentForhåndsvisningVarselbrev.decodeToString()).isEqualTo("Dette er en PDF!")
    }

    @Test
    fun `finnesÅpenBehandling returnerer true hvis server retrurnerer transportobjekt med true`() {
        val finnesÅpenBehandling = tilbakekrevingClient.finnesÅpenBehandling(1L)

        assertThat(finnesÅpenBehandling).isEqualTo(true)
    }

    @Test
    fun `finnBehandlinger returnerer enn liste med behandlinger`() {
        val finnBehandlinger = tilbakekrevingClient.finnBehandlinger(1L)

        assertThat(finnBehandlinger).hasSize(1)
        assertThat(finnBehandlinger.first()).isInstanceOf(Behandling::class.java)
    }

    @Test
    fun `kanBehandlingOpprettesManuelt returnere transportobjekt far server`() {
        val kanBehandlingOpprettesManuelt =
            tilbakekrevingClient.kanBehandlingOpprettesManuelt(1L, Ytelsestype.DAGPENGER)

        assertThat(kanBehandlingOpprettesManuelt.kanBehandlingOpprettes).isEqualTo(true)
    }
}
