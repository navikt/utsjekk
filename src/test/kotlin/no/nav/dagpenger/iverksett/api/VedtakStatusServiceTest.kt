package no.nav.dagpenger.iverksett.api

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.iverksett.lagIverksett
import no.nav.dagpenger.iverksett.lagIverksettData
import no.nav.dagpenger.kontrakter.iverksett.felles.BehandlingType
import no.nav.dagpenger.kontrakter.iverksett.felles.Vedtaksresultat
import no.nav.dagpenger.kontrakter.iverksett.iverksett.IverksettStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class VedtakStatusServiceTest {

    private val iverksettingRepositoryMock: IverksettingRepository = mockk()
    private val iverksettingServiceMock: IverksettingService = mockk()
    private val vedtakStatusService: VedtakStatusService = VedtakStatusService(iverksettingRepositoryMock, iverksettingServiceMock)

    @Test
    fun `skal hente vedtak som er iverksatt OK`() {
        every { iverksettingRepositoryMock.findByPersonId(any()) } returns listOf(
            lagIverksett(iverksettDataFørsteVedtak),
        )

        val vedtak = vedtakStatusService.getVedtakStatus("12345678910")

        assertNotNull(vedtak)
    }

    @Test
    fun `skal hente siste vedtak som er iverksatt OK når det finnes flere`() {
        every { iverksettingRepositoryMock.findByPersonId(any()) } returns listOf(
            lagIverksett(iverksettDataFørsteVedtak),
            lagIverksett(iverksettDataSisteVedtak),
        )

        val vedtak = vedtakStatusService.getVedtakStatus("12345678910")

        assertEquals(iverksettDataSisteVedtak.vedtak, vedtak)
    }

    @Test
    fun `skal hente vedtak som er innvilget når det også finnes vedtak som er avslått`() {
        every { iverksettingRepositoryMock.findByPersonId(any()) } returns listOf(
            lagIverksett(iverksettDataFørsteVedtak),
            lagIverksett(iverksettDataSisteVedtakAvslått),
        )

        every { iverksettingServiceMock.utledStatus(iverksettDataFørsteVedtak.behandling.behandlingId) } returns IverksettStatus.SENDT_TIL_OPPDRAG
        every { iverksettingServiceMock.utledStatus(iverksettDataSisteVedtak.behandling.behandlingId) } returns IverksettStatus.FEILET_MOT_OPPDRAG

        val vedtak = vedtakStatusService.getVedtakStatus("12345678910")

        assertEquals(iverksettDataFørsteVedtak.vedtak, vedtak)
    }

    companion object {
        private val iverksettDataFørsteVedtak = lagIverksettData(
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtakstidspunkt = LocalDateTime.now().minusMonths(2),
        )
        private val iverksettDataSisteVedtak = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtakstidspunkt = LocalDateTime.now(),
        )
        private val iverksettDataSisteVedtakAvslått = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.AVSLÅTT,
            vedtakstidspunkt = LocalDateTime.now(),
        )
    }
}
