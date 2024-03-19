package no.nav.dagpenger.iverksett.utbetaling.domene.transformer

import no.nav.dagpenger.iverksett.utbetaling.domene.AndelTilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.domene.Behandlingsdetaljer
import no.nav.dagpenger.iverksett.utbetaling.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.StønadsdataTilleggsstønader
import no.nav.dagpenger.iverksett.utbetaling.domene.Søker
import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.domene.Vedtaksdetaljer
import no.nav.dagpenger.kontrakter.felles.Datoperiode
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.iverksett.IverksettTilleggsstønaderDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingTilleggsstønaderDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerTilleggsstønaderDto

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
        periode = Datoperiode(this.fraOgMedDato, this.tilOgMedDato),
        stønadsdata =
            StønadsdataTilleggsstønader(
                brukersNavKontor = this.brukersNavKontor,
                stønadstype = this.stønadstype,
            ),
    )
