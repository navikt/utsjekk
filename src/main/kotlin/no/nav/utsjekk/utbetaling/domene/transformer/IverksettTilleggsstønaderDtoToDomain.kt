package no.nav.utsjekk.utbetaling.domene.transformer

import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.iverksett.IverksettTilleggsstønaderDto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingTilleggsstønaderDto
import no.nav.utsjekk.kontrakter.iverksett.VedtaksdetaljerTilleggsstønaderDto
import no.nav.utsjekk.utbetaling.domene.AndelTilkjentYtelse
import no.nav.utsjekk.utbetaling.domene.Behandlingsdetaljer
import no.nav.utsjekk.utbetaling.domene.Fagsakdetaljer
import no.nav.utsjekk.utbetaling.domene.Iverksetting
import no.nav.utsjekk.utbetaling.domene.Periode
import no.nav.utsjekk.utbetaling.domene.StønadsdataTilleggsstønader
import no.nav.utsjekk.utbetaling.domene.Søker
import no.nav.utsjekk.utbetaling.domene.TilkjentYtelse
import no.nav.utsjekk.utbetaling.domene.Vedtaksdetaljer

fun IverksettTilleggsstønaderDto.toDomain(): Iverksetting =
    Iverksetting(
        fagsak = this.tilFagsak(),
        søker = Søker(personident = this.personident.verdi),
        behandling = this.tilBehandling(),
        vedtak = this.vedtak.toDomain(),
    )

fun IverksettTilleggsstønaderDto.tilFagsak(): Fagsakdetaljer {
    return Fagsakdetaljer(
        fagsakId = this.sakId,
        fagsystem = Fagsystem.TILLEGGSSTØNADER,
    )
}

fun IverksettTilleggsstønaderDto.tilBehandling() =
    Behandlingsdetaljer(
        behandlingId = this.behandlingId,
        forrigeBehandlingId = this.forrigeIverksetting?.behandlingId,
        iverksettingId = this.iverksettingId,
        forrigeIverksettingId = this.forrigeIverksetting?.iverksettingId,
    )

fun VedtaksdetaljerTilleggsstønaderDto.toDomain() =
    Vedtaksdetaljer(
        vedtakstidspunkt = this.vedtakstidspunkt,
        saksbehandlerId = this.saksbehandlerId,
        beslutterId = this.beslutterId,
        tilkjentYtelse = this.utbetalinger.tilTilkjentYtelse(),
    )

fun List<UtbetalingTilleggsstønaderDto>.tilTilkjentYtelse(): TilkjentYtelse {
    val andeler = this.map { it.toDomain() }

    return when (andeler.size) {
        0 -> tomTilkjentYtelse()
        else -> TilkjentYtelse(andelerTilkjentYtelse = andeler)
    }
}

fun UtbetalingTilleggsstønaderDto.toDomain() =
    AndelTilkjentYtelse(
        beløp = this.beløp,
        satstype = this.satstype,
        periode = Periode(this.fraOgMedDato, this.tilOgMedDato),
        stønadsdata =
            StønadsdataTilleggsstønader(
                brukersNavKontor = this.brukersNavKontor,
                stønadstype = this.stønadstype,
            ),
    )
