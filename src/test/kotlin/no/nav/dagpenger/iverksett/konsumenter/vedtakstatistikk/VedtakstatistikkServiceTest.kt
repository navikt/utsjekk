package no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import no.nav.dagpenger.iverksett.ResourceLoaderTestUtil
import no.nav.dagpenger.iverksett.infrastruktur.transformer.toDomain
import no.nav.dagpenger.iverksett.infrastruktur.util.ObjectMapperProvider.objectMapper
import no.nav.dagpenger.iverksett.util.opprettIverksettDagpenger
import no.nav.dagpenger.kontrakter.iverksett.IverksettDagpengerdDto
import no.nav.dagpenger.kontrakter.iverksett.Opplysningskilde
import no.nav.dagpenger.kontrakter.iverksett.Revurderingsårsak
import no.nav.dagpenger.kontrakter.iverksett.VilkårType
import no.nav.dagpenger.kontrakter.iverksett.dvh.AdressebeskyttelseDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.AktivitetTypeDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.AktivitetskravDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.BarnDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.BehandlingTypeDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.BehandlingÅrsakDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.PersonDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.StønadTypeDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.UtbetalingDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.UtbetalingsdetaljDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.VedtakDagpengerDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.VedtakresultatDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.VedtaksperiodeDagpengerDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.VedtaksperiodeTypeDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.VilkårDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.VilkårsresultatDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.VilkårsvurderingDVH
import no.nav.dagpenger.kontrakter.iverksett.dvh.ÅrsakRevurderingDVH
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID

class VedtakstatistikkServiceTest {

    private var vedtakstatistikkKafkaProducer: VedtakstatistikkKafkaProducer = mockk(relaxed = true)
    private var vedtakstatistikkService = VedtakstatistikkService(vedtakstatistikkKafkaProducer)

    @Test
    internal fun `vedtakstatistikk skal kalle kafka producer med riktig data`() {
        val behandlingId = UUID.randomUUID()

        val vedtakstatistikkJsonSlot = slot<VedtakDagpengerDVH>()
        every { vedtakstatistikkKafkaProducer.sendVedtak(capture(vedtakstatistikkJsonSlot)) } just Runs

        val iverksettDagpenger = opprettIverksettDagpenger(behandlingId)
        vedtakstatistikkService.sendTilKafka(iverksettData = iverksettDagpenger, forrigeIverksett = null)

        val vedtakDagpenger = opprettVedtakstatistikkDagpenger(
            behandlingId = iverksettDagpenger.behandling.behandlingId,
            fagsakId = iverksettDagpenger.fagsak.fagsakId,
            tidspunktVedtak = iverksettDagpenger.vedtak.vedtakstidspunkt.toLocalDate(),
            barn = iverksettDagpenger.søker.barn.map { BarnDVH(it.personIdent, it.termindato) },
        )
        assertThat(vedtakDagpenger).isEqualTo(vedtakstatistikkJsonSlot.captured)
    }

    @Test
    internal fun `map fra iverksettDtoEksempel til behandlingDVH`() {
        val iverksettDtoJson: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")

        val iverksettDto = objectMapper.readValue<IverksettDagpengerdDto>(iverksettDtoJson)
        val iverksett = iverksettDto.toDomain()

        val vedtakDagpengerDVH = slot<VedtakDagpengerDVH>()
        every { vedtakstatistikkKafkaProducer.sendVedtak(capture(vedtakDagpengerDVH)) } just Runs
        vedtakstatistikkService.sendTilKafka(iverksett, null)
        assertThat(vedtakDagpengerDVH).isNotNull
        assertThat(vedtakDagpengerDVH.captured.vilkårsvurderinger.size).isEqualTo(2)
        // Egen test på vilkårtype, da det er mismatch mellom ekstern kontrakt og ef. F.eks. finnes ikke aktivitet i kontrakt.
        assertThat(vedtakDagpengerDVH.captured.vilkårsvurderinger.first().vilkår.name).isEqualTo(VilkårType.AKTIVITET.name)
        assertThat(vedtakDagpengerDVH.captured.vilkårsvurderinger.last().vilkår.name)
            .isEqualTo(VilkårType.SAGT_OPP_ELLER_REDUSERT.name)
    }

    private fun opprettVedtakstatistikkDagpenger(
        behandlingId: UUID,
        fagsakId: UUID,
        tidspunktVedtak: LocalDate,
        barn: List<BarnDVH> = emptyList(),
    ): VedtakDagpengerDVH {
        return VedtakDagpengerDVH(
            fagsakId = fagsakId,
            behandlingId = behandlingId,
            relatertBehandlingId = null,
            adressebeskyttelse = AdressebeskyttelseDVH.UGRADERT,
            tidspunktVedtak = tidspunktVedtak.atStartOfDay(ZoneId.of("Europe/Oslo")),
            vilkårsvurderinger = listOf(
                VilkårsvurderingDVH(
                    vilkår = VilkårDVH.SAGT_OPP_ELLER_REDUSERT,
                    resultat = VilkårsresultatDVH.OPPFYLT,
                ),
            ),
            person = PersonDVH(personIdent = "12345678910"),
            barn = barn,
            behandlingType = BehandlingTypeDVH.FØRSTEGANGSBEHANDLING,
            behandlingÅrsak = BehandlingÅrsakDVH.SØKNAD,
            vedtak = VedtakresultatDVH.INNVILGET,
            vedtaksperioder = listOf(
                VedtaksperiodeDagpengerDVH(
                    fraOgMed = YearMonth.now().atDay(1),
                    tilOgMed = YearMonth.now().atEndOfMonth(),
                    aktivitet = AktivitetTypeDVH.BARNET_ER_SYKT,
                    periodeType = VedtaksperiodeTypeDVH.HOVEDPERIODE,
                ),
            ),
            utbetalinger = listOf(
                UtbetalingDVH(
                    beløp = 5000,
                    fraOgMed = LocalDate.parse("2021-01-01"),
                    tilOgMed = LocalDate.parse("2021-12-31"),
                    inntekt = 100,
                    inntektsreduksjon = 5,
                    samordningsfradrag = 2,
                    utbetalingsdetalj = UtbetalingsdetaljDVH(
                        klassekode = "DPORAS",
                        gjelderPerson = PersonDVH(personIdent = "12345678910"),
                        delytelseId = fagsakId.toString(),
                    ),
                ),
            ),

            aktivitetskrav = AktivitetskravDVH(
                aktivitetspliktInntrefferDato = null,
                harSagtOppArbeidsforhold = true,
            ),
            funksjonellId = behandlingId,
            stønadstype = StønadTypeDVH.DAGPENGER,
            kravMottatt = LocalDate.of(2021, 3, 3),
            årsakRevurdering = ÅrsakRevurderingDVH(
                Opplysningskilde.MELDING_MODIA.name,
                Revurderingsårsak.ENDRING_INNTEKT.name,
            ),
        )
    }
}
