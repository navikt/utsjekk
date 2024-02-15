package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.util.enTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.util.etIverksettingsresultat
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomString
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.felles.somUUID
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
    private val sakId = GeneriskIdSomUUID(UUID.randomUUID())
    private val tilkjentYtelse: TilkjentYtelse = enTilkjentYtelse(behandlingId)

    @BeforeEach
    fun beforeEach() {
        iverksettingsresultatService.opprettTomtResultat(Fagsystem.DAGPENGER, sakId, behandlingId)
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
            Fagsystem.DAGPENGER,
            sakId,
            behandlingId,
            tilkjentYtelse,
        )
    }

    @Test
    fun `hent ekisterende tilkjent ytelse, forvent likhet og ingen unntak`() {
        val hentetTilkjentYtelse =
            iverksettingsresultatService.hentTilkjentYtelse(Fagsystem.DAGPENGER, sakId, behandlingId)
        assertEquals(tilkjentYtelse, hentetTilkjentYtelse)
    }

    @Test
    fun `hent ikke-eksisterende tilstand, forvent nullverdi i retur og ingen unntak`() {
        val hentetTilkjentYtelse =
            iverksettingsresultatService.hentTilkjentYtelse(Fagsystem.DAGPENGER, sakId, UUID.randomUUID())
        assertNull(hentetTilkjentYtelse)
    }

    @Test
    fun `hent korrekt iverksettingsresultat for behandling med to iverksettinger`() {
        val behandlingId = UUID.randomUUID()
        val iverksettingId1 = "IVERK-1"
        val iverksettingId2 = "IVERK-2"
        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId,
            behandlingId = behandlingId,
            iverksettingId = iverksettingId1,
        )
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId,
            behandlingId = behandlingId,
            tilkjentYtelseForUtbetaling = tilkjentYtelse,
            iverksettingId = iverksettingId1,
        )
        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId,
            behandlingId = behandlingId,
            iverksettingId = iverksettingId2,
        )

        val iverksetting2 =
            iverksettingsresultatService.hentIverksettResultat(
                fagsystem = Fagsystem.TILLEGGSSTØNADER,
                sakId = sakId,
                behandlingId = behandlingId,
                iverksettingId = iverksettingId2,
            )

        assertNotNull(iverksetting2)
        assertNull(iverksetting2?.tilkjentYtelseForUtbetaling)
        assertEquals(iverksettingId2, iverksetting2?.iverksettingId)
    }

    @Test
    fun `hent korrekt iverksettingsresultat for flere fagsystem med samme behandlingId`() {
        val behandlingId = UUID.randomUUID()
        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId,
            behandlingId = behandlingId,
        )
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId,
            behandlingId = behandlingId,
            tilkjentYtelseForUtbetaling = tilkjentYtelse,
        )
        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = Fagsystem.TILTAKSPENGER,
            sakId = sakId,
            behandlingId = behandlingId,
        )

        val iverksettingTilleggsstønader =
            iverksettingsresultatService.hentIverksettResultat(
                fagsystem = Fagsystem.TILLEGGSSTØNADER,
                sakId = sakId,
                behandlingId = behandlingId,
            )

        assertNotNull(iverksettingTilleggsstønader)
        assertNotNull(iverksettingTilleggsstønader?.tilkjentYtelseForUtbetaling)
        assertEquals(Fagsystem.TILLEGGSSTØNADER, iverksettingTilleggsstønader?.fagsystem)
    }

    @Test
    fun `hent korrekt iverksettingsresultat for flere fagsaker med samme behandlingId`() {
        val sakId1 = GeneriskIdSomString("1")
        val sakId2 = GeneriskIdSomString("2")
        val behandlingId = GeneriskIdSomString("1")
        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId1,
            behandlingId = behandlingId.somUUID,
        )
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId1,
            behandlingId = behandlingId.somUUID,
            tilkjentYtelseForUtbetaling = tilkjentYtelse,
        )
        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId2,
            behandlingId = behandlingId.somUUID,
        )

        val iverksettingSak1 =
            iverksettingsresultatService.hentIverksettResultat(
                fagsystem = Fagsystem.TILLEGGSSTØNADER,
                sakId = sakId1,
                behandlingId = behandlingId.somUUID,
            )

        assertNotNull(iverksettingSak1)
        assertNotNull(iverksettingSak1?.tilkjentYtelseForUtbetaling)
        assertEquals(sakId1, iverksettingSak1?.sakId)
    }

    @Test
    fun `lagre tilkjentYtelse, hent IverksettResultat med riktig behandlingId`() {
        val resultat =
            etIverksettingsresultat(
                fagsystem = Fagsystem.DAGPENGER,
                sakId = sakId,
                behandlingId = behandlingId,
                tilkjentYtelse = tilkjentYtelse,
                oppdragResultat = OppdragResultat(OppdragStatus.KVITTERT_OK),
            )

        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
            fagsystem = Fagsystem.DAGPENGER,
            sakId = sakId,
            behandlingId = behandlingId,
            tilkjentYtelseForUtbetaling = tilkjentYtelse,
        )
        iverksettingsresultatService.oppdaterOppdragResultat(
            fagsystem = Fagsystem.DAGPENGER,
            sakId = sakId,
            behandlingId = behandlingId,
            oppdragResultat = resultat.oppdragResultat!!,
            iverksettingId = null,
        )

        val iverksettResultat =
            iverksettingsresultatService.hentIverksettResultat(Fagsystem.DAGPENGER, sakId, behandlingId)
        assertEquals(resultat, iverksettResultat)
    }
}
