package no.nav.dagpenger.iverksett.konsumenter.oppgave

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import no.nav.dagpenger.iverksett.api.IverksettingRepository
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.VedtaksperiodeDagpenger
import no.nav.dagpenger.iverksett.api.domene.behandlingId
import no.nav.dagpenger.iverksett.infrastruktur.FamilieIntegrasjonerClient
import no.nav.dagpenger.iverksett.infrastruktur.repository.findByIdOrThrow
import no.nav.dagpenger.iverksett.lagIverksett
import no.nav.dagpenger.iverksett.lagIverksettData
import no.nav.dagpenger.iverksett.util.mockFeatureToggleService
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.iverksett.BehandlingType
import no.nav.dagpenger.kontrakter.iverksett.BehandlingÅrsak
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeType
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class OppgaveServiceTest {

    val iverksettRepository = mockk<IverksettingRepository>()
    val familieIntegrasjonerClient = mockk<FamilieIntegrasjonerClient>()
    val oppgaveClient = mockk<OppgaveClient>()
    val featureToggleService = mockFeatureToggleService()
    val oppgaveService =
        OppgaveService(oppgaveClient, familieIntegrasjonerClient, iverksettRepository, featureToggleService)

    @BeforeEach
    internal fun init() {
        mockkObject(OppgaveUtil)
        mockkObject(OppgaveBeskrivelse)
        every { familieIntegrasjonerClient.hentBehandlendeEnhetForOppfølging(any()) } returns mockk()
        every { oppgaveClient.opprettOppgave(any()) } returns 0L
        every { OppgaveUtil.opprettOppgaveRequest(any(), any(), any(), any(), any(), any(), any()) } returns mockk()
    }

    @AfterEach
    internal fun tearDown() {
        unmockkAll()
    }

    @Test
    internal fun `innvilget førstegangsbehandling, forvent skalOpprette true`() {
        val iverksett = lagIverksettData(
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = emptyList(),
        )
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue
    }

    @Test
    internal fun `revurdering opphørt, forvent skalOpprette true`() {
        val iverksett = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.OPPHØRT,
            vedtaksperioder = emptyList(),
        )

        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue
    }

    @Test
    internal fun `revurdering avslått, forvent skalOpprette false`() {
        val iverksettData = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.AVSLÅTT,
            vedtaksperioder = emptyList(),
        )
        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(iverksettData)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksettData)).isFalse
    }

    @Test
    internal fun `revurdering innvilget, forrige er opphørt skal opprette opppgave`() {
        val forrigeBehandlingIverksett = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.OPPHØRT,
            vedtaksperioder = emptyList(),
        )
        val iverksett = lagIverksettData(
            forrigeIverksetting = forrigeBehandlingIverksett,
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(vedtaksPeriode()),
        )
        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(forrigeBehandlingIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget, forrige behandling opphørt og uten perioder, forvent skalOpprette true`() {
        val forrigeBehandlingIverksett = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.OPPHØRT,
            vedtaksperioder = listOf(vedtaksPeriode()),
        )
        val iverksett = lagIverksettData(
            forrigeIverksetting = forrigeBehandlingIverksett,
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(vedtaksPeriode()),
        )

        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(forrigeBehandlingIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget, men avslått f-behandling, forvent skalOpprette true`() {
        val iverksettData = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(vedtaksPeriode()),
        )
        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(iverksettData)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksettData)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget, men avslått f-behandling, forvent kall til beskrivelseFørstegangsbehandlingInnvilget`() {
        val iverksett = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(vedtaksPeriode()),
        )

        oppgaveService.opprettVurderHenvendelseOppgave(iverksett)
        verify { OppgaveBeskrivelse.beskrivelseFørstegangsbehandlingInnvilget(any()) }
        verify(exactly = 0) { OppgaveBeskrivelse.beskrivelseRevurderingInnvilget(any()) }
    }

    @Test
    internal fun `revurdering innvilget med aktivitetsendring og periodeendring, forvent skalOpprette true`() {
        val forrigeBehandlingIverksett = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(
                vedtaksPeriode(
                    fraOgMed = LocalDate.now().plusMonths(2),
                    tilOgMed = LocalDate.now().plusMonths(3),
                ),
            ),
        )
        val iverksett = lagIverksettData(
            forrigeIverksetting = forrigeBehandlingIverksett,
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(vedtaksPeriode()),
        )

        val forrigeBehandlingId = iverksett.behandling.forrigeBehandlingId!!
        every { iverksettRepository.findByIdOrThrow(forrigeBehandlingId) } returns lagIverksett(
            forrigeBehandlingIverksett,
        )
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()

        verify(exactly = 1) { iverksettRepository.findByIdOrThrow(forrigeBehandlingId) }
    }

    @Test
    internal fun `revurdering innvilget med kun periodeendring, forvent skalOpprette true`() {
        val forrigeBehandlingIverksett = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(
                vedtaksPeriode(
                    fraOgMed = LocalDate.now().plusMonths(2),
                    tilOgMed = LocalDate.now().plusMonths(3),
                ),
            ),
        )
        val iverksett = lagIverksettData(
            forrigeIverksetting = forrigeBehandlingIverksett,
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(vedtaksPeriode()),
        )

        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(forrigeBehandlingIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering innvilget med kun endring i fom dato, forvent skalOpprette false`() {
        val forrigeBehandlingIverksett = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(
                vedtaksPeriode(
                    fraOgMed = LocalDate.now().minusMonths(3),
                ),
            ),
        )
        val iverksett = lagIverksettData(
            forrigeIverksetting = forrigeBehandlingIverksett,
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(vedtaksPeriode()),
        )

        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(forrigeBehandlingIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isFalse()
    }

    @Test
    internal fun `innvilget førstegangsbehandling, forvent kall til beskrivelseFørstegangsbehandlingInnvilget`() {
        val iverksett = lagIverksettData(
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(vedtaksPeriode()),
        )

        oppgaveService.opprettVurderHenvendelseOppgave(iverksett)
        verify { OppgaveBeskrivelse.beskrivelseFørstegangsbehandlingInnvilget(any()) }
    }

    @Test
    internal fun `avslått førstegangsbehandling, forvent kall til beskrivelseFørstegangsbehandlingAvslått`() {
        val iverksett = lagIverksettData(
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            vedtaksresultat = Vedtaksresultat.AVSLÅTT,
            vedtaksperioder = listOf(vedtaksPeriode()),
        )

        oppgaveService.opprettVurderHenvendelseOppgave(iverksett)
        verify { OppgaveBeskrivelse.beskrivelseFørstegangsbehandlingAvslått(any()) }
    }

    @Test
    internal fun `innvilget revurdering, forvent kall til beskrivelseRevurderingInnvilget`() {
        val iverksett = lagIverksettData(
            forrigeBehandlingId = UUID.randomUUID(),
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(vedtaksPeriode()),
        )

        oppgaveService.opprettVurderHenvendelseOppgave(iverksett)
        verify { OppgaveBeskrivelse.beskrivelseRevurderingInnvilget(any()) }
    }

    @Test
    internal fun `Hvis iverksetting av sanksjon, lag sanksjonsbeskrivelse på oppgave`() {
        val førsteFebruar23 = LocalDate.of(2023, 2, 1)
        val iverksett = lagIverksettDagpengerSanksjon(førsteFebruar23)

        val oppgavebeskrivelse = oppgaveService.lagOppgavebeskrivelse(iverksett)

        assertThat(oppgavebeskrivelse).isEqualTo("Bruker har fått vedtak om sanksjon 1 mnd: 01 februar 2023")
    }

    @Test
    internal fun `opphørt revurdering, forvent kall til beskrivelseRevurderingOpphørt`() {
        val iverksett = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.OPPHØRT,
            vedtaksperioder = listOf(vedtaksPeriode()),
            andelsdatoer = listOf(LocalDate.now()),
        )

        oppgaveService.opprettVurderHenvendelseOppgave(iverksett)
        verify { OppgaveBeskrivelse.beskrivelseRevurderingOpphørt(any()) }
    }

    @Test
    internal fun `revurdering opphør, forvent at andel med maks tom dato blir sendt som arg til beskrivelse`() {
        val opphørsdato = slot<LocalDate>()
        val iverksett = lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.OPPHØRT,
            vedtaksperioder = listOf(vedtaksPeriode()),
            andelsdatoer = listOf(LocalDate.now().minusMonths(2), LocalDate.now(), LocalDate.now().minusMonths(1)),
        )

        oppgaveService.opprettVurderHenvendelseOppgave(iverksett)
        verify { OppgaveBeskrivelse.beskrivelseRevurderingOpphørt(capture(opphørsdato)) }
        assertThat(opphørsdato.captured).isEqualTo(LocalDate.now())
    }

    @Test
    internal fun `av migreringssak, revurdering opphør, forvent at skalOppretteVurderHendelseOppgave er lik true`() {
        val forrigeBehandlingId = UUID.randomUUID()
        val vedtaksperioder = listOf(vedtaksPeriode())
        val forrigeIverksett = lagIverksettData(
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = vedtaksperioder,
            erMigrering = true,
        )
        val iverksett = lagIverksettData(
            forrigeIverksetting = forrigeIverksett,
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.OPPHØRT,
            vedtaksperioder = vedtaksperioder,
        )

        every { iverksettRepository.findByIdOrThrow(forrigeBehandlingId) } returns lagIverksett(forrigeIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `revurdering, forrige behandling er gOmregnet, periodetype migrering, forvent skalOppretteVurderHendelseOppgave lik false`() {
        val vedtaksperioder = listOf(vedtaksPeriode())
        val forrigeVedtaksperioder = listOf(
            vedtaksPeriode(
                fraOgMed = LocalDate.of(LocalDate.now().year, 5, 1),
                tilOgMed = LocalDate.of(LocalDate.now().year.plus(2), 10, 31),
                periodeType = VedtaksperiodeType.MIGRERING,
            ),
        )
        val forrigeIverksett = lagIverksettData(
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = forrigeVedtaksperioder,
            årsak = BehandlingÅrsak.G_OMREGNING,
        )
        val iverksett = lagIverksettData(
            forrigeIverksetting = forrigeIverksett,
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = vedtaksperioder,
        )

        every { iverksettRepository.findByIdOrThrow(forrigeIverksett.behandlingId) } returns lagIverksett(forrigeIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isFalse()
    }

    @Test
    internal fun `revurdering, forrige behandling er gOmregnet, periodetype ikke migrering, forvent skalOppretteVurderHendelseOppgave lik true`() {
        val vedtaksperioder = listOf(vedtaksPeriode())
        val forrigeVedtaksperioder = listOf(
            vedtaksPeriode(
                fraOgMed = LocalDate.of(LocalDate.now().year, 5, 1),
                tilOgMed = LocalDate.of(LocalDate.now().year.plus(2), 10, 31),
                periodeType = VedtaksperiodeType.HOVEDPERIODE,
            ),
        )
        val forrigeIverksett = lagIverksettData(
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = forrigeVedtaksperioder,
        )
        val iverksett = lagIverksettData(
            forrigeIverksetting = forrigeIverksett,
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = vedtaksperioder,
        )

        every { iverksettRepository.findByIdOrThrow(forrigeIverksett.behandlingId) } returns lagIverksett(forrigeIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue()
    }

    @Test
    internal fun `av migreringssak, revurdering innvilget, forvent at skalOppretteVurderHendelseOppgave er lik false`() {
        val forrigeBehandlingIverksett = lagMigreringsIverksetting()
        val iverksett = lagIverksettData(
            forrigeIverksetting = forrigeBehandlingIverksett,
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(vedtaksPeriode()),
            andelsdatoer = listOf(LocalDate.now().minusMonths(2), LocalDate.now(), LocalDate.now().minusMonths(1)),
        )

        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(forrigeBehandlingIverksett)
        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isFalse()
    }

    @Test
    internal fun `Forrige behandling er migreringssak, iverksettbehandling er av type sanksjon - skal opprette vurder hendelse oppgave`() {
        val iverksett = lagIverksettDagpengerSanksjon()
        val forrigeBehandlingIverksett = lagMigreringsIverksetting()

        every { iverksettRepository.findByIdOrThrow(any()) } returns lagIverksett(forrigeBehandlingIverksett)

        assertThat(oppgaveService.skalOppretteVurderHenvendelseOppgave(iverksett)).isTrue
    }

    private fun lagMigreringsIverksetting() = lagIverksettData(
        behandlingType = BehandlingType.REVURDERING,
        vedtaksresultat = Vedtaksresultat.INNVILGET,
        vedtaksperioder = listOf(
            vedtaksPeriode(
                fraOgMed = LocalDate.now().minusMonths(3),
                periodeType = VedtaksperiodeType.MIGRERING,
            ),
        ),
    )

    private fun lagIverksettDagpengerSanksjon(sanksjonsdato: LocalDate = LocalDate.now()): IverksettDagpenger {
        val datoperiode = Datoperiode(fom = sanksjonsdato, tom = sanksjonsdato)
        val vedtaksPeriode = VedtaksperiodeDagpenger(
            periode = datoperiode,
            periodeType = VedtaksperiodeType.SANKSJON,
        )
        val andeler = listOf(sanksjonsdato.minusMonths(1), sanksjonsdato.plusMonths(1))
        return lagIverksettData(
            behandlingType = BehandlingType.REVURDERING,
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            vedtaksperioder = listOf(vedtaksPeriode),
            andelsdatoer = andeler,
            årsak = BehandlingÅrsak.SANKSJON_1_MND,
        )
    }

    private fun vedtaksPeriode(
        fraOgMed: LocalDate = LocalDate.now(),
        tilOgMed: LocalDate = LocalDate.now(),
        periodeType: VedtaksperiodeType = VedtaksperiodeType.HOVEDPERIODE,
    ): VedtaksperiodeDagpenger {
        return VedtaksperiodeDagpenger(
            periode = Datoperiode(fraOgMed, tilOgMed),
            periodeType = periodeType,
        )
    }
}
