package no.nav.dagpenger.iverksett.infrastruktur.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.Random
import java.util.UUID
import no.nav.dagpenger.iverksett.api.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.api.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.api.domene.Iverksett
import no.nav.dagpenger.iverksett.api.domene.IverksettResultat
import no.nav.dagpenger.iverksett.api.domene.OppdragResultat
import no.nav.dagpenger.iverksett.api.domene.Søker
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.Vedtaksdetaljer
import no.nav.dagpenger.iverksett.api.domene.Vedtaksperiode
import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagAndelTilkjentYtelse
import no.nav.dagpenger.iverksett.konsumenter.økonomi.lagUtbetalingDto
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.Behandlingsinformasjon
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.iverksett.Ferietillegg
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.TilkjentYtelseDto
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeType
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat

fun opprettIverksettDto(
    behandlingId: UUID = UUID.randomUUID(),
    sakId: UUID = UUID.randomUUID(),
    andelsbeløp: Int = 500,
    vedtaksresultat: Vedtaksresultat = Vedtaksresultat.INNVILGET,
    stønadType: StønadType = StønadTypeDagpenger.DAGPENGER_ARBEIDSSOKER_ORDINAER,
    ferietillegg: Ferietillegg? = null,
    vedtaksperioder: List<VedtaksperiodeDto> = emptyList(),
    enhet: String? = null,
): IverksettDto {
    val andelTilkjentYtelse = lagUtbetalingDto(
        beløp = andelsbeløp,
        fraOgMed = LocalDate.of(2021, 1, 1),
        tilOgMed = LocalDate.of(2021, 12, 31),
        stønadstype = stønadType,
        ferietillegg = ferietillegg,
    )
    val tilkjentYtelse = TilkjentYtelseDto(
        utbetalinger = listOf(andelTilkjentYtelse)
    )

    return IverksettDto(
        behandlingId = behandlingId,
        sakId = sakId,
        personIdent = "12345678910",
        vedtak = VedtaksdetaljerDto(
            resultat = vedtaksresultat,
            vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
            saksbehandlerId = "A12345",
            beslutterId = "B23456",
            enhet = enhet,
            utbetalinger = tilkjentYtelse.utbetalinger,
            vedtaksperioder = vedtaksperioder,
            vedtakstype = VedtakType.RAMMEVEDTAK
        ),
    )
}

fun opprettAndelTilkjentYtelse(
    beløp: Int = 5000,
    fra: LocalDate = LocalDate.of(2021, 1, 1),
    til: LocalDate = LocalDate.of(2021, 12, 31),
) = lagAndelTilkjentYtelse(
    beløp = beløp,
    fraOgMed = fra,
    tilOgMed = til,
)

private val eksternIdGenerator = Random()

fun opprettBehandlingsinformasjon(
    behandlingId: UUID = UUID.randomUUID(),
): Behandlingsinformasjon {
    return Behandlingsinformasjon(
        saksbehandlerId = "saksbehandlerId",
        fagsakId = UUID.randomUUID(),
        behandlingId = behandlingId.toString(),
        personIdent = "12345678910",
        vedtaksdato = LocalDate.of(2021, 1, 1),
    )
}

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
        vedtakstype = VedtakType.UTBETALINGSVEDTAK,
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
    Iverksett(
        fagsak = Fagsakdetaljer(fagsakId = UUID.randomUUID(), stønadstype = StønadTypeDagpenger.DAGPENGER_ARBEIDSSOKER_ORDINAER),
        behandling = behandlingsdetaljer,
        søker = Søker(
            personIdent = "12345678910",
        ),
        vedtak = vedtaksdetaljer,
    )

fun opprettIverksett(
    behandlingId: UUID = UUID.randomUUID(),
    forrigeBehandlingId: UUID? = null,
    andeler: List<AndelTilkjentYtelse> = listOf(opprettAndelTilkjentYtelse()),
    forrigeIverksetting: Iverksett? = null,
    fagsakId: UUID = UUID.randomUUID(),
): Iverksett {
    return Iverksett(
        fagsak = Fagsakdetaljer(fagsakId = fagsakId, stønadstype = StønadTypeDagpenger.DAGPENGER_ARBEIDSSOKER_ORDINAER),
        behandling = behandlingsdetaljer(behandlingId, forrigeBehandlingId),
        søker = Søker(
            personIdent = "12345678910",
        ),
        vedtak = vedtaksdetaljer(
            vedtaksresultat = Vedtaksresultat.INNVILGET,
            andeler = andeler,
        ),
        forrigeIverksetting = forrigeIverksetting,
    )
}

class IverksettResultatMockBuilder private constructor(
    val tilkjentYtelse: TilkjentYtelse,
) {

    data class Builder(
        var oppdragResultat: OppdragResultat? = null,
    ) {

        fun oppdragResultat(oppdragResultat: OppdragResultat) = apply { this.oppdragResultat = oppdragResultat }

        fun build(behandlingId: UUID, tilkjentYtelse: TilkjentYtelse?) =
            IverksettResultat(
                behandlingId,
                tilkjentYtelse,
                oppdragResultat,
            )
    }
}
