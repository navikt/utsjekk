package no.nav.dagpenger.iverksett.api

import java.util.UUID
import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.api.domene.OppdragResultat
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.util.opprettTilkjentYtelse
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class LagreIverksettResultatServiceTest : ServerTest() {

    @Autowired
    private lateinit var tilstandRepositoryService: IverksettResultatService

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
