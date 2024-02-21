package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.util.enTilkjentYtelse
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

internal class LagreIverksettingResultatServiceTest : ServerTest() {
    @Autowired
    private lateinit var iverksettingsresultatService: IverksettingsresultatService

    private val behandlingsId: UUID = UUID.randomUUID()
    private val sakId = GeneriskIdSomUUID(UUID.randomUUID())

    @BeforeEach
    fun beforeEach() {
        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = Fagsystem.DAGPENGER,
            sakId = sakId,
            behandlingId = behandlingsId,
            iverksettingId = null,
        )
    }

    @Test
    fun `oppdater tilkjent ytelse`() {
        assertDoesNotThrow {
            iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
                fagsystem = Fagsystem.DAGPENGER,
                sakId = sakId,
                behandlingId = behandlingsId,
                tilkjentYtelseForUtbetaling = enTilkjentYtelse(behandlingsId),
                iverksettingId = null,
            )
        }
    }

    @Test
    fun `oppdater korrekt tilkjent ytelse når samme behandlingId for flere fagsystem`() {
        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = Fagsystem.TILTAKSPENGER,
            sakId = sakId,
            behandlingId = behandlingsId,
            iverksettingId = null,
        )
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
            fagsystem = Fagsystem.DAGPENGER,
            sakId = sakId,
            behandlingId = behandlingsId,
            tilkjentYtelseForUtbetaling = enTilkjentYtelse(behandlingsId),
            iverksettingId = null,
        )

        val tilkjentYtelseDagpenger =
            iverksettingsresultatService.hentTilkjentYtelse(
                fagsystem = Fagsystem.DAGPENGER,
                sakId = sakId,
                behandlingId = behandlingsId,
                iverksettingId = null,
            )
        val tilkjentYtelseTiltakspenger =
            iverksettingsresultatService.hentTilkjentYtelse(
                fagsystem = Fagsystem.TILTAKSPENGER,
                sakId = sakId,
                behandlingId = behandlingsId,
                iverksettingId = null,
            )

        assertNotNull(tilkjentYtelseDagpenger)
        assertNull(tilkjentYtelseTiltakspenger)
    }

    @Test
    fun `oppdater korrekt tilkjent ytelse når flere iverksettinger for samme behandling`() {
        val iverksettingId1 = "IVERK1"
        val iverksettingId2 = "IVERK2"
        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId,
            behandlingId = behandlingsId,
            iverksettingId = iverksettingId1,
        )
        val tilkjentYtelse = enTilkjentYtelse(behandlingsId)
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId,
            behandlingId = behandlingsId,
            tilkjentYtelseForUtbetaling = tilkjentYtelse,
            iverksettingId = iverksettingId1,
        )
        iverksettingsresultatService.opprettTomtResultat(
            fagsystem = Fagsystem.TILLEGGSSTØNADER,
            sakId = sakId,
            behandlingId = behandlingsId,
            iverksettingId = iverksettingId2,
        )

        val tilkjentYtelseIverksetting1 =
            iverksettingsresultatService.hentTilkjentYtelse(
                fagsystem = Fagsystem.TILLEGGSSTØNADER,
                sakId = sakId,
                behandlingId = behandlingsId,
                iverksettingId = iverksettingId1,
            )
        val tilkjentYtelseIverksetting2 =
            iverksettingsresultatService.hentTilkjentYtelse(
                fagsystem = Fagsystem.TILLEGGSSTØNADER,
                sakId = sakId,
                behandlingId = behandlingsId,
                iverksettingId = iverksettingId2,
            )

        assertNotNull(tilkjentYtelseIverksetting1)
        assertNull(tilkjentYtelseIverksetting2)
    }

    @Test
    fun `oppdater oppdrag, forvent ingen unntak`() {
        val oppdragResultat = OppdragResultat(oppdragStatus = OppdragStatus.KVITTERT_OK)
        iverksettingsresultatService.oppdaterOppdragResultat(
            fagsystem = Fagsystem.DAGPENGER,
            sakId = sakId,
            behandlingId = behandlingsId,
            oppdragResultat = oppdragResultat,
            iverksettingId = null,
        )
    }
}
