package no.nav.dagpenger.iverksett.utbetaling

import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.IverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.domene.transformer.RandomOSURId
import no.nav.dagpenger.iverksett.utbetaling.util.behandlingsdetaljer
import no.nav.dagpenger.iverksett.utbetaling.util.enIverksetting
import no.nav.dagpenger.iverksett.utbetaling.util.lagAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.util.vedtaksdetaljer
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import java.time.LocalDate
import java.time.LocalDateTime

fun lagIverksettingsdata(
    fagsystem: Fagsystem = Fagsystem.DAGPENGER,
    sakId: String? = null,
    behandlingId: String? = null,
    forrigeBehandlingId: String? = null,
    iverksettingId: String? = null,
    forrigeIverksettingId: String? = null,
    andelsdatoer: List<LocalDate> = emptyList(),
    beløp: Int = 100,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
) = enIverksetting(
    fagsystem = fagsystem,
    sakId = sakId,
    behandlingsdetaljer =
        behandlingsdetaljer(
            behandlingId = behandlingId ?: RandomOSURId.generate(),
            forrigeBehandlingId = forrigeBehandlingId,
            iverksettingId = iverksettingId,
            forrigeIverksettingId = forrigeIverksettingId,
        ),
    vedtaksdetaljer =
        vedtaksdetaljer(
            andeler =
                andelsdatoer.map {
                    lagAndelTilkjentYtelse(beløp = beløp, fraOgMed = it, tilOgMed = it)
                },
            vedtakstidspunkt = vedtakstidspunkt,
        ),
)

fun lagIverksettingEntitet(
    iverksettingData: Iverksetting,
    mottattTidspunkt: LocalDateTime = LocalDateTime.now(),
) = IverksettingEntitet(
    behandlingId = iverksettingData.behandling.behandlingId,
    data = iverksettingData,
    mottattTidspunkt = mottattTidspunkt,
)
