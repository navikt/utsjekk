package no.nav.utsjekk.utbetaling.util

import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.utbetaling.domene.Iverksetting
import no.nav.utsjekk.utbetaling.domene.IverksettingEntitet
import no.nav.utsjekk.utbetaling.domene.transformer.RandomOSURId
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
