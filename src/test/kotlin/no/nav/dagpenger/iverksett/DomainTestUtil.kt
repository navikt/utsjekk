package no.nav.dagpenger.iverksett

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.dagpenger.iverksett.api.domene.Iverksett
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.VedtaksperiodeDagpenger
import no.nav.dagpenger.iverksett.api.domene.behandlingId
import no.nav.dagpenger.iverksett.infrastruktur.util.behandlingsdetaljer
import no.nav.dagpenger.iverksett.infrastruktur.util.opprettIverksettDagpenger
import no.nav.dagpenger.iverksett.infrastruktur.util.vedtaksdetaljerDagpenger
import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagAndelTilkjentYtelse
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat

fun Int.januar(år: Int) = LocalDate.of(år, 1, this)
fun Int.mai(år: Int) = LocalDate.of(år, 5, this)

fun lagIverksettData(
    forrigeIverksetting: IverksettDagpenger? = null,
    forrigeBehandlingId: UUID? = forrigeIverksetting?.behandlingId,
    vedtaksresultat: Vedtaksresultat = Vedtaksresultat.INNVILGET,
    vedtaksperioder: List<VedtaksperiodeDagpenger> = emptyList(),
    andelsdatoer: List<LocalDate> = emptyList(),
    beløp: Int = 100,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
): IverksettDagpenger {
    return opprettIverksettDagpenger(
        behandlingsdetaljer = behandlingsdetaljer(
            forrigeBehandlingId = forrigeBehandlingId,
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

fun lagIverksett(iverksettData: IverksettDagpenger) = Iverksett(
    iverksettData.behandling.behandlingId,
    iverksettData,
)
