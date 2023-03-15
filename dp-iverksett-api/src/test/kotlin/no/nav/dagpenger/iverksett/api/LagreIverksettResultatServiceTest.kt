package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.api.domene.OppdragResultat
import no.nav.dagpenger.iverksett.api.domene.TilbakekrevingResultat
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.DistribuerBrevResultat
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.JournalpostResultat
import no.nav.dagpenger.iverksett.konsumenter.tilbakekreving.tilOpprettTilbakekrevingRequest
import no.nav.dagpenger.iverksett.kontrakter.felles.Enhet
import no.nav.dagpenger.iverksett.kontrakter.oppdrag.OppdragStatus
import no.nav.dagpenger.iverksett.util.opprettIverksettOvergangsstønad
import no.nav.dagpenger.iverksett.util.opprettTilkjentYtelse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

internal class LagreIverksettResultatServiceTest : ServerTest() {

    @Autowired
    private lateinit var tilstandRepositoryService: IverksettResultatService

    private val behandlingsId: UUID = UUID.randomUUID()
    private val journalpostId: UUID = UUID.randomUUID()

    private val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)

    @BeforeEach
    fun beforeEach() {
        tilstandRepositoryService.opprettTomtResultat(behandlingsId)
    }

    @Test
    fun `oppdater tilkjent ytelse, forvent ingen unntak`() {
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        tilstandRepositoryService.oppdaterTilkjentYtelseForUtbetaling(behandlingsId, tilkjentYtelse)
    }

    @Test
    fun `oppdater oppdrag, forvent ingen unntak`() {
        val oppdragResultat = OppdragResultat(oppdragStatus = OppdragStatus.KVITTERT_OK)
        tilstandRepositoryService.oppdaterOppdragResultat(behandlingsId, oppdragResultat)
    }

    @Test
    fun `oppdater journalpost, forvent ingen unntak`() {
        tilstandRepositoryService.oppdaterJournalpostResultat(
            behandlingsId,
            "123",
            JournalpostResultat(
                journalpostId = journalpostId.toString(),
            ),
        )
    }

    @Test
    fun `oppdater distribuerVedtaksbrev, forvent ingen unntak`() {
        tilstandRepositoryService.oppdaterDistribuerVedtaksbrevResultat(
            behandlingsId,
            "123",
            DistribuerBrevResultat(bestillingId = "12345"),
        )
    }

    @Test
    fun `oppdater distribuerVedtaksbrev med feil behandlingId, forvent IllegalStateException`() {
        assertThrows<IllegalStateException> {
            tilstandRepositoryService.oppdaterDistribuerVedtaksbrevResultat(
                UUID.randomUUID(),
                "123",
                DistribuerBrevResultat(bestillingId = journalpostId.toString()),
            )
        }
    }

    @Test
    fun `oppdater tilbakekrevingsresultat, forvent ingen unntak`() {
        val opprettTilbakekrevingRequest = opprettIverksettOvergangsstønad(behandlingsId)
            .tilOpprettTilbakekrevingRequest(Enhet("1", "Enhet"))

        tilstandRepositoryService.oppdaterTilbakekrevingResultat(
            behandlingsId,
            TilbakekrevingResultat(opprettTilbakekrevingRequest),
        )
    }
}
