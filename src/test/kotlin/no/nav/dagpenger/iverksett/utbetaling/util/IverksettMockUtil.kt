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
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsoppdrag
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

fun enIverksettDto(
    behandlingId: GeneriskId = GeneriskIdSomUUID(UUID.randomUUID()),
    sakId: GeneriskId = GeneriskIdSomUUID(UUID.randomUUID()),
    andelsbeløp: Int = 500,
    stønadType: StønadType = StønadTypeDagpenger.DAGPENGER_ARBEIDSSØKER_ORDINÆR,
    ferietillegg: Ferietillegg? = null,
    brukersNavKontor: BrukersNavKontor? = null,
) = IverksettDto(
    behandlingId = behandlingId,
    sakId = sakId,
    personident = Personident("15507600333"),
    vedtak =
        VedtaksdetaljerDto(
            vedtakstidspunkt = LocalDateTime.of(2021, 5, 12, 0, 0),
            saksbehandlerId = "A12345",
            beslutterId = "B23456",
            brukersNavKontor = brukersNavKontor,
            utbetalinger =
                listOf(
                    lagUtbetalingDto(
                        beløp = andelsbeløp,
                        fraOgMed = LocalDate.of(2021, 1, 1),
                        tilOgMed = LocalDate.of(2021, 12, 31),
                        stønadsdata = stønadsdata(stønadType, ferietillegg),
                    ),
                ),
        ),
)

private fun stønadsdata(
    stønadType: StønadType,
    ferietillegg: Ferietillegg?,
) = when (stønadType) {
    is StønadTypeDagpenger -> StønadsdataDagpengerDto(stønadType, ferietillegg)
    is StønadTypeTiltakspenger -> StønadsdataTiltakspengerDto(stønadType)
    else -> throw UnsupportedOperationException("Støtter ikke opprettelse av IverksettDto for tilleggsstønader")
}

fun enAndelTilkjentYtelse(
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

fun enTilkjentYtelse(
    behandlingId: UUID = UUID.randomUUID(),
    andeler: List<AndelTilkjentYtelse> = listOf(enAndelTilkjentYtelse()),
    sisteAndelIKjede: AndelTilkjentYtelse? = null,
    utbetalingsoppdrag: Utbetalingsoppdrag? = null,
) = TilkjentYtelse(
    id = behandlingId,
    utbetalingsoppdrag = utbetalingsoppdrag,
    andelerTilkjentYtelse = andeler,
    sisteAndelIKjede = sisteAndelIKjede,
)

fun behandlingsdetaljer(
    behandlingId: UUID = UUID.randomUUID(),
    forrigeBehandlingId: UUID? = null,
    iverksettingId: String? = null,
    forrigeIverksettingId: String? = null,
) = Behandlingsdetaljer(
    behandlingId = GeneriskIdSomUUID(behandlingId),
    forrigeBehandlingId = forrigeBehandlingId?.let { GeneriskIdSomUUID(it) },
    iverksettingId = iverksettingId,
    forrigeIverksettingId = forrigeIverksettingId,
)

fun vedtaksdetaljer(
    andeler: List<AndelTilkjentYtelse> = listOf(enAndelTilkjentYtelse()),
    vedtakstidspunkt: LocalDateTime = LocalDateTime.of(2021, 5, 12, 0, 0),
) = Vedtaksdetaljer(
    vedtakstidspunkt = vedtakstidspunkt,
    saksbehandlerId = "A12345",
    beslutterId = "B23456",
    tilkjentYtelse = enTilkjentYtelse(andeler),
)

private fun enTilkjentYtelse(andeler: List<AndelTilkjentYtelse>): TilkjentYtelse =
    TilkjentYtelse(
        id = UUID.randomUUID(),
        utbetalingsoppdrag = null,
        andelerTilkjentYtelse = andeler,
    )

fun enIverksetting(
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

fun enIverksetting(
    behandlingId: GeneriskId = GeneriskIdSomUUID(UUID.randomUUID()),
    forrigeBehandlingId: GeneriskId? = null,
    andeler: List<AndelTilkjentYtelse> = listOf(enAndelTilkjentYtelse()),
    sakId: GeneriskId = GeneriskIdSomUUID(UUID.randomUUID()),
    iverksettingId: String? = null,
    forrigeIverksettingId: String? = null,
): Iverksetting {
    val fagsystem = andeler.firstOrNull()?.stønadsdata?.stønadstype?.tilFagsystem() ?: Fagsystem.DAGPENGER

    return Iverksetting(
        fagsak = Fagsakdetaljer(fagsakId = sakId, fagsystem = fagsystem),
        behandling =
            Behandlingsdetaljer(
                behandlingId = behandlingId,
                iverksettingId = iverksettingId,
                forrigeBehandlingId = forrigeBehandlingId,
                forrigeIverksettingId = forrigeIverksettingId,
            ),
        søker =
            Søker(
                personident = "15507600333",
            ),
        vedtak =
            vedtaksdetaljer(
                andeler = andeler,
            ),
        forrigeIverksettingBehandlingId = forrigeBehandlingId,
    )
}

fun etIverksettingsresultat(
    fagsystem: Fagsystem = Fagsystem.DAGPENGER,
    sakId: GeneriskId = GeneriskIdSomUUID(UUID.randomUUID()),
    behandlingId: UUID = UUID.randomUUID(),
    tilkjentYtelse: TilkjentYtelse? = null,
    iverksettingId: String? = null,
    oppdragResultat: OppdragResultat? = null,
) = Iverksettingsresultat(
    fagsystem = fagsystem,
    sakId = sakId,
    behandlingId = behandlingId,
    iverksettingId = iverksettingId,
    tilkjentYtelseForUtbetaling = tilkjentYtelse,
    oppdragResultat = oppdragResultat,
)

fun etTomtUtbetalingsoppdrag() =
    Utbetalingsoppdrag(
        aktør = "en-aktør",
        avstemmingstidspunkt = LocalDateTime.now(),
        brukersNavKontor =
            BrukersNavKontor(
                enhet = "",
                gjelderFom = LocalDate.now(),
            ),
        kodeEndring = Utbetalingsoppdrag.KodeEndring.NY,
        fagsystem = Fagsystem.DAGPENGER,
        saksnummer = GeneriskIdSomUUID(UUID.randomUUID()),
        saksbehandlerId = "en-saksbehandler",
        utbetalingsperiode = emptyList(),
        iverksettingId = null,
    )
