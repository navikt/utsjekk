package no.nav.dagpenger.iverksett.api

import java.util.UUID
import no.nav.dagpenger.iverksett.ServerTest
import no.nav.dagpenger.iverksett.api.domene.OppdragResultat
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.tilstand.IverksettingsresultatService
import no.nav.dagpenger.iverksett.infrastruktur.util.IverksettResultatMockBuilder
import no.nav.dagpenger.iverksett.infrastruktur.util.opprettTilkjentYtelse
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class HentIverksettingResultatServiceTest : ServerTest() {

    @Autowired
    private lateinit var iverksettingsresultatService: IverksettingsresultatService

    private val behandlingId: UUID = UUID.randomUUID()
    private val tilkjentYtelse: TilkjentYtelse = opprettTilkjentYtelse(behandlingId)

    @BeforeEach
    fun beforeEach() {
        iverksettingsresultatService.opprettTomtResultat(behandlingId)
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)
    }

    @Test
    fun `hent ekisterende tilkjent ytelse, forvent likhet og ingen unntak`() {
        val hentetTilkjentYtelse = iverksettingsresultatService.hentTilkjentYtelse(behandlingId)
        assertThat(hentetTilkjentYtelse).isEqualTo(tilkjentYtelse)
    }

    @Test
    fun `hent ikke-eksisterende tilstand, forvent nullverdi i retur og ingen unntak`() {
        val hentetTilkjentYtelse = iverksettingsresultatService.hentTilkjentYtelse(UUID.randomUUID())
        assertThat(hentetTilkjentYtelse).isEqualTo(null)
    }

    @Test
    fun `lagre tilkjentYtelse, hent IverksettResultat med riktig behandlingsID`() {
        val resultat = IverksettResultatMockBuilder.Builder()
            .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_OK))
            .build(behandlingId, tilkjentYtelse)
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)
        iverksettingsresultatService.oppdaterOppdragResultat(behandlingId, resultat.oppdragResultat!!)

        val iverksettResultat = iverksettingsresultatService.hentIverksettResultat(behandlingId)
        assertThat(iverksettResultat).isEqualTo(resultat)
    }

    @Test
    internal fun `hent tilkjentytelser for flere oppdragIdn`() {
        val behandlingId2 = UUID.randomUUID()
        iverksettingsresultatService.opprettTomtResultat(behandlingId2)

        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId2, tilkjentYtelse)

        val tilkjentYtelsePåBehandlingId = iverksettingsresultatService.hentTilkjentYtelse(setOf(behandlingId, behandlingId2))

        assertThat(tilkjentYtelsePåBehandlingId).containsKeys(behandlingId, behandlingId2)
    }

    @Test
    internal fun `hentTilkjentYtelse for flere oppdragIdn kaster feil hvis den ikke finner tilkjent ytelse for en behandling`() {
        val behandlingId2 = UUID.randomUUID()
        iverksettingsresultatService.oppdaterTilkjentYtelseForUtbetaling(behandlingId, tilkjentYtelse)

        assertThat(catchThrowable { iverksettingsresultatService.hentTilkjentYtelse(setOf(behandlingId, behandlingId2)) })
            .hasMessageContaining("=[$behandlingId2]")
    }
}
