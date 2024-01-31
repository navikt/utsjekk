package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.util.IverksettResultatMockBuilder
import no.nav.dagpenger.iverksett.utbetaling.util.opprettTilkjentYtelse
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

internal class HentIverksettingResultatServiceTest : ServerTest() {
    @Autowired
    private lateinit var iverksettingsresultatService: IverksettingsresultatService

    private val behandlingId: UUID = UUID.randomUUID()
    private val tilkjentYtelse: TilkjentYtelse = opprettTilkjentYtelse(behandlingId)

    @BeforeEach
    fun beforeEach() {
        iverksettingsresultatService.opprettTomtResultat(Fagsystem.DAGPENGER, behandlingId)
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(Fagsystem.DAGPENGER, behandlingId, tilkjentYtelse)
    }

    @Test
    fun `hent ekisterende tilkjent ytelse, forvent likhet og ingen unntak`() {
        val hentetTilkjentYtelse = iverksettingsresultatService.hentTilkjentYtelse(Fagsystem.DAGPENGER, behandlingId)
        assertEquals(tilkjentYtelse, hentetTilkjentYtelse)
    }

    @Test
    fun `hent ikke-eksisterende tilstand, forvent nullverdi i retur og ingen unntak`() {
        val hentetTilkjentYtelse = iverksettingsresultatService.hentTilkjentYtelse(Fagsystem.DAGPENGER, UUID.randomUUID())
        assertNull(hentetTilkjentYtelse)
    }

    @Test
    fun `hent korrekt iverksettingsresultat for behandling med to iverksettinger`() {
        val behandlingId = UUID.randomUUID()
        val iverksettingId1 = "IVERK-1"
        val iverksettingId2 = "IVERK-2"
        iverksettingsresultatService.opprettTomtResultat(Fagsystem.TILLEGGSSTØNADER, behandlingId, iverksettingId1)
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
            Fagsystem.TILLEGGSSTØNADER,
            behandlingId,
            tilkjentYtelse,
            iverksettingId1,
        )
        iverksettingsresultatService.opprettTomtResultat(Fagsystem.TILLEGGSSTØNADER, behandlingId, iverksettingId2)

        val iverksetting2 = iverksettingsresultatService.hentIverksettResultat(Fagsystem.TILLEGGSSTØNADER, behandlingId, iverksettingId2)

        assertNotNull(iverksetting2)
        assertNull(iverksetting2?.tilkjentYtelseForUtbetaling)
        assertEquals(iverksettingId2, iverksetting2?.iverksettingId)
    }

    @Test
    fun `hent korrekt iverksettingsresultat for flere fagsystem med samme behandlingId`() {
        val behandlingId = UUID.randomUUID()
        iverksettingsresultatService.opprettTomtResultat(Fagsystem.TILLEGGSSTØNADER, behandlingId)
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
            Fagsystem.TILLEGGSSTØNADER,
            behandlingId,
            tilkjentYtelse,
        )
        iverksettingsresultatService.opprettTomtResultat(Fagsystem.TILTAKSPENGER, behandlingId)

        val iverksetting2 = iverksettingsresultatService.hentIverksettResultat(Fagsystem.TILLEGGSSTØNADER, behandlingId)

        assertNotNull(iverksetting2)
        assertNotNull(iverksetting2?.tilkjentYtelseForUtbetaling)
        assertEquals(Fagsystem.TILLEGGSSTØNADER, iverksetting2?.fagsystem)
    }

    @Test
    fun `lagre tilkjentYtelse, hent IverksettResultat med riktig behandlingsID`() {
        val resultat =
            IverksettResultatMockBuilder.Builder()
                .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_OK))
                .build(Fagsystem.DAGPENGER, behandlingId, tilkjentYtelse)
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(Fagsystem.DAGPENGER, behandlingId, tilkjentYtelse)
        iverksettingsresultatService.oppdaterOppdragResultat(Fagsystem.DAGPENGER, behandlingId, resultat.oppdragResultat!!)

        val iverksettResultat = iverksettingsresultatService.hentIverksettResultat(Fagsystem.DAGPENGER, behandlingId)
        assertEquals(resultat, iverksettResultat)
    }
}
