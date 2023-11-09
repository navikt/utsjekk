package no.nav.dagpenger.iverksett.api.domene

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.felles.SakIdentifikator
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeType
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat

data class Iverksett(
    val fagsak: Fagsakdetaljer,
    val behandling: Behandlingsdetaljer,
    val søker: Søker,
    val vedtak: Vedtaksdetaljer,
    val forrigeIverksetting: Iverksett? = null,
)

data class Fagsakdetaljer(
    val fagsakId: UUID? = null,
    val saksreferanse: String? = null,
    val stønadstype: StønadType = StønadTypeDagpenger.DAGPENGER_ARBEIDSSOKER_ORDINAER,
) {
    init {
        SakIdentifikator.valider(fagsakId, saksreferanse)
    }
}

data class Søker(
    val personIdent: String,
)

data class Vedtaksperiode(
    val periode: Datoperiode,
    val periodeType: VedtaksperiodeType,
)

data class Vedtaksdetaljer(
    val vedtakstype: VedtakType = VedtakType.RAMMEVEDTAK,
    val vedtaksresultat: Vedtaksresultat,
    val vedtakstidspunkt: LocalDateTime,
    val saksbehandlerId: String,
    val beslutterId: String,
    val enhet: String? = null,
    val tilkjentYtelse: TilkjentYtelse?,
    val vedtaksperioder: List<Vedtaksperiode> = listOf(),
)

data class Behandlingsdetaljer(
    val forrigeBehandlingId: UUID? = null,
    val behandlingId: UUID,
    val relatertBehandlingId: UUID? = null,
    val aktivitetspliktInntrefferDato: LocalDate? = null,
    val kravMottatt: LocalDate? = null,
)

val Iverksett.sakId get() = this.fagsak.fagsakId
val Iverksett.personIdent get() = this.søker.personIdent

val Iverksett.behandlingId get() = this.behandling.behandlingId
