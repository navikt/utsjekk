package no.nav.dagpenger.iverksett.kontrakter.iverksett

import no.nav.dagpenger.iverksett.kontrakter.felles.AvslagÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingType
import no.nav.dagpenger.iverksett.kontrakter.felles.BehandlingÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.Datoperiode
import no.nav.dagpenger.iverksett.kontrakter.felles.OpphørÅrsak
import no.nav.dagpenger.iverksett.kontrakter.felles.RegelId
import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import no.nav.dagpenger.iverksett.kontrakter.felles.SvarId
import no.nav.dagpenger.iverksett.kontrakter.felles.Vedtaksresultat
import no.nav.dagpenger.iverksett.kontrakter.felles.VilkårType
import no.nav.dagpenger.iverksett.kontrakter.felles.Vilkårsresultat
import no.nav.dagpenger.iverksett.kontrakter.tilbakekreving.Tilbakekrevingsvalg
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class IverksettDagpengerdDto(
    val fagsak: FagsakdetaljerDto,
    val behandling: BehandlingsdetaljerDto,
    val søker: SøkerDto,
    val vedtak: VedtaksdetaljerDagpengerDto,
    val forrigeVedtak: VedtaksdetaljerDagpengerDto? = null
)

data class SøkerDto(
    val personIdent: String,
    val barn: List<BarnDto> = emptyList(),
    val tilhørendeEnhet: String,
    val adressebeskyttelse: AdressebeskyttelseGradering? = null,
)

data class FagsakdetaljerDto(
    val fagsakId: UUID,
    val eksternId: Long,
    val stønadstype: StønadType,
)

data class BehandlingsdetaljerDto(
    val behandlingId: UUID,
    val forrigeBehandlingId: UUID? = null,
    val eksternId: Long,
    val behandlingType: BehandlingType,
    val behandlingÅrsak: BehandlingÅrsak,
    val vilkårsvurderinger: List<VilkårsvurderingDto> = emptyList(),
    val aktivitetspliktInntrefferDato: LocalDate? = null,
    val kravMottatt: LocalDate? = null,
    val årsakRevurdering: ÅrsakRevurderingDto? = null,
)

data class VedtaksdetaljerDagpengerDto(
    val vedtakstidspunkt: LocalDateTime,
    val resultat: Vedtaksresultat,
    val opphørÅrsak: OpphørÅrsak?,
    val avslagÅrsak: AvslagÅrsak? = null,
    val saksbehandlerId: String,
    val beslutterId: String,
    val utbetalinger: List<UtbetalingDto> = emptyList(),
    val vedtaksperioder: List<VedtaksperiodeDagpengerDto> = emptyList(),
    val tilbakekreving: TilbakekrevingDto? = null,
    val brevmottakere: List<Brevmottaker> = emptyList(),
)

data class VilkårsvurderingDto(
    val vilkårType: VilkårType,
    val resultat: Vilkårsresultat,
    val delvilkårsvurderinger: List<DelvilkårsvurderingDto> = emptyList(),
)

data class DelvilkårsvurderingDto(
    val resultat: Vilkårsresultat,
    val vurderinger: List<VurderingDto> = emptyList(),
)

data class VurderingDto(
    val regelId: RegelId,
    val svar: SvarId? = null,
    val begrunnelse: String? = null
)
data class VedtaksperiodeDagpengerDto(
    val periode: Datoperiode,
    val aktivitet: AktivitetType,
    val periodeType: VedtaksperiodeType,
)

data class TilbakekrevingDto(
    val tilbakekrevingsvalg: Tilbakekrevingsvalg,
    val tilbakekrevingMedVarsel: TilbakekrevingMedVarselDto?,
)

data class TilbakekrevingMedVarselDto(
    val varseltekst: String,
    val sumFeilutbetaling: BigDecimal? = null, // Hentes fra simulering hvis det mangler
    val fellesperioder: List<Datoperiode> = emptyList()
) // Hentes fra simulering hvis det mangler

enum class AdressebeskyttelseGradering {
    STRENGT_FORTROLIG,
    STRENGT_FORTROLIG_UTLAND,
    FORTROLIG,
    UGRADERT,
}

enum class IverksettStatus {
    SENDT_TIL_OPPDRAG,
    FEILET_MOT_OPPDRAG,
    OK_MOT_OPPDRAG,
    JOURNALFØRT,
    OK,
    IKKE_PÅBEGYNT,
}

data class IverksettStatusDto(
    val status: IverksettStatus,
    val feilmelding: String? = null
) {
    fun ok() = IverksettStatusDto(IverksettStatus.OK)
}

fun IverksettStatus.medFeil(feilmelding: String) = IverksettStatusDto(
    status = this,
    feilmelding = feilmelding
)

fun IverksettStatus.utenFeil() = IverksettStatusDto(this)

enum class VedtaksperiodeType {
    MIGRERING,
    FORLENGELSE,
    HOVEDPERIODE,
    UTVIDELSE,
    SANKSJON
}

enum class AktivitetType {
    MIGRERING,
    IKKE_AKTIVITETSPLIKT,
    BARN_UNDER_ETT_ÅR,
    FORSØRGER_I_ARBEID,
    FORSØRGER_I_UTDANNING,
    FORSØRGER_REELL_ARBEIDSSØKER,
    FORSØRGER_ETABLERER_VIRKSOMHET,
    BARNET_SÆRLIG_TILSYNSKREVENDE,
    FORSØRGER_MANGLER_TILSYNSORDNING,
    FORSØRGER_ER_SYK,
    BARNET_ER_SYKT,
    UTVIDELSE_FORSØRGER_I_UTDANNING,
    UTVIDELSE_BARNET_SÆRLIG_TILSYNSKREVENDE,
    FORLENGELSE_MIDLERTIDIG_SYKDOM,
    FORLENGELSE_STØNAD_PÅVENTE_ARBEID,
    FORLENGELSE_STØNAD_PÅVENTE_ARBEID_REELL_ARBEIDSSØKER,
    FORLENGELSE_STØNAD_PÅVENTE_OPPSTART_KVALIFISERINGSPROGRAM,
    FORLENGELSE_STØNAD_PÅVENTE_TILSYNSORDNING,
    FORLENGELSE_STØNAD_PÅVENTE_UTDANNING,
    FORLENGELSE_STØNAD_UT_SKOLEÅRET,
}

// Brukes også fra [FrittståendeBrevDto]
data class Brevmottaker(
    val ident: String,
    val navn: String,
    val mottakerRolle: MottakerRolle,
    val identType: IdentType,
) {

    enum class MottakerRolle {
        BRUKER,
        VERGE,
        FULLMEKTIG,
    }

    enum class IdentType {
        PERSONIDENT,
        ORGANISASJONSNUMMER,
    }
}
