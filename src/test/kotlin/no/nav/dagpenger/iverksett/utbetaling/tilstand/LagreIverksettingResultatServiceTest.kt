package no.nav.dagpenger.iverksett.utbetaling.tilstand

import java.util.UUID
import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.tilstand.IverksettingsresultatService
import no.nav.dagpenger.iverksett.utbetaling.util.opprettTilkjentYtelse
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class LagreIverksettingResultatServiceTest : ServerTest() {

    @Autowired
    private lateinit var tilstandRepositoryService: IverksettingsresultatService

    private val behandlingsId: UUID = UUID.randomUUID()

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
}