package no.nav.dagpenger.iverksett.api

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.VedtaksperiodeDagpenger
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottaker
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottakere
import no.nav.dagpenger.iverksett.lagIverksett
import no.nav.dagpenger.iverksett.lagIverksettData
import no.nav.dagpenger.kontrakter.felles.BrevmottakerDto
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.iverksett.BehandlingType
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeType
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class VedtakStatusServiceTest {

    private val iverksettingRepositoryMock: IverksettingRepository = mockk()
    private val vedtakStatusService: VedtakStatusService = VedtakStatusService(iverksettingRepositoryMock)

    @Test
    fun `skal hente vedtak som er iverksatt OK`() {
        every { iverksettingRepositoryMock.findByPersonIdAndResult("12345678910", "INNVILGET") } returns listOf(
            lagIverksett(iverksettDataFørsteVedtak),
        )

        val vedtak = vedtakStatusService.getVedtakStatus("12345678910")

        assertVedtak(iverksettDataFørsteVedtak, vedtak)
    }

    @Test
    fun `skal hente siste vedtak som er iverksatt OK når det finnes flere`() {
        every { iverksettingRepositoryMock.findByPersonIdAndResult("12345678910", "INNVILGET") } returns listOf(
            lagIverksett(iverksettDataFørsteVedtak),
            lagIverksett(iverksettDataSisteVedtak),
        )

        val vedtak = vedtakStatusService.getVedtakStatus("12345678910")

        assertVedtak(iverksettDataSisteVedtak, vedtak)
    }

    companion object {
        private val iverksettDataFørsteVedtak = lagIverksettData(
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtakstidspunkt = LocalDateTime.now().minusMonths(2),
            brevmottakere = Brevmottakere(
                listOf(
                    Brevmottaker(
                        ident = "01020312345",
                        navn = "Test Testesen",
                        mottakerRolle = BrevmottakerDto.MottakerRolle.BRUKER,
                        identType = BrevmottakerDto.IdentType.PERSONIDENT,
                    ),
                    Brevmottaker(
                        ident = "987654321",
                        navn = "NAV",
                        mottakerRolle = BrevmottakerDto.MottakerRolle.FULLMEKTIG,
                        identType = BrevmottakerDto.IdentType.ORGANISASJONSNUMMER,
                    ),
                ),
            ),
        )
        private val iverksettDataSisteVedtak = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtakstidspunkt = LocalDateTime.now(),
            vedtaksperioder = listOf(
                VedtaksperiodeDagpenger(
                    periode = Datoperiode(LocalDate.now(), LocalDate.now()),
                    periodeType = VedtaksperiodeType.HOVEDPERIODE,
                ),
            ),
        )
        private val iverksettDataSisteVedtakAvslått = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.AVSLÅTT,
            vedtakstidspunkt = LocalDateTime.now(),
        )
    }

    private fun assertVedtak(riktigIverksettData: IverksettDagpenger, returnertVedtak: VedtaksdetaljerDto?) {
        val riktigVedtak = riktigIverksettData.vedtak

        assertEquals(riktigVedtak.vedtakstype, returnertVedtak?.vedtakstype)
        assertEquals(riktigVedtak.vedtakstidspunkt, returnertVedtak?.vedtakstidspunkt)
        assertEquals(riktigVedtak.vedtaksresultat, returnertVedtak?.resultat)
        assertEquals("", returnertVedtak?.saksbehandlerId)
        assertEquals("", returnertVedtak?.beslutterId)
        assertEquals(null, returnertVedtak?.opphorAarsak)
        assertEquals(null, returnertVedtak?.avslagAarsak)
        assertEquals(emptyList<UtbetalingDto>(), returnertVedtak?.utbetalinger)

        assertEquals(riktigVedtak.vedtaksperioder.size, returnertVedtak?.vedtaksperioder?.size)
        riktigVedtak.vedtaksperioder.forEachIndexed { index, riktigPeriod ->
            val returnertPeriod = returnertVedtak?.vedtaksperioder?.get(index)
            assertEquals(riktigPeriod.periode.fom, returnertPeriod?.fraOgMedDato)
            assertEquals(riktigPeriod.periode.tom, returnertPeriod?.tilOgMedDato)
            assertEquals(riktigPeriod.periodeType, returnertPeriod?.periodeType)
        }

        assertEquals(null, riktigVedtak.tilbakekreving)
        assertEquals(emptyList<BrevmottakerDto>(), returnertVedtak?.brevmottakere)
    }
}
