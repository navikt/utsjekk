package no.nav.dagpenger.iverksett.utbetaling.util

import no.nav.dagpenger.iverksett.utbetaling.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.utbetaling.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksettingsresultat
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.domene.Søker
import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.domene.Vedtaksdetaljer
import no.nav.dagpenger.kontrakter.felles.BrukersNavKontor
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.GeneriskId
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.felles.Personident
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataDagpengerDto
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataTiltakspengerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

fun opprettIverksettDto(
    behandlingId: GeneriskId = GeneriskIdSomUUID(UUID.randomUUID()),
    sakId: GeneriskId = GeneriskIdSomUUID(UUID.randomUUID()),
    andelsbeløp: Int = 500,
    stønadType: StønadType = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
    ferietillegg: Ferietillegg? = null,
    brukersNavKontor: BrukersNavKontor? = null,
): IverksettDto {
    val stønadsdata =
        when (stønadType) {
            is StønadTypeDagpenger ->
                StønadsdataDagpengerDto(
                    stønadstype = stønadType,
                    ferietillegg = ferietillegg,
                )
            is StønadTypeTiltakspenger ->
                StønadsdataTiltakspengerDto(
                    stønadstype = stønadType,
                )
            else -> throw UnsupportedOperationException("Støtter ikke opprettelse av IverksettDto for tilleggsstønader")
        }
    val andelTilkjentYtelse =
        lagUtbetalingDto(
            beløp = andelsbeløp,
            fraOgMed = LocalDate.of(2021, 1, 1),
            tilOgMed = LocalDate.of(2021, 12, 31),
            stønadsdata = stønadsdata,
        )

    return IverksettDto(
        behandlingId = behandlingId,
        sakId = sakId,
        personident = Personident("15507600333"),
        vedtak =
            VedtaksdetaljerDto(
                vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
                saksbehandlerId = "A12345",
                beslutterId = "B23456",
                brukersNavKontor = brukersNavKontor,
                utbetalinger = listOf(andelTilkjentYtelse),
            ),
    )
}

fun opprettAndelTilkjentYtelse(
    beløp: Int = 5000,
    fra: LocalDate = LocalDate.of(2021, 1, 1),
    til: LocalDate = LocalDate.of(2021, 12, 31),
    stønadstype: StønadType = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
) = lagAndelTilkjentYtelse(
    beløp = beløp,
    fraOgMed = fra,
    tilOgMed = til,
    stønadstype = stønadstype,
)

fun opprettTilkjentYtelse(
    behandlingId: UUID = UUID.randomUUID(),
    andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
    sisteAndelIKjede: AndelTilkjentYtelse? = null,
): TilkjentYtelse {
    return TilkjentYtelse(
        id = behandlingId,
        utbetalingsoppdrag = null,
        andelerTilkjentYtelse = andeler,
        sisteAndelIKjede = sisteAndelIKjede,
    )
}

fun behandlingsdetaljer(
    behandlingId: UUID = UUID.randomUUID(),
    forrigeBehandlingId: UUID? = null,
    iverksettingId: String? = null,
    forrigeIverksettingId: String? = null,
): Behandlingsdetaljer {
    return Behandlingsdetaljer(
        behandlingId = GeneriskIdSomUUID(behandlingId),
        forrigeBehandlingId = forrigeBehandlingId?.let { GeneriskIdSomUUID(it) },
        iverksettingId = iverksettingId,
        forrigeIverksettingId = forrigeIverksettingId,
    )
}

fun vedtaksdetaljer(
    andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
    vedtakstidspunkt: LocalDateTime = LocalDateTime.of(2021, 5, 12, 0, 0),
): Vedtaksdetaljer {
    val tilkjentYtelse = lagTilkjentYtelse(andeler)
    return Vedtaksdetaljer(
        vedtakstidspunkt = vedtakstidspunkt,
        saksbehandlerId = "A12345",
        beslutterId = "B23456",
        tilkjentYtelse = tilkjentYtelse,
    )
}

private fun lagTilkjentYtelse(andeler: List<AndelTilkjentYtelse>): TilkjentYtelse =
    TilkjentYtelse(
        id = UUID.randomUUID(),
        utbetalingsoppdrag = null,
        andelerTilkjentYtelse = andeler,
    )

fun opprettIverksett(
    fagsystem: Fagsystem = Fagsystem.DAGPENGER,
    sakId: UUID? = null,
    behandlingsdetaljer: Behandlingsdetaljer = behandlingsdetaljer(),
    vedtaksdetaljer: Vedtaksdetaljer = vedtaksdetaljer(),
) = Iverksetting(
    fagsak =
        Fagsakdetaljer(
            fagsakId = GeneriskIdSomUUID(sakId ?: UUID.randomUUID()),
            fagsystem = fagsystem,
        ),
    behandling = behandlingsdetaljer,
    søker =
        Søker(
            personident = "15507600333",
        ),
    vedtak = vedtaksdetaljer,
)

fun opprettIverksett(
    behandlingId: UUID = UUID.randomUUID(),
    forrigeBehandlingId: UUID? = null,
    andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
    fagsakId: UUID = UUID.randomUUID(),
    iverksettingId: String? = null,
    forrigeIverksettingId: String? = null,
): Iverksetting {
    val fagsystem = andeler.firstOrNull()?.stønadsdata?.stønadstype?.tilFagsystem() ?: Fagsystem.DAGPENGER
    return Iverksetting(
        fagsak = Fagsakdetaljer(fagsakId = GeneriskIdSomUUID(fagsakId), fagsystem = fagsystem),
        behandling = behandlingsdetaljer(behandlingId, forrigeBehandlingId, iverksettingId, forrigeIverksettingId),
        søker =
            Søker(
                personident = "15507600333",
            ),
        vedtak =
            vedtaksdetaljer(
                andeler = andeler,
            ),
        forrigeIverksettingBehandlingId = forrigeBehandlingId?.let { GeneriskIdSomUUID(it) },
    )
}

class IverksettResultatMockBuilder {
    data class Builder(
        var oppdragResultat: OppdragResultat? = null,
    ) {
        fun oppdragResultat(oppdragResultat: OppdragResultat) = apply { this.oppdragResultat = oppdragResultat }

        fun build(
            fagsystem: Fagsystem,
            behandlingId: UUID,
            tilkjentYtelse: TilkjentYtelse?,
            iverksettingId: String? = null,
        ) = Iverksettingsresultat(
            fagsystem,
            behandlingId,
            iverksettingId,
            tilkjentYtelse,
            oppdragResultat,
        )
    }
}
