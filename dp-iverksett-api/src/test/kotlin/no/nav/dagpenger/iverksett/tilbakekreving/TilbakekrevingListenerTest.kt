package no.nav.dagpenger.iverksett.tilbakekreving

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.iverksett.felles.FamilieIntegrasjonerClient
import no.nav.dagpenger.iverksett.iverksetting.IverksettingRepository
import no.nav.dagpenger.iverksett.kontrakter.felles.Enhet
import no.nav.dagpenger.iverksett.kontrakter.felles.Språkkode
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Faktainfo
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.HentFagsystemsbehandling
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.HentFagsystemsbehandlingRespons
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Tilbakekrevingsvalg
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Ytelsestype
import no.nav.dagpenger.iverksett.lagIverksett
import no.nav.dagpenger.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.familie.kontrakter.felles.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class TilbakekrevingListenerTest {

    private val iverksettingRepository = mockk<IverksettingRepository>()
    private val familieIntegrasjonerClient = mockk<FamilieIntegrasjonerClient>()
    private val tilbakekrevingProducer = mockk<TilbakekrevingProducer>()

    private lateinit var listener: TilbakekrevingListener
    private val behandling = opprettIverksettOvergangsstønad()

    @BeforeEach
    internal fun setUp() {
        every { iverksettingRepository.findByEksternId(any()) }
            .returns(lagIverksett(behandling))
        every { familieIntegrasjonerClient.hentBehandlendeEnhetForBehandling(any()) }
            .returns(Enhet(enhetId = "0", enhetNavn = "navn"))
        every { tilbakekrevingProducer.send(any(), any()) } just runs
        listener = TilbakekrevingListener(iverksettingRepository, familieIntegrasjonerClient, tilbakekrevingProducer)
    }

    @Test
    internal fun `send kafkamelding til listener med ef-type, forvent kall til kafkaproducer`() {
        listener.listen(record(Ytelsestype.OVERGANGSSTØNAD))
        listener.listen(record(Ytelsestype.SKOLEPENGER))
        listener.listen(record(Ytelsestype.BARNETILSYN))
        verify(exactly = 3) { tilbakekrevingProducer.send(any(), any()) }
    }

    @Test
    internal fun `send kafkamelding til listener med annen type enn overgangsstønad, forvent ingen kall til kafkaproducer`() {
        listener.listen(record(Ytelsestype.KONTANTSTØTTE))
        listener.listen(record(Ytelsestype.BARNETRYGD))
        verify(exactly = 0) { tilbakekrevingProducer.send(any(), any()) }
    }

    @Test
    internal fun `kafkamelding med fagsakID forskjellig fra iverksatt fagsakID, forvent feilmelding om inkonsistens`() {
        val respons = slot<HentFagsystemsbehandlingRespons>()
        every { tilbakekrevingProducer.send(capture(respons), any()) } just runs
        every { iverksettingRepository.findByEksternId(any()) }
            .returns(lagIverksett(behandling.copy(fagsak = behandling.fagsak.copy(eksternId = 11L))))
        listener.listen(record(Ytelsestype.OVERGANGSSTØNAD))
        assertThat(respons.captured.feilMelding!!).contains("Inkonsistens. Ekstern fagsakID")
    }

    private fun record(ytelsestype: Ytelsestype, eksternFagsakId: String = "0"): ConsumerRecord<String, String> {
        val behandling = objectMapper.writeValueAsString(
            HentFagsystemsbehandling(
                eksternFagsakId = "0",
                eksternId = "1",
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
