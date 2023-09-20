package no.nav.dagpenger.iverksett

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.dagpenger.iverksett.api.domene.Brev
import no.nav.dagpenger.iverksett.api.domene.Iverksett
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.VedtaksperiodeDagpenger
import no.nav.dagpenger.iverksett.api.domene.behandlingId
import no.nav.dagpenger.iverksett.infrastruktur.util.behandlingsdetaljer
import no.nav.dagpenger.iverksett.infrastruktur.util.opprettIverksettDagpenger
import no.nav.dagpenger.iverksett.infrastruktur.util.vedtaksdetaljerDagpenger
import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagAndelTilkjentYtelse
import no.nav.dagpenger.kontrakter.iverksett.BehandlingType
import no.nav.dagpenger.kontrakter.iverksett.BehandlingÅrsak
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat

fun Int.januar(år: Int) = LocalDate.of(år, 1, this)
fun Int.mai(år: Int) = LocalDate.of(år, 5, this)

fun lagIverksettData(
    forrigeIverksetting: IverksettDagpenger? = null,
    forrigeBehandlingId: UUID? = forrigeIverksetting?.behandlingId,
    behandlingType: BehandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
    vedtaksresultat: Vedtaksresultat = Vedtaksresultat.INNVILGET,
    vedtaksperioder: List<VedtaksperiodeDagpenger> = emptyList(),
    erMigrering: Boolean = false,
    andelsdatoer: List<LocalDate> = emptyList(),
    beløp: Int = 100,
    årsak: BehandlingÅrsak = BehandlingÅrsak.SØKNAD,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
): IverksettDagpenger {
    val behandlingÅrsak = if (erMigrering) BehandlingÅrsak.MIGRERING else årsak
    return opprettIverksettDagpenger(
        behandlingsdetaljer = behandlingsdetaljer(
            forrigeBehandlingId = forrigeBehandlingId,
            behandlingType = behandlingType,
            behandlingÅrsak = behandlingÅrsak,
        ),
        vedtaksdetaljer = vedtaksdetaljerDagpenger(
            vedtaksresultat = vedtaksresultat,
            vedtaksperioder = vedtaksperioder,
            andeler = andelsdatoer.map {
                lagAndelTilkjentYtelse(beløp = beløp, fraOgMed = it, tilOgMed = it)
            },
            vedtakstidspunkt = vedtakstidspunkt,
        ),
    )
}

fun lagIverksett(iverksettData: IverksettDagpenger, brev: Brev? = null) = Iverksett(
    iverksettData.behandling.behandlingId,
    iverksettData,
    brev,
)
