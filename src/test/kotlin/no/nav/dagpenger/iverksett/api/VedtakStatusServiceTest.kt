package no.nav.dagpenger.iverksett.api

import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.dagpenger.iverksett.api.domene.Iverksett
import no.nav.dagpenger.iverksett.api.domene.Vedtaksperiode
import no.nav.dagpenger.iverksett.lagIverksett
import no.nav.dagpenger.iverksett.lagIverksettData
import no.nav.dagpenger.kontrakter.datadeling.DatadelingRequest
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeType
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import no.nav.dagpenger.kontrakter.iverksett.VedtaksstatusDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VedtakStatusServiceTest {

    private val iverksettingRepositoryMock: IverksettingRepository = mockk()
    private val vedtakStatusService: VedtakStatusService = VedtakStatusService(iverksettingRepositoryMock)
    private val personId = "12345678910"

    @Test
    fun `skal hente vedtak som er iverksatt OK`() {
        every { iverksettingRepositoryMock.findByPersonIdAndResult(personId, "INNVILGET") } returns listOf(
            lagIverksett(iverksettDataFørsteVedtak),
        )

        val vedtak = vedtakStatusService.getVedtakStatus(personId)

        assertVedtak(iverksettDataFørsteVedtak, vedtak)
    }

    @Test
    fun `skal hente siste vedtak som er iverksatt OK når det finnes flere`() {
        every { iverksettingRepositoryMock.findByPersonIdAndResult(personId, "INNVILGET") } returns listOf(
            lagIverksett(iverksettDataFørsteVedtak),
            lagIverksett(iverksettDataSisteVedtak),
        )

        val vedtak = vedtakStatusService.getVedtakStatus(personId)

        assertVedtak(iverksettDataSisteVedtak, vedtak)
    }

    @Test
    fun `skal hente iverksettinger som har overlapende perioder`() {
        every { iverksettingRepositoryMock.findByPersonIdAndResult(personId, "INNVILGET") } returns listOf(
            lagIverksett(iverksettDataFørsteVedtak),
            lagIverksett(iverksettDataSisteVedtak),
        )

        // Perioder:  |-------|  |-------|
        // Request:              |-------|
        val response1 = vedtakStatusService.hentVedtaksperioderForPersonOgPeriode(
            DatadelingRequest(personId, LocalDate.now(), LocalDate.now().plusDays(14))
        )
        assertEquals(1, response1.perioder.size)
    }

    @Test
    fun `skal hente iverksettinger som starter etter fom dato 1`() {
        every { iverksettingRepositoryMock.findByPersonIdAndResult(personId, "INNVILGET") } returns listOf(
            lagIverksett(iverksettDataFørsteVedtak),
            lagIverksett(iverksettDataSisteVedtak),
        )

        // Perioder:  |-------|  |-------|
        // Request:            |-----------
        val response2 = vedtakStatusService.hentVedtaksperioderForPersonOgPeriode(
            DatadelingRequest(personId, LocalDate.now().minusDays(1), null)
        )
        assertEquals(1, response2.perioder.size)
    }

    @Test
    fun `skal hente iverksettinger som inneholder fom dato`() {
        every { iverksettingRepositoryMock.findByPersonIdAndResult(personId, "INNVILGET") } returns listOf(
            lagIverksett(iverksettDataFørsteVedtak),
            lagIverksett(iverksettDataSisteVedtak),
        )

        // Perioder:  |-------|  |-------|
        // Request:           |-----------
        val response3 = vedtakStatusService.hentVedtaksperioderForPersonOgPeriode(
            DatadelingRequest(personId, LocalDate.now().minusDays(2), null)
        )
        assertEquals(2, response3.perioder.size)
    }

    @Test
    fun `skal hente iverksettinger som starter etter fom dato 2`() {
        every { iverksettingRepositoryMock.findByPersonIdAndResult(personId, "INNVILGET") } returns listOf(
            lagIverksett(iverksettDataFørsteVedtak),
            lagIverksett(iverksettDataSisteVedtak),
        )

        // Perioder:  |-------|  |-------|
        // Request:  |--------------------
        val response4 = vedtakStatusService.hentVedtaksperioderForPersonOgPeriode(
            DatadelingRequest(personId, LocalDate.now().minusDays(20), null)
        )
        assertEquals(2, response4.perioder.size)
    }

    @Test
    fun `skal hente iverksettinger som har inneholder både fom og tom datoer`() {
        every { iverksettingRepositoryMock.findByPersonIdAndResult(personId, "INNVILGET") } returns listOf(
            lagIverksett(iverksettDataFørsteVedtak),
            lagIverksett(iverksettDataSisteVedtak),
        )

        // Perioder:  |-------|  |-------|
        // Request:    |----|
        val response5 = vedtakStatusService.hentVedtaksperioderForPersonOgPeriode(
            DatadelingRequest(personId, LocalDate.now().minusDays(15), LocalDate.now().minusDays(10))
        )
        assertEquals(1, response5.perioder.size)
    }

    @Test
    fun `skal ikke hente iverksettinger som er før fom dato`() {
        every { iverksettingRepositoryMock.findByPersonIdAndResult(personId, "INNVILGET") } returns listOf(
            lagIverksett(iverksettDataFørsteVedtak),
            lagIverksett(iverksettDataSisteVedtak),
        )

        // Perioder:  |-------|  |-------|
        // Request:                        |----
        val response6 = vedtakStatusService.hentVedtaksperioderForPersonOgPeriode(
            DatadelingRequest(personId, LocalDate.now().plusDays(20), null)
        )
        assertEquals(0, response6.perioder.size)
    }

    companion object {
        private val iverksettDataFørsteVedtak = lagIverksettData(
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtakstidspunkt = LocalDateTime.now().minusMonths(2),
            vedtaksperioder = listOf(
                Vedtaksperiode(
                    periode = Datoperiode(LocalDate.now().minusDays(16), LocalDate.now().minusDays(2)),
                    periodeType = VedtaksperiodeType.HOVEDPERIODE,
                ),
            ),
        )
        private val iverksettDataSisteVedtak = lagIverksettData(
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtakstidspunkt = LocalDateTime.now(),
            vedtaksperioder = listOf(
                Vedtaksperiode(
                    periode = Datoperiode(LocalDate.now(), LocalDate.now().plusDays(14)),
                    periodeType = VedtaksperiodeType.HOVEDPERIODE,
                ),
            ),
        )
    }

    private fun assertVedtak(riktigIverksettData: Iverksett, returnertVedtak: VedtaksstatusDto?) {
        val riktigVedtak = riktigIverksettData.vedtak

        assertEquals(riktigVedtak.vedtakstype, returnertVedtak?.vedtakstype)
        assertEquals(riktigVedtak.vedtakstidspunkt, returnertVedtak?.vedtakstidspunkt)
        assertEquals(riktigVedtak.vedtaksresultat, returnertVedtak?.resultat)

        assertEquals(riktigVedtak.vedtaksperioder.size, returnertVedtak?.vedtaksperioder?.size)
        riktigVedtak.vedtaksperioder.forEachIndexed { index, riktigPeriod ->
            val returnertPeriod = returnertVedtak?.vedtaksperioder?.get(index)
            assertEquals(riktigPeriod.periode.fom, returnertPeriod?.fraOgMedDato)
            assertEquals(riktigPeriod.periode.tom, returnertPeriod?.tilOgMedDato)
            assertEquals(riktigPeriod.periodeType, returnertPeriod?.periodeType)
        }
    }
}
