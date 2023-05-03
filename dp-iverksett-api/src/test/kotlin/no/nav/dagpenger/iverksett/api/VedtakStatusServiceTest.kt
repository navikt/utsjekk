package no.nav.dagpenger.iverksett.api

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingType
import no.nav.dagpenger.iverksett.kontrakter.felles.Vedtaksresultat
import no.nav.dagpenger.iverksett.kontrakter.iverksett.IverksettStatus
import no.nav.dagpenger.iverksett.lagIverksett
import no.nav.dagpenger.iverksett.lagIverksettData
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
            lagIverksett(iverksettDataFørsteVedtak)
        )
        every { iverksettingServiceMock.utledStatus(any()) } returns IverksettStatus.OK

        val vedtak = vedtakStatusService.getVedtakStatus("12345678910")

        assertNotNull(vedtak)
    }

    @Test
    fun `skal hente siste vedtak som er iverksatt OK når det finnes flere`() {
        every { iverksettingRepositoryMock.findByPersonId(any()) } returns listOf(
            lagIverksett(iverksettDataFørsteVedtak),
            lagIverksett(iverksettDataSisteVedtak)
        )
        every { iverksettingServiceMock.utledStatus(any()) } returns IverksettStatus.OK

        val vedtak = vedtakStatusService.getVedtakStatus("12345678910")

        assertEquals(iverksettDataSisteVedtak.vedtak, vedtak)
    }

    @Test
    fun `skal hente vedtak som er iverksatt OK når det også finnes vedtak som ikke er ferdig iverksatt`() {
        every { iverksettingRepositoryMock.findByPersonId(any()) } returns listOf(
            lagIverksett(iverksettDataFørsteVedtak),
            lagIverksett(iverksettDataSisteVedtak)
        )

        every { iverksettingServiceMock.utledStatus(iverksettDataFørsteVedtak.behandling.behandlingId) } returns IverksettStatus.OK
        every { iverksettingServiceMock.utledStatus(iverksettDataSisteVedtak.behandling.behandlingId) } returns IverksettStatus.FEILET_MOT_OPPDRAG

        val vedtak = vedtakStatusService.getVedtakStatus("12345678910")

        assertEquals(iverksettDataFørsteVedtak.vedtak, vedtak)
    }

    companion object {
        private val iverksettDataFørsteVedtak = lagIverksettData(
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtakstidspunkt = LocalDateTime.now().minusMonths(2)
        )
        private val iverksettDataSisteVedtak = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtakstidspunkt = LocalDateTime.now()
        )
    }
}