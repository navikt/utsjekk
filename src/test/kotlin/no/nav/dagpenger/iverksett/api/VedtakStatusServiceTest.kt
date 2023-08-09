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
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeType
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import no.nav.dagpenger.kontrakter.iverksett.VedtaksstatusDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

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
        val iverksettingsListe1 = vedtakStatusService.hentIverksettingerForPersonOgPeriode(
            VedtakStatusService.VedtakRequest(personId, LocalDate.now(), LocalDate.now().plusDays(14))
        )
        assertEquals(1, iverksettingsListe1.size)

        // Perioder:  |-------|  |-------|
        // Request:            |-----------
        val iverksettingsListe2 = vedtakStatusService.hentIverksettingerForPersonOgPeriode(
            VedtakStatusService.VedtakRequest(personId, LocalDate.now().minusDays(1), null)
        )
        assertEquals(1, iverksettingsListe2.size)

        // Perioder:  |-------|  |-------|
        // Request:           |-----------
        val iverksettingsListe3 = vedtakStatusService.hentIverksettingerForPersonOgPeriode(
            VedtakStatusService.VedtakRequest(personId, LocalDate.now().minusDays(2), null)
        )
        assertEquals(2, iverksettingsListe3.size)

        // Perioder:  |-------|  |-------|
        // Request:  |--------------------
        val iverksettingsListe4 = vedtakStatusService.hentIverksettingerForPersonOgPeriode(
            VedtakStatusService.VedtakRequest(personId, LocalDate.now().minusDays(20), null)
        )
        assertEquals(2, iverksettingsListe4.size)

        // Perioder:  |-------|  |-------|
        // Request:    |----|
        val iverksettingsListe5 = vedtakStatusService.hentIverksettingerForPersonOgPeriode(
            VedtakStatusService.VedtakRequest(personId, LocalDate.now().minusDays(15), LocalDate.now().minusDays(10))
        )
        assertEquals(1, iverksettingsListe5.size)

        // Perioder:  |-------|  |-------|
        // Request:                        |----
        val iverksettingsListe6 = vedtakStatusService.hentIverksettingerForPersonOgPeriode(
            VedtakStatusService.VedtakRequest(personId, LocalDate.now().plusDays(20), null)
        )
        assertEquals(0, iverksettingsListe6.size)
    }

    companion object {
        private val iverksettDataFørsteVedtak = lagIverksettData(
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtakstidspunkt = LocalDateTime.now().minusMonths(2),
            vedtaksperioder = listOf(
                VedtaksperiodeDagpenger(
                    periode = Datoperiode(LocalDate.now().minusDays(16), LocalDate.now().minusDays(2)),
                    periodeType = VedtaksperiodeType.HOVEDPERIODE,
                ),
            ),
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
                    periode = Datoperiode(LocalDate.now(), LocalDate.now().plusDays(14)),
                    periodeType = VedtaksperiodeType.HOVEDPERIODE,
                ),
            ),
        )
    }

    private fun assertVedtak(riktigIverksettData: IverksettDagpenger, returnertVedtak: VedtaksstatusDto?) {
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
