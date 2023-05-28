package no.nav.dagpenger.iverksett.konsumenter.tilbakekreving

import no.nav.dagpenger.iverksett.api.domene.TilbakekrevingMedVarsel
import no.nav.dagpenger.iverksett.api.domene.Tilbakekrevingsdetaljer
import no.nav.dagpenger.iverksett.util.opprettIverksettDagpenger
import no.nav.dagpenger.iverksett.util.opprettTilbakekrevingsdetaljer
import no.nav.dagpenger.kontrakter.felles.Enhet
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.Språkkode
import no.nav.dagpenger.kontrakter.felles.Tilbakekrevingsvalg
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.dagpenger.kontrakter.iverksett.tilbakekreving.Behandlingstype
import no.nav.dagpenger.kontrakter.iverksett.tilbakekreving.Ytelsestype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

internal class TilbakekrevingMapperTest {

    @Test
    fun `konverter Iverksetting til OpprettTilbakekrevingRequest`() {
        val behandlingsId = UUID.randomUUID()
        val tilbakekreving = opprettTilbakekrevingsdetaljer()
        val iverksett = opprettIverksettDagpenger(behandlingsId, tilbakekreving = tilbakekreving)
        val enhet = Enhet("123", "enhet")
        val forventetRevurderingssårsak = "Søknad"

        val request = iverksett.tilOpprettTilbakekrevingRequest(enhet)

        assertThat(request.fagsystem).isEqualTo(Fagsystem.Dagpenger)
        assertThat(request.eksternFagsakId).isEqualTo(iverksett.fagsak.fagsakId.toString())
        assertThat(request.eksternId).isEqualTo(iverksett.behandling.behandlingId.toString())
        assertThat(request.ytelsestype).isEqualTo(Ytelsestype.DAGPENGER_ARBEIDSSOKER_ORDINAER)

        assertThat(request.enhetId).isEqualTo(enhet.enhetId)
        assertThat(request.enhetsnavn).isEqualTo(ENHETSNAVN_BREV)

        assertThat(request.manueltOpprettet).isFalse
        assertThat(request.personIdent).isEqualTo(iverksett.søker.personIdent)
        assertThat(request.behandlingstype).isEqualTo(Behandlingstype.TILBAKEKREVING)

        assertThat(request.faktainfo.tilbakekrevingsvalg).isEqualTo(iverksett.vedtak.tilbakekreving!!.tilbakekrevingsvalg)
        assertThat(request.faktainfo.revurderingsresultat).isEqualTo(iverksett.vedtak.vedtaksresultat.visningsnavn)
        assertThat(request.faktainfo.revurderingsårsak).isEqualTo(forventetRevurderingssårsak)
        assertThat(request.faktainfo.konsekvensForYtelser).isEmpty()

        assertThat(request.revurderingsvedtaksdato).isEqualTo(iverksett.vedtak.vedtakstidspunkt.toLocalDate())
        assertThat(request.saksbehandlerIdent).isEqualTo(iverksett.vedtak.saksbehandlerId)

        assertThat(request.språkkode).isEqualTo(Språkkode.NB)
        assertThat(request.varsel?.varseltekst).isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingMedVarsel?.varseltekst)
        assertThat(request.varsel?.sumFeilutbetaling)
            .isEqualTo(iverksett.vedtak.tilbakekreving?.tilbakekrevingMedVarsel?.sumFeilutbetaling)
        assertThat(objectMapper.writeValueAsString(request.varsel?.perioder))
            .isEqualTo(objectMapper.writeValueAsString(iverksett.vedtak.tilbakekreving?.tilbakekrevingMedVarsel?.perioder))
    }

    @Test
    fun `konverter Iverksetting til Hentfagsystembehandling`() {
        val behandlingsId = UUID.randomUUID()
        val iverksett = opprettIverksettDagpenger(behandlingsId)
        val enhet = Enhet(enhetId = "enhetId", enhetNavn = "enhetNavn")
        val fagsystemsbehandling = iverksett.tilFagsystembehandling(enhet = enhet).hentFagsystemsbehandling!!

        assertThat(fagsystemsbehandling.behandlingId).isEqualTo(iverksett.behandling.behandlingId)
        assertThat(fagsystemsbehandling.fagsakId).isEqualTo(iverksett.fagsak.fagsakId)
        assertThat(fagsystemsbehandling.ytelsestype.name).isEqualTo(iverksett.fagsak.stønadstype.name)
        assertThat(fagsystemsbehandling.revurderingsvedtaksdato).isEqualTo(iverksett.vedtak.vedtakstidspunkt.toLocalDate())
        assertThat(fagsystemsbehandling.personIdent).isEqualTo(iverksett.søker.personIdent)
        assertThat(fagsystemsbehandling.språkkode).isEqualTo(Språkkode.NB)
        assertThat(fagsystemsbehandling.verge).isEqualTo(null)

        assertThat(fagsystemsbehandling.enhetId).isEqualTo(enhet.enhetId)
        assertThat(fagsystemsbehandling.enhetsnavn).isEqualTo(enhet.enhetNavn)
    }

    @Test
    fun `skal validere at tilbakekreving med varsel ikker gyldig uten varseltekst`() {
        val tilbakekreving = Tilbakekrevingsdetaljer(
            tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
            tilbakekrevingMedVarsel = null,
        )
        assertThat(tilbakekreving.validerTilbakekreving()).isFalse
    }

    @Test
    fun `skal validere at tilbakekreving med varsel ikke er gyldig uten sumFeilutbetaling`() {
        val tilbakekreving = Tilbakekrevingsdetaljer(
            tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
            tilbakekrevingMedVarsel = TilbakekrevingMedVarsel(
                varseltekst = "",
                perioder = emptyList(),
                sumFeilutbetaling = null,
            ),
        )
        assertThat(tilbakekreving.validerTilbakekreving()).isFalse
    }

    @Test
    fun `skal validere at tilbakekreving med varsel ikke er gyldig uten perioder`() {
        val tilbakekreving = Tilbakekrevingsdetaljer(
            tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
            tilbakekrevingMedVarsel = TilbakekrevingMedVarsel(
                varseltekst = "",
                perioder = null,
                sumFeilutbetaling = BigDecimal.ZERO,
            ),
        )
        assertThat(tilbakekreving.validerTilbakekreving()).isFalse
    }

    @Test
    fun `skal validere at tilbakekreving uten varsel ignorerer manglende varsel-data`() {
        assertThat(
            Tilbakekrevingsdetaljer(
                tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_UTEN_VARSEL,
                tilbakekrevingMedVarsel = null,
            ).validerTilbakekreving(),
        ).isTrue
        assertThat(
            Tilbakekrevingsdetaljer(
                tilbakekrevingsvalg = Tilbakekrevingsvalg.IGNORER_TILBAKEKREVING,
                tilbakekrevingMedVarsel = null,
            ).validerTilbakekreving(),
        ).isTrue
    }

    @Test
    fun `skal validere at tilbakekreving uten varsel ignorerer feil i varsel-data`() {
        val tilbakekrevingsdetaljer = Tilbakekrevingsdetaljer(
            tilbakekrevingMedVarsel = TilbakekrevingMedVarsel(
                varseltekst = "",
                sumFeilutbetaling = null,
                perioder = null,
            ),
            tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
        )

        assertThat(
            tilbakekrevingsdetaljer
                .copy(tilbakekrevingsvalg = Tilbakekrevingsvalg.IGNORER_TILBAKEKREVING)
                .validerTilbakekreving(),
        ).isTrue
        assertThat(
            tilbakekrevingsdetaljer
                .copy(tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_UTEN_VARSEL)
                .validerTilbakekreving(),
        ).isTrue
    }
}
