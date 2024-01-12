package no.nav.dagpenger.iverksett.utbetaling.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID
import no.nav.dagpenger.iverksett.utbetaling.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.utbetaling.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksettingsresultat
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.domene.Søker
import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.domene.Vedtaksdetaljer
import no.nav.dagpenger.iverksett.utbetaling.domene.Vedtaksperiode
import no.nav.dagpenger.kontrakter.felles.BrukersNavKontor
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.felles.Personident
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeType
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat

fun opprettIverksettDto(
    behandlingId: UUID = UUID.randomUUID(),
    sakId: UUID = UUID.randomUUID(),
    andelsbeløp: Int = 500,
    vedtaksresultat: Vedtaksresultat = Vedtaksresultat.INNVILGET,
    stønadType: StønadType = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
    ferietillegg: Ferietillegg? = null,
    vedtaksperioder: List<VedtaksperiodeDto> = emptyList(),
    brukersNavKontor: BrukersNavKontor? = null,
): IverksettDto {
    val andelTilkjentYtelse = lagUtbetalingDto(
        beløp = andelsbeløp,
        fraOgMed = LocalDate.of(2021, 1, 1),
        tilOgMed = LocalDate.of(2021, 12, 31),
        stønadstype = stønadType,
        ferietillegg = ferietillegg,
    )

    return IverksettDto(
        behandlingId = behandlingId,
        sakId = sakId,
        personident = Personident("15507600333"),
        vedtak = VedtaksdetaljerDto(
            resultat = vedtaksresultat,
            vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
            saksbehandlerId = "A12345",
            beslutterId = "B23456",
            brukersNavKontor = brukersNavKontor,
            utbetalinger = listOf(andelTilkjentYtelse),
            vedtaksperioder = vedtaksperioder
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
    stønadstype = stønadstype
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
): Behandlingsdetaljer {
    return Behandlingsdetaljer(
        behandlingId = behandlingId,
        forrigeBehandlingId = forrigeBehandlingId,
        relatertBehandlingId = null,
        kravMottatt = LocalDate.of(2021, 3, 3),

    )
}

fun vedtaksperiode() =
    Vedtaksperiode(
        periode = Datoperiode(YearMonth.now().atDay(1), YearMonth.now().atEndOfMonth()),
        periodeType = VedtaksperiodeType.HOVEDPERIODE,
    )

fun vedtaksdetaljer(
        vedtaksresultat: Vedtaksresultat = Vedtaksresultat.INNVILGET,
        andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
        vedtakstidspunkt: LocalDateTime = LocalDateTime.of(2021, 5, 12, 0, 0),
        vedtaksperioder: List<Vedtaksperiode> = listOf(vedtaksperiode()),
): Vedtaksdetaljer {
    val tilkjentYtelse = lagTilkjentYtelse(andeler)
    return Vedtaksdetaljer(
        vedtaksresultat = vedtaksresultat,
        vedtakstidspunkt = vedtakstidspunkt,
        saksbehandlerId = "A12345",
        beslutterId = "B23456",
        tilkjentYtelse = tilkjentYtelse,
        vedtaksperioder = vedtaksperioder,
    )
}

private fun lagTilkjentYtelse(
        andeler: List<AndelTilkjentYtelse>,
): TilkjentYtelse =
    TilkjentYtelse(
        id = UUID.randomUUID(),
        utbetalingsoppdrag = null,
        andelerTilkjentYtelse = andeler,
    )

fun opprettIverksett(
        behandlingsdetaljer: Behandlingsdetaljer = behandlingsdetaljer(),
        vedtaksdetaljer: Vedtaksdetaljer = vedtaksdetaljer(),
) =
    Iverksetting(
        fagsak = Fagsakdetaljer(fagsakId = UUID.randomUUID(), stønadstype = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR),
        behandling = behandlingsdetaljer,
        søker = Søker(
            personident = "15507600333",
        ),
        vedtak = vedtaksdetaljer,
    )

fun opprettIverksett(
        behandlingId: UUID = UUID.randomUUID(),
        forrigeBehandlingId: UUID? = null,
        andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
        fagsakId: UUID = UUID.randomUUID(),
): Iverksetting {
    return Iverksetting(
        fagsak = Fagsakdetaljer(fagsakId = fagsakId, stønadstype = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR),
        behandling = behandlingsdetaljer(behandlingId, forrigeBehandlingId),
        søker = Søker(
            personident = "15507600333",
        ),
        vedtak = vedtaksdetaljer(
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            andeler = andeler,
        ),
        forrigeIverksettingBehandlingId = forrigeBehandlingId,
    )
}

class IverksettResultatMockBuilder {

    data class Builder(
            var oppdragResultat: OppdragResultat? = null,
    ) {

        fun oppdragResultat(oppdragResultat: OppdragResultat) = apply { this.oppdragResultat = oppdragResultat }

        fun build(behandlingId: UUID, tilkjentYtelse: TilkjentYtelse?) =
            Iverksettingsresultat(
                behandlingId,
                tilkjentYtelse,
                oppdragResultat,
            )
    }
}
