package no.nav.utsjekk.iverksetting.tilstand

import no.nav.utsjekk.Integrasjonstest
import no.nav.utsjekk.iverksetting.domene.OppdragResultat
import no.nav.utsjekk.iverksetting.domene.TilkjentYtelse
import no.nav.utsjekk.iverksetting.domene.transformer.RandomOSURId
import no.nav.utsjekk.iverksetting.util.enTilkjentYtelse
import no.nav.utsjekk.iverksetting.util.etIverksettingsresultat
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.oppdrag.OppdragStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class HentIverksettingResultatServiceTest : Integrasjonstest() {
    @Autowired
    private lateinit var iverksettingsresultatService: IverksettingsresultatService

    private val behandlingId = RandomOSURId.generate()
    private val sakId = RandomOSURId.generate()
    private val tilkjentYtelse: TilkjentYtelse = enTilkjentYtelse(behandlingId)

    @BeforeEach
    fun beforeEach() {
        iverksettingsresultatService.opprettTomtResultat(Fagsystem.DAGPENGER, sakId, behandlingId, null)
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
            Fagsystem.DAGPENGER,
            sakId,
            behandlingId,
            tilkjentYtelse,
            null,
        )
    }

    @Test
    fun `hent ekisterende tilkjent ytelse, forvent likhet og ingen unntak`() {
        val hentetTilkjentYtelse =
            iverksettingsresultatService.hentTilkjentYtelse(Fagsystem.DAGPENGER, sakId, behandlingId, null)
        assertEquals(tilkjentYtelse, hentetTilkjentYtelse)
    }

    @Test
    fun `hent ikke-eksisterende tilstand, forvent nullverdi i retur og ingen unntak`() {
        val hentetTilkjentYtelse =
            iverksettingsresultatService.hentTilkjentYtelse(Fagsystem.DAGPENGER, sakId, RandomOSURId.generate(), null)
        assertNull(hentetTilkjentYtelse)
    }

    @Test
    fun `hent korrekt iverksettingsresultat for behandling med to iverksettinger`() {
        val behandlingId = RandomOSURId.generate()
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
            iverksettingsresultatService.hentIverksettingsresultat(
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
        val behandlingId = RandomOSURId.generate()
        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId,
            behandlingId = behandlingId,
            iverksettingId = null,
        )
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId,
            behandlingId = behandlingId,
            tilkjentYtelseForUtbetaling = tilkjentYtelse,
            iverksettingId = null,
        )
        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = Fagsystem.TILTAKSPENGER,
            sakId = sakId,
            behandlingId = behandlingId,
            iverksettingId = null,
        )

        val iverksettingTilleggsstønader =
            iverksettingsresultatService.hentIverksettingsresultat(
                fagsystem = Fagsystem.TILLEGGSSTØNADER,
                sakId = sakId,
                behandlingId = behandlingId,
                iverksettingId = null,
            )

        assertNotNull(iverksettingTilleggsstønader)
        assertNotNull(iverksettingTilleggsstønader?.tilkjentYtelseForUtbetaling)
        assertEquals(Fagsystem.TILLEGGSSTØNADER, iverksettingTilleggsstønader?.fagsystem)
    }

    @Test
    fun `hent korrekt iverksettingsresultat for flere fagsaker med samme behandlingId`() {
        val sakId1 = RandomOSURId.generate()
        val sakId2 = RandomOSURId.generate()
        val behandlingId = RandomOSURId.generate()
        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId1,
            behandlingId = behandlingId,
            iverksettingId = null,
        )
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId1,
            behandlingId = behandlingId,
            tilkjentYtelseForUtbetaling = tilkjentYtelse,
            iverksettingId = null,
        )
        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId2,
            behandlingId = behandlingId,
            iverksettingId = null,
        )

        val iverksettingSak1 =
            iverksettingsresultatService.hentIverksettingsresultat(
                fagsystem = Fagsystem.TILLEGGSSTØNADER,
                sakId = sakId1,
                behandlingId = behandlingId,
                iverksettingId = null,
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
            iverksettingId = null,
        )
        iverksettingsresultatService.oppdaterOppdragResultat(
            fagsystem = Fagsystem.DAGPENGER,
            sakId = sakId,
            behandlingId = behandlingId,
            oppdragResultat = resultat.oppdragResultat!!,
            iverksettingId = null,
        )

        val iverksettResultat =
            iverksettingsresultatService.hentIverksettingsresultat(Fagsystem.DAGPENGER, sakId, behandlingId, null)
        assertEquals(resultat, iverksettResultat)
    }
}
