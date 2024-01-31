package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.util.opprettTilkjentYtelse
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

internal class LagreIverksettingResultatServiceTest : ServerTest() {
    @Autowired
    private lateinit var iverksettingsresultatService: IverksettingsresultatService

    private val behandlingsId: UUID = UUID.randomUUID()

    @BeforeEach
    fun beforeEach() {
        iverksettingsresultatService.opprettTomtResultat(Fagsystem.DAGPENGER, behandlingsId)
    }

    @Test
    fun `oppdater tilkjent ytelse, forvent ingen unntak`() {
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(Fagsystem.DAGPENGER, behandlingsId, tilkjentYtelse)
    }

    @Test
    fun `oppdater korrekt tilkjent ytelse når samme behandlingId for flere fagsystem`() {
        iverksettingsresultatService.opprettTomtResultat(Fagsystem.TILTAKSPENGER, behandlingsId)
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(Fagsystem.DAGPENGER, behandlingsId, tilkjentYtelse)

        val tilkjentYtelseDagpenger = iverksettingsresultatService.hentTilkjentYtelse(Fagsystem.DAGPENGER, behandlingsId)
        val tilkjentYtelseTiltakspenger = iverksettingsresultatService.hentTilkjentYtelse(Fagsystem.TILTAKSPENGER, behandlingsId)

        assertNotNull(tilkjentYtelseDagpenger)
        assertNull(tilkjentYtelseTiltakspenger)
    }

    @Test
    fun `oppdater korrekt tilkjent ytelse når flere iverksettinger for samme behandling`() {
        val iverksettingId1 = "IVERK1"
        val iverksettingId2 = "IVERK2"
        iverksettingsresultatService.opprettTomtResultat(Fagsystem.TILLEGGSSTØNADER, behandlingsId, iverksettingId1)
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(
            Fagsystem.TILLEGGSSTØNADER,
            behandlingsId,
            tilkjentYtelse,
            iverksettingId1,
        )
        iverksettingsresultatService.opprettTomtResultat(Fagsystem.TILLEGGSSTØNADER, behandlingsId, iverksettingId2)

        val tilkjentYtelseIverksetting1 =
            iverksettingsresultatService.hentTilkjentYtelse(
                Fagsystem.TILLEGGSSTØNADER,
                behandlingsId,
                iverksettingId1,
            )
        val tilkjentYtelseIverksetting2 =
            iverksettingsresultatService.hentTilkjentYtelse(
                Fagsystem.TILLEGGSSTØNADER,
                behandlingsId,
                iverksettingId2,
            )

        assertNotNull(tilkjentYtelseIverksetting1)
        assertNull(tilkjentYtelseIverksetting2)
    }

    @Test
    fun `oppdater oppdrag, forvent ingen unntak`() {
        val oppdragResultat = OppdragResultat(oppdragStatus = OppdragStatus.KVITTERT_OK)
        iverksettingsresultatService.oppdaterOppdragResultat(Fagsystem.DAGPENGER, behandlingsId, oppdragResultat)
    }
}
