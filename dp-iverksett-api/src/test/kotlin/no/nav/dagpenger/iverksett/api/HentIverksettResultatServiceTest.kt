package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.api.domene.OppdragResultat
import no.nav.dagpenger.iverksett.api.domene.TilbakekrevingResultat
import no.nav.dagpenger.iverksett.kontrakter.iverksett.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.konsumenter.tilbakekreving.tilOpprettTilbakekrevingRequest
import no.nav.dagpenger.iverksett.kontrakter.felles.Enhet
import no.nav.dagpenger.iverksett.kontrakter.oppdrag.OppdragStatus
import no.nav.dagpenger.iverksett.util.IverksettResultatMockBuilder
import no.nav.dagpenger.iverksett.util.opprettIverksettDagpenger
import no.nav.dagpenger.iverksett.util.opprettTilkjentYtelse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

internal class HentIverksettResultatServiceTest : ServerTest() {

    @Autowired
    private lateinit var iverksettResultatService: IverksettResultatService

    private val behandlingId: UUID = UUID.randomUUID()
    private val tilkjentYtelse: TilkjentYtelse = opprettTilkjentYtelse(behandlingId)

    @BeforeEach
    fun beforeEach() {
        iverksettResultatService.opprettTomtResultat(behandlingId)
        iverksettResultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)
    }

    @Test
    fun `hent ekisterende tilkjent ytelse, forvent likhet og ingen unntak`() {
        val hentetTilkjentYtelse = iverksettResultatService.hentTilkjentYtelse(behandlingId)
        assertThat(hentetTilkjentYtelse).isEqualTo(tilkjentYtelse)
    }

    @Test
    fun `hent ikke-eksisterende tilstand, forvent nullverdi i retur og ingen unntak`() {
        val hentetTilkjentYtelse = iverksettResultatService.hentTilkjentYtelse(UUID.randomUUID())
        assertThat(hentetTilkjentYtelse).isEqualTo(null)
    }

    @Test
    fun `hent ekisterende journalpost resultat, forvent likhet og ingen unntak`() {
        val journalpostResultat =
            IverksettResultatMockBuilder.Builder().journalPostResultat().build(behandlingId, tilkjentYtelse).journalpostResultat

        val (mottakerIdent, resultat) = journalpostResultat.map.entries.first()
        iverksettResultatService.oppdaterJournalpostResultat(behandlingId, mottakerIdent, resultat)

        val hentetJournalpostResultat = iverksettResultatService.hentJournalpostResultat(behandlingId)
        assertThat(hentetJournalpostResultat).isEqualTo(journalpostResultat.map)
    }

    @Test
    fun `hent ikke-eksisterende journalpost resultat, forvent nullverdi i retur og ingen unntak`() {
        val hentetJournalpostResultat = iverksettResultatService.hentJournalpostResultat(UUID.randomUUID())
        assertThat(hentetJournalpostResultat).isEqualTo(null)
    }

    @Test
    fun `lagre tilkjentYtelse, hent IverksettResultat med riktig behandlingsID`() {
        val resultat = IverksettResultatMockBuilder.Builder()
            .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_OK))
            .journalPostResultat()
            .vedtaksbrevResultat(behandlingId).build(behandlingId, tilkjentYtelse)
        iverksettResultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)
        iverksettResultatService.oppdaterOppdragResultat(behandlingId, resultat.oppdragResultat!!)
        val (mottakerIdent, journalpostresultat) = resultat.journalpostResultat.map.entries.first()
        iverksettResultatService.oppdaterJournalpostResultat(behandlingId, mottakerIdent, journalpostresultat)
        val (journalpostId, vedtaksbrevResultat) = resultat.vedtaksbrevResultat.map.entries.first()

        iverksettResultatService.oppdaterDistribuerVedtaksbrevResultat(behandlingId, journalpostId, vedtaksbrevResultat)
        val iverksettResultat = iverksettResultatService.hentIverksettResultat(behandlingId)
        assertThat(iverksettResultat).isEqualTo(resultat)
    }

    @Test
    internal fun `hent tilkjentytelser for flere oppdragIdn`() {
        val behandlingId2 = UUID.randomUUID()
        iverksettResultatService.opprettTomtResultat(behandlingId2)

        iverksettResultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)
        iverksettResultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId2, tilkjentYtelse)

        val tilkjentYtelsePåBehandlingId = iverksettResultatService.hentTilkjentYtelse(setOf(behandlingId, behandlingId2))

        assertThat(tilkjentYtelsePåBehandlingId).containsKeys(behandlingId, behandlingId2)
    }

    @Test
    internal fun `hentTilkjentYtelse for flere oppdragIdn kaster feil hvis den ikke finner tilkjent ytelse for en behandling`() {
        val behandlingId2 = UUID.randomUUID()
        iverksettResultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)

        assertThat(catchThrowable { iverksettResultatService.hentTilkjentYtelse(setOf(behandlingId, behandlingId2)) })
            .hasMessageContaining("=[$behandlingId2]")
    }

    @Test
    fun `lagre tilbakekrevingsresultat, hent IverksettResultat med tilbakekrevingsresultat`() {
        val iverksett = opprettIverksettDagpenger(behandlingId)
        val opprettTilbakekrevingRequest = iverksett
            .tilOpprettTilbakekrevingRequest(Enhet("1", "Enhet"))

        val tilbakekrevingResultat = TilbakekrevingResultat(opprettTilbakekrevingRequest)

        iverksettResultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId, iverksett.vedtak.tilkjentYtelse!!)
        iverksettResultatService.oppdaterTilbakekrevingResultat(behandlingId, tilbakekrevingResultat)

        val hentetTilbakekrevingResultat = iverksettResultatService.hentTilbakekrevingResultat(behandlingId)
        assertThat(hentetTilbakekrevingResultat!!).isEqualTo(tilbakekrevingResultat)

        val iverksettResultat = iverksettResultatService.hentIverksettResultat(behandlingId)
        assertThat(iverksettResultat!!.tilkjentYtelseForUtbetaling).isNotNull
        assertThat(iverksettResultat.tilbakekrevingResultat).isEqualTo(tilbakekrevingResultat)
    }

    @Test
    fun `overskriv tomt (null) tilbakekrevingsresultat`() {
        val id = UUID.randomUUID()
        val iverksett = opprettIverksettDagpenger(id)
        val opprettTilbakekrevingRequest = iverksett
            .tilOpprettTilbakekrevingRequest(Enhet("1", "Enhet"))

        val tilbakekrevingResultat = TilbakekrevingResultat(opprettTilbakekrevingRequest)

        assertThat(iverksettResultatService.hentIverksettResultat(id)).isNull()
        iverksettResultatService.opprettTomtResultat(id)
        assertThat(iverksettResultatService.hentIverksettResultat(id)!!.tilbakekrevingResultat).isNull()

        iverksettResultatService.oppdaterTilbakekrevingResultat(id, tilbakekrevingResultat)
        assertThat(iverksettResultatService.hentTilbakekrevingResultat(id)!!)
            .isEqualTo(tilbakekrevingResultat)
    }
}
