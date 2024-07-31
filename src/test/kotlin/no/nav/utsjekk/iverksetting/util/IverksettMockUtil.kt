package no.nav.utsjekk.iverksetting.util

import no.nav.utsjekk.iverksetting.domene.AndelTilkjentYtelse
import no.nav.utsjekk.iverksetting.domene.Behandlingsdetaljer
import no.nav.utsjekk.iverksetting.domene.Fagsakdetaljer
import no.nav.utsjekk.iverksetting.domene.Iverksetting
import no.nav.utsjekk.iverksetting.domene.Iverksettingsresultat
import no.nav.utsjekk.iverksetting.domene.OppdragResultat
import no.nav.utsjekk.iverksetting.domene.Søker
import no.nav.utsjekk.iverksetting.domene.TilkjentYtelse
import no.nav.utsjekk.iverksetting.domene.Vedtaksdetaljer
import no.nav.utsjekk.iverksetting.domene.transformer.RandomOSURId
import no.nav.utsjekk.kontrakter.felles.BrukersNavKontor
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.felles.StønadType
import no.nav.utsjekk.kontrakter.felles.StønadTypeDagpenger
import no.nav.utsjekk.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.utsjekk.kontrakter.iverksett.Ferietillegg
import no.nav.utsjekk.kontrakter.iverksett.IverksettDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataDagpengerDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTiltakspengerDto
import no.nav.utsjekk.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.utsjekk.kontrakter.oppdrag.Utbetalingsoppdrag
import java.time.LocalDate
import java.time.LocalDateTime

fun enIverksettDto(
    behandlingId: String = RandomOSURId.generate(),
    sakId: String = RandomOSURId.generate(),
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
    behandlingId: String = RandomOSURId.generate(),
    andeler: List<AndelTilkjentYtelse> = listOf(enAndelTilkjentYtelse()),
    sisteAndelIKjede: AndelTilkjentYtelse? = null,
    utbetalingsoppdrag: Utbetalingsoppdrag? = null,
) = TilkjentYtelse(
    id = behandlingId,
    utbetalingsoppdrag = utbetalingsoppdrag,
    andelerTilkjentYtelse = andeler,
    sisteAndelIKjede = sisteAndelIKjede,
    sisteAndelPerKjede = andeler.firstOrNull()?.let { mapOf(it.stønadsdata.tilKjedenøkkel() to it) } ?: emptyMap(),
)

fun behandlingsdetaljer(
    behandlingId: String = RandomOSURId.generate(),
    forrigeBehandlingId: String? = null,
    iverksettingId: String? = null,
    forrigeIverksettingId: String? = null,
) = Behandlingsdetaljer(
    behandlingId = behandlingId,
    forrigeBehandlingId = forrigeBehandlingId,
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
        id = RandomOSURId.generate(),
        utbetalingsoppdrag = null,
        andelerTilkjentYtelse = andeler,
    )

fun enIverksetting(
    fagsystem: Fagsystem = Fagsystem.DAGPENGER,
    sakId: String? = null,
    behandlingsdetaljer: Behandlingsdetaljer = behandlingsdetaljer(),
    vedtaksdetaljer: Vedtaksdetaljer = vedtaksdetaljer(),
) = Iverksetting(
    fagsak =
        Fagsakdetaljer(
            fagsakId = sakId ?: RandomOSURId.generate(),
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
    behandlingId: String = RandomOSURId.generate(),
    forrigeBehandlingId: String? = null,
    andeler: List<AndelTilkjentYtelse> = listOf(enAndelTilkjentYtelse()),
    sakId: String = RandomOSURId.generate(),
    iverksettingId: String? = null,
    forrigeIverksettingId: String? = null,
): Iverksetting {
    val fagsystem =
        andeler
            .firstOrNull()
            ?.stønadsdata
            ?.stønadstype
            ?.tilFagsystem() ?: Fagsystem.DAGPENGER

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
    )
}

fun etIverksettingsresultat(
    fagsystem: Fagsystem = Fagsystem.DAGPENGER,
    sakId: String = RandomOSURId.generate(),
    behandlingId: String = RandomOSURId.generate(),
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
        brukersNavKontor = null,
        erFørsteUtbetalingPåSak = true,
        fagsystem = Fagsystem.DAGPENGER,
        saksnummer = RandomOSURId.generate(),
        saksbehandlerId = "en-saksbehandler",
        utbetalingsperiode = emptyList(),
        iverksettingId = null,
    )
