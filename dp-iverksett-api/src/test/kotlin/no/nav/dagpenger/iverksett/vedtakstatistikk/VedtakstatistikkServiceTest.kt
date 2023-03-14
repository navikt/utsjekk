package no.nav.dagpenger.iverksett.vedtakstatistikk

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import no.nav.dagpenger.iverksett.ResourceLoaderTestUtil
import no.nav.dagpenger.iverksett.infrastruktur.transformer.toDomain
import no.nav.dagpenger.iverksett.kontrakter.dvh.Adressebeskyttelse
import no.nav.dagpenger.iverksett.kontrakter.dvh.AktivitetType
import no.nav.dagpenger.iverksett.kontrakter.dvh.Aktivitetskrav
import no.nav.dagpenger.iverksett.kontrakter.dvh.Barn
import no.nav.dagpenger.iverksett.kontrakter.dvh.BehandlingType
import no.nav.dagpenger.iverksett.kontrakter.dvh.BehandlingÅrsak
import no.nav.dagpenger.iverksett.kontrakter.dvh.Person
import no.nav.dagpenger.iverksett.kontrakter.dvh.StønadType
import no.nav.dagpenger.iverksett.kontrakter.dvh.Utbetaling
import no.nav.dagpenger.iverksett.kontrakter.dvh.Utbetalingsdetalj
import no.nav.dagpenger.iverksett.kontrakter.dvh.Vedtak
import no.nav.dagpenger.iverksett.kontrakter.dvh.VedtakOvergangsstønadDVH
import no.nav.dagpenger.iverksett.kontrakter.dvh.VedtaksperiodeOvergangsstønadDto
import no.nav.dagpenger.iverksett.kontrakter.dvh.VedtaksperiodeType
import no.nav.dagpenger.iverksett.kontrakter.dvh.Vilkår
import no.nav.dagpenger.iverksett.kontrakter.dvh.Vilkårsresultat
import no.nav.dagpenger.iverksett.kontrakter.dvh.VilkårsvurderingDto
import no.nav.dagpenger.iverksett.kontrakter.dvh.ÅrsakRevurdering
import no.nav.dagpenger.iverksett.kontrakter.felles.Opplysningskilde
import no.nav.dagpenger.iverksett.kontrakter.felles.Revurderingsårsak
import no.nav.dagpenger.iverksett.kontrakter.felles.VilkårType
import no.nav.dagpenger.iverksett.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.iverksett.util.ObjectMapperProvider.objectMapper
import no.nav.dagpenger.iverksett.util.opprettIverksettOvergangsstønad
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

        val vedtakstatistikkJsonSlot = slot<VedtakOvergangsstønadDVH>()
        every { vedtakstatistikkKafkaProducer.sendVedtak(capture(vedtakstatistikkJsonSlot)) } just Runs

        val iverksettOvergangsstønad = opprettIverksettOvergangsstønad(behandlingId)
        vedtakstatistikkService.sendTilKafka(iverksettData = iverksettOvergangsstønad, forrigeIverksett = null)

        val vedtakOvergangsstønad = opprettVedtakstatistikkOvergangsstønad(
            behandlingId = iverksettOvergangsstønad.behandling.eksternId,
            fagsakId = iverksettOvergangsstønad.fagsak.eksternId,
            tidspunktVedtak = iverksettOvergangsstønad.vedtak.vedtakstidspunkt.toLocalDate(),
            barn = iverksettOvergangsstønad.søker.barn.map { Barn(it.personIdent, it.termindato) },
        )
        assertThat(vedtakOvergangsstønad).isEqualTo(vedtakstatistikkJsonSlot.captured)
    }

    @Test
    internal fun `map fra iverksettDtoEksempel til behandlingDVH`() {
        val iverksettDtoJson: String = ResourceLoaderTestUtil.readResource("json/IverksettDtoEksempel.json")

        val iverksettDto = objectMapper.readValue<IverksettDto>(iverksettDtoJson)
        val iverksett = iverksettDto.toDomain()

        val vedtakOvergangsstønadDVH = slot<VedtakOvergangsstønadDVH>()
        every { vedtakstatistikkKafkaProducer.sendVedtak(capture(vedtakOvergangsstønadDVH)) } just Runs
        vedtakstatistikkService.sendTilKafka(iverksett, null)
        // val vedtakOvergangsstønadDVH = objectMapper.readValue(vedtakOvergangsstønadDVHJsonSlot.captured, VedtakOvergangsstønadDVH::class.java)
        assertThat(vedtakOvergangsstønadDVH).isNotNull
        assertThat(vedtakOvergangsstønadDVH.captured.vilkårsvurderinger.size).isEqualTo(2)
        // Egen test på vilkårtype, da det er mismatch mellom ekstern kontrakt og ef. F.eks. finnes ikke aktivitet i kontrakt.
        assertThat(vedtakOvergangsstønadDVH.captured.vilkårsvurderinger.first().vilkår.name).isEqualTo(VilkårType.AKTIVITET.name)
        assertThat(vedtakOvergangsstønadDVH.captured.vilkårsvurderinger.last().vilkår.name)
            .isEqualTo(VilkårType.SAGT_OPP_ELLER_REDUSERT.name)
    }

    private fun opprettVedtakstatistikkOvergangsstønad(
        behandlingId: Long,
        fagsakId: Long,
        tidspunktVedtak: LocalDate,
        barn: List<Barn> = emptyList(),
    ): VedtakOvergangsstønadDVH {
        return VedtakOvergangsstønadDVH(
            fagsakId = fagsakId,
            behandlingId = behandlingId,
            relatertBehandlingId = null,
            adressebeskyttelse = Adressebeskyttelse.UGRADERT,
            tidspunktVedtak = tidspunktVedtak.atStartOfDay(ZoneId.of("Europe/Oslo")),
            vilkårsvurderinger = listOf(
                VilkårsvurderingDto(
                    vilkår = Vilkår.SAGT_OPP_ELLER_REDUSERT,
                    resultat = Vilkårsresultat.OPPFYLT,
                ),
            ),
            person = Person(personIdent = "12345678910"),
            barn = barn,
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            behandlingÅrsak = BehandlingÅrsak.SØKNAD,
            vedtak = Vedtak.INNVILGET,
            vedtaksperioder = listOf(
                VedtaksperiodeOvergangsstønadDto(
                    fraOgMed = YearMonth.now().atDay(1),
                    tilOgMed = YearMonth.now().atEndOfMonth(),
                    aktivitet = AktivitetType.BARNET_ER_SYKT,
                    periodeType = VedtaksperiodeType.HOVEDPERIODE,
                ),
            ),
            utbetalinger = listOf(
                Utbetaling(
                    beløp = 5000,
                    fraOgMed = LocalDate.parse("2021-01-01"),
                    tilOgMed = LocalDate.parse("2021-12-31"),
                    inntekt = 100,
                    inntektsreduksjon = 5,
                    samordningsfradrag = 2,
                    utbetalingsdetalj = Utbetalingsdetalj(
                        klassekode = "EFOG",
                        gjelderPerson = Person(personIdent = "12345678910"),
                        delytelseId = "1",
                    ),
                ),
            ),

            aktivitetskrav = Aktivitetskrav(
                aktivitetspliktInntrefferDato = null,
                harSagtOppArbeidsforhold = true,
            ),
            funksjonellId = 9L,
            stønadstype = StønadType.OVERGANGSSTØNAD,
            kravMottatt = LocalDate.of(2021, 3, 3),
            årsakRevurdering = ÅrsakRevurdering(
                Opplysningskilde.MELDING_MODIA.name,
                Revurderingsårsak.ENDRING_INNTEKT.name,
            ),
        )
    }
}
