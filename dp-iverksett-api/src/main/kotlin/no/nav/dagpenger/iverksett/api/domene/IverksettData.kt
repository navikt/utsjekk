package no.nav.dagpenger.iverksett.api.domene

import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottakere
import no.nav.dagpenger.iverksett.kontrakter.felles.AvslagÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingType
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.Datoperiode
import no.nav.dagpenger.iverksett.kontrakter.felles.Månedsperiode
import no.nav.dagpenger.iverksett.kontrakter.felles.OpphørÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.Opplysningskilde
import no.nav.dagpenger.iverksett.kontrakter.felles.RegelId
import no.nav.dagpenger.iverksett.kontrakter.felles.Revurderingsårsak
import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import no.nav.dagpenger.iverksett.kontrakter.felles.SvarId
import no.nav.dagpenger.iverksett.kontrakter.felles.Vedtaksresultat
import no.nav.dagpenger.iverksett.kontrakter.felles.VilkårType
import no.nav.dagpenger.iverksett.kontrakter.felles.Vilkårsresultat
import no.nav.dagpenger.iverksett.kontrakter.iverksett.AdressebeskyttelseGradering
import no.nav.dagpenger.iverksett.kontrakter.iverksett.AktivitetType
import no.nav.dagpenger.iverksett.kontrakter.iverksett.VedtaksperiodeType
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Tilbakekrevingsvalg
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class IverksettOvergangsstønad(
    val fagsak: Fagsakdetaljer,
    val behandling: Behandlingsdetaljer,
    val søker: Søker,
    val vedtak: VedtaksdetaljerOvergangsstønad,
) {

    fun erGOmregning() = behandling.behandlingÅrsak == BehandlingÅrsak.G_OMREGNING

    fun erMigrering() = behandling.behandlingÅrsak == BehandlingÅrsak.MIGRERING

    fun erKorrigeringUtenBrev() = behandling.behandlingÅrsak == BehandlingÅrsak.KORRIGERING_UTEN_BREV

    fun erSatsendring() = behandling.behandlingÅrsak == BehandlingÅrsak.SATSENDRING

    fun skalIkkeSendeBrev() = erMigrering() || erGOmregning() || erKorrigeringUtenBrev() || erSatsendring()

    fun medNyTilbakekreving(nyTilbakekreving: Tilbakekrevingsdetaljer?): IverksettOvergangsstønad {
        return this.copy(vedtak = this.vedtak.copy(tilbakekreving = nyTilbakekreving))
    }
}

data class Fagsakdetaljer(
    val fagsakId: UUID,
    val eksternId: Long,
    val stønadstype: StønadType,
)

data class Søker(
    val personIdent: String,
    val barn: List<Barn> = ArrayList(),
    val tilhørendeEnhet: String,
    val adressebeskyttelse: AdressebeskyttelseGradering? = null,
)

sealed class Vedtaksperiode

data class VedtaksperiodeOvergangsstønad(
    @Deprecated("Bruk periode.", ReplaceWith("periode.fom")) val fraOgMed: LocalDate? = null,
    @Deprecated("Bruk periode.", ReplaceWith("periode.tom")) val tilOgMed: LocalDate? = null,
    val periode: Månedsperiode = Månedsperiode(
        fraOgMed ?: error("Minst en av fraOgMed og periode.fom må ha verdi."),
        tilOgMed ?: error("Minst en av tilOgMed og periode.tom må ha verdi."),
    ),
    val aktivitet: AktivitetType,
    val periodeType: VedtaksperiodeType,
) : Vedtaksperiode()

data class VedtaksperiodeBarnetilsyn(
    @Deprecated("Bruk periode.", ReplaceWith("periode.fom")) val fraOgMed: LocalDate? = null,
    @Deprecated("Bruk periode.", ReplaceWith("periode.tom")) val tilOgMed: LocalDate? = null,
    val periode: Månedsperiode = Månedsperiode(
        fraOgMed ?: error("Minst en av fraOgMed og periode.fom må ha verdi."),
        tilOgMed ?: error("Minst en av tilOgMed og periode.tom må ha verdi."),
    ),
    val utgifter: Int,
    val antallBarn: Int,
) : Vedtaksperiode()

data class PeriodeMedBeløp(
    @Deprecated("Bruk periode.", ReplaceWith("periode.fom")) val fraOgMed: LocalDate? = null,
    @Deprecated("Bruk periode.", ReplaceWith("periode.tom")) val tilOgMed: LocalDate? = null,
    val periode: Månedsperiode = Månedsperiode(
        fraOgMed ?: error("Minst en av fraOgMed og periode.fom må ha verdi."),
        tilOgMed ?: error("Minst en av tilOgMed og periode.tom må ha verdi."),
    ),
    val beløp: Int,
)

sealed class Vedtaksdetaljer {

    abstract val vedtaksresultat: Vedtaksresultat
    abstract val vedtakstidspunkt: LocalDateTime
    abstract val opphørÅrsak: OpphørÅrsak?
    abstract val saksbehandlerId: String
    abstract val beslutterId: String
    abstract val tilkjentYtelse: TilkjentYtelse?
    abstract val tilbakekreving: Tilbakekrevingsdetaljer?
    abstract val brevmottakere: Brevmottakere?
    abstract val vedtaksperioder: List<Vedtaksperiode>
    abstract val avslagÅrsak: AvslagÅrsak?
}

data class VedtaksdetaljerOvergangsstønad(
    override val vedtaksresultat: Vedtaksresultat,
    override val vedtakstidspunkt: LocalDateTime,
    override val opphørÅrsak: OpphørÅrsak?,
    override val saksbehandlerId: String,
    override val beslutterId: String,
    override val tilkjentYtelse: TilkjentYtelse?,
    override val tilbakekreving: Tilbakekrevingsdetaljer? = null,
    override val brevmottakere: Brevmottakere? = null,
    override val vedtaksperioder: List<VedtaksperiodeOvergangsstønad> = listOf(),
    override val avslagÅrsak: AvslagÅrsak? = null,
) : Vedtaksdetaljer()

data class VedtaksdetaljerBarnetilsyn(
    override val vedtaksresultat: Vedtaksresultat,
    override val vedtakstidspunkt: LocalDateTime,
    override val opphørÅrsak: OpphørÅrsak?,
    override val saksbehandlerId: String,
    override val beslutterId: String,
    override val tilkjentYtelse: TilkjentYtelse?,
    override val tilbakekreving: Tilbakekrevingsdetaljer? = null,
    override val brevmottakere: Brevmottakere? = null,
    override val vedtaksperioder: List<VedtaksperiodeBarnetilsyn> = listOf(),
    override val avslagÅrsak: AvslagÅrsak? = null,
    val kontantstøtte: List<PeriodeMedBeløp> = listOf(),
    val tilleggsstønad: List<PeriodeMedBeløp> = listOf(),
) : Vedtaksdetaljer()

data class Behandlingsdetaljer(
    val forrigeBehandlingId: UUID? = null,
    val behandlingId: UUID,
    val eksternId: Long,
    val behandlingType: BehandlingType,
    val behandlingÅrsak: BehandlingÅrsak,
    val relatertBehandlingId: UUID? = null,
    val vilkårsvurderinger: List<Vilkårsvurdering> = emptyList(),
    val aktivitetspliktInntrefferDato: LocalDate? = null,
    val kravMottatt: LocalDate? = null,
    val årsakRevurdering: ÅrsakRevurdering? = null,
)

data class ÅrsakRevurdering(
    val opplysningskilde: Opplysningskilde,
    val årsak: Revurderingsårsak,
)

data class Vilkårsvurdering(
    val vilkårType: VilkårType,
    val resultat: Vilkårsresultat,
    val delvilkårsvurderinger: List<Delvilkårsvurdering> = emptyList(),
)

data class Delvilkårsvurdering(
    val resultat: Vilkårsresultat,
    val vurderinger: List<Vurdering> = emptyList(),
)

data class Vurdering(
    val regelId: RegelId,
    val svar: SvarId? = null,
    val begrunnelse: String? = null,
)

data class Tilbakekrevingsdetaljer(
    val tilbakekrevingsvalg: Tilbakekrevingsvalg,
    val tilbakekrevingMedVarsel: TilbakekrevingMedVarsel?,
)

data class TilbakekrevingMedVarsel(
    val varseltekst: String,
    val sumFeilutbetaling: BigDecimal?,
    val perioder: List<Datoperiode>?,
)
