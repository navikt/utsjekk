package no.nav.dagpenger.iverksett.konsumenter.tilbakekreving

import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.kontrakter.felles.Språkkode
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Behandling
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.FeilutbetaltePerioderDto
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.ForhåndsvisVarselbrevRequest
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Periode
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Ytelsestype
import no.nav.dagpenger.kontrakter.utbetaling.Fagsystem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.UUID

internal class TilbakekrevingClientTest : ServerTest() {

    @Autowired
    private lateinit var tilbakekrevingClient: TilbakekrevingClient

    @Test
    fun `hentForhåndsvisningVarselbrev returnere en byteArray med data fra server`() {
        val forhåndsvisVarselbrevRequest =
            ForhåndsvisVarselbrevRequest(
                ytelsestype = Ytelsestype.DAGPENGER_ARBEIDSSOKER_ORDINAER,
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
                fagsystem = Fagsystem.Dagpenger,
                ident = "32165498721",
                eksternFagsakId = "654654",
            )
        val hentForhåndsvisningVarselbrev = tilbakekrevingClient.hentForhåndsvisningVarselbrev(forhåndsvisVarselbrevRequest)

        assertThat(hentForhåndsvisningVarselbrev.decodeToString()).isEqualTo("Dette er en PDF!")
    }

    @Test
    fun `finnesÅpenBehandling returnerer true hvis server retrurnerer transportobjekt med true`() {
        val finnesÅpenBehandling = tilbakekrevingClient.finnesÅpenBehandling(UUID.randomUUID())

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
            tilbakekrevingClient.kanBehandlingOpprettesManuelt(1L, Ytelsestype.DAGPENGER_ARBEIDSSOKER_ORDINAER)

        assertThat(kanBehandlingOpprettesManuelt.kanBehandlingOpprettes).isEqualTo(true)
    }
}
