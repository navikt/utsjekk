package no.nav.dagpenger.iverksett.konsumenter.tilbakekreving

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.iverksett.api.IverksettingRepository
import no.nav.dagpenger.iverksett.infrastruktur.FamilieIntegrasjonerClient
import no.nav.dagpenger.iverksett.kontrakter.felles.Enhet
import no.nav.dagpenger.iverksett.kontrakter.felles.Språkkode
import no.nav.dagpenger.iverksett.kontrakter.objectMapper
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Faktainfo
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.HentFagsystemsbehandling
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.HentFagsystemsbehandlingRespons
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Tilbakekrevingsvalg
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Ytelsestype
import no.nav.dagpenger.iverksett.lagIverksett
import no.nav.dagpenger.iverksett.util.opprettIverksettDagpenger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

internal class TilbakekrevingListenerTest {

    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val familieIntegrasjonerClient = mockk<FamilieIntegrasjonerClient>()
    private val tilbakekrevingProducer = mockk<TilbakekrevingProducer>()

    private lateinit var listener: TilbakekrevingListener
    private val behandling = opprettIverksettDagpenger()

    @BeforeEach
    internal fun setUp() {
        every { iverksettingRepository.findById(any()) }
            .returns(Optional.of(lagIverksett(behandling)))
        every { familieIntegrasjonerClient.hentBehandlendeEnhetForBehandling(any()) }
            .returns(Enhet(enhetId = "0", enhetNavn = "navn"))
        every { tilbakekrevingProducer.send(any(), any()) } just runs
        listener = TilbakekrevingListener(iverksettingRepository, familieIntegrasjonerClient, tilbakekrevingProducer)
    }

    @Test
    internal fun `send kafkamelding til listener med sp-type, forvent kall til kafkaproducer`() {
        listener.listen(record(Ytelsestype.DAGPENGER_ARBEIDSSOKER_ORDINAER))
        verify(exactly = 1) { tilbakekrevingProducer.send(any(), any()) }
    }

    @Test
    internal fun `kafkamelding med fagsakID forskjellig fra iverksatt fagsakID, forvent feilmelding om inkonsistens`() {
        val respons = slot<HentFagsystemsbehandlingRespons>()
        every { tilbakekrevingProducer.send(capture(respons), any()) } just runs
        every { iverksettingRepository.findById(any()) }
            .returns(
                Optional.of(
                    lagIverksett(
                        behandling.copy(fagsak = behandling.fagsak.copy(fagsakId = UUID.randomUUID())),
                    ),
                ),
            )
        listener.listen(record(Ytelsestype.DAGPENGER_ARBEIDSSOKER_ORDINAER))
        assertThat(respons.captured.feilMelding!!).contains("Inkonsistens. FagsakID")
    }

    private fun record(ytelsestype: Ytelsestype): ConsumerRecord<String, String> {
        val behandling = objectMapper.writeValueAsString(
            HentFagsystemsbehandling(
                fagsakId = UUID.randomUUID(),
                behandlingId = UUID.randomUUID(),
                ytelsestype = ytelsestype,
                personIdent = "12345678910",
                språkkode = Språkkode.NB,
                enhetId = "enhet",
                enhetsnavn = "enhetNavn",
                revurderingsvedtaksdato = LocalDate.EPOCH,
                faktainfo = Faktainfo(
                    revurderingsårsak = "årsak",
                    revurderingsresultat = "resultat",
                    tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_UTEN_VARSEL,
                    konsekvensForYtelser = emptySet(),
                ),
            ),
        )
        return ConsumerRecord("topic", 0, 0L, "1", behandling)
    }
}
