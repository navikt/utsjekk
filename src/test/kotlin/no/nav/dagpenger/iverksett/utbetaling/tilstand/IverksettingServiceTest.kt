package no.nav.dagpenger.iverksett.utbetaling.tilstand

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.iverksett.felles.oppdrag.OppdragClient
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.util.IverksettResultatMockBuilder
import no.nav.dagpenger.iverksett.utbetaling.util.opprettTilkjentYtelse
import no.nav.dagpenger.iverksett.util.mockFeatureToggleService
import no.nav.dagpenger.kontrakter.iverksett.IverksettStatus
import no.nav.dagpenger.kontrakter.oppdrag.OppdragStatus
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

internal class IverksettingServiceTest {
    val iverksettingsresultatService = mockk<IverksettingsresultatService>()
    val taskService = mockk<TaskService>()
    val iverksettingRepository = mockk<IverksettingRepository>()
    private val oppdragClient = mockk<OppdragClient>()

    private var iverksettStatusService: IverksettingService =
        IverksettingService(
            taskService = taskService,
            iverksettingsresultatService = iverksettingsresultatService,
            iverksettingRepository = iverksettingRepository,
            oppdragClient = oppdragClient,
            featureToggleService = mockFeatureToggleService(),
        )

    @Test
    fun `la IverksettResultat ha felt kun satt for tilkjent ytelse, forvent status SENDT_TIL_OPPDRAG`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { iverksettingsresultatService.hentIverksettResultat(behandlingsId) } returns
            IverksettResultatMockBuilder.Builder()
                .build(behandlingsId, tilkjentYtelse)

        val status = iverksettStatusService.utledStatus(behandlingsId)
        assertThat(status).isEqualTo(IverksettStatus.SENDT_TIL_OPPDRAG)
    }

    @Test
    fun `la IverksettResultat ha tilkjent ytelse, oppdrag, og oppdragsresultat satt, forvent status FEILET_MOT_OPPDRAG`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { iverksettingsresultatService.hentIverksettResultat(behandlingsId) } returns
            IverksettResultatMockBuilder.Builder()
                .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_FUNKSJONELL_FEIL))
                .build(behandlingsId, tilkjentYtelse)

        val status = iverksettStatusService.utledStatus(behandlingsId)
        assertThat(status).isEqualTo(IverksettStatus.FEILET_MOT_OPPDRAG)
    }

    @Test
    fun `la IverksettResultat ha felt satt for tilkjent ytelse, oppdrag med kvittert_ok, forvent status OK`() {
        val behandlingsId = UUID.randomUUID()
        val tilkjentYtelse = opprettTilkjentYtelse(behandlingsId)
        every { iverksettingsresultatService.hentIverksettResultat(behandlingsId) } returns
            IverksettResultatMockBuilder.Builder()
                .oppdragResultat(OppdragResultat(OppdragStatus.KVITTERT_OK))
                .build(behandlingsId, tilkjentYtelse)

        val status = iverksettStatusService.utledStatus(behandlingsId)
        assertThat(status).isEqualTo(IverksettStatus.OK)
    }
}
