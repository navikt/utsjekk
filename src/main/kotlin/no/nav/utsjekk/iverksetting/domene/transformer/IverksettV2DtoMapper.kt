package no.nav.utsjekk.iverksetting.domene.transformer

import no.nav.utsjekk.iverksetting.domene.AndelTilkjentYtelse
import no.nav.utsjekk.iverksetting.domene.Behandlingsdetaljer
import no.nav.utsjekk.iverksetting.domene.Fagsakdetaljer
import no.nav.utsjekk.iverksetting.domene.Iverksetting
import no.nav.utsjekk.iverksetting.domene.Periode
import no.nav.utsjekk.iverksetting.domene.StønadsdataDagpenger
import no.nav.utsjekk.iverksetting.domene.StønadsdataTilleggsstønader
import no.nav.utsjekk.iverksetting.domene.StønadsdataTiltakspenger
import no.nav.utsjekk.iverksetting.domene.Søker
import no.nav.utsjekk.iverksetting.domene.TilkjentYtelse
import no.nav.utsjekk.iverksetting.domene.Vedtaksdetaljer
import no.nav.utsjekk.kontrakter.felles.BrukersNavKontor
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.iverksett.IverksettV2Dto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataDagpengerDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTilleggsstønaderDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTiltakspengerDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTiltakspengerV2Dto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingV2Dto
import no.nav.utsjekk.kontrakter.iverksett.VedtaksdetaljerV2Dto

object IverksettV2DtoMapper {
    fun tilDomene(
        dto: IverksettV2Dto,
        fagsystem: Fagsystem,
    ) = Iverksetting(
        fagsak = dto.tilFagsak(fagsystem),
        søker = dto.personident.verdi.tilSøker(),
        behandling = dto.tilBehandling(),
        vedtak = dto.vedtak.tilVedtaksdetaljer(),
    )

    private fun VedtaksdetaljerV2Dto.tilVedtaksdetaljer() =
        Vedtaksdetaljer(
            vedtakstidspunkt = this.vedtakstidspunkt,
            saksbehandlerId = this.saksbehandlerId,
            beslutterId = this.beslutterId,
            tilkjentYtelse = this.utbetalinger.tilTilkjentYtelse(),
        )

    private fun IverksettV2Dto.tilFagsak(fagsystem: Fagsystem) =
        Fagsakdetaljer(
            fagsakId = this.sakId,
            fagsystem = fagsystem,
        )

    private fun String.tilSøker(): Søker = Søker(personident = this)

    private fun IverksettV2Dto.tilBehandling(): Behandlingsdetaljer =
        Behandlingsdetaljer(
            behandlingId = this.behandlingId,
            iverksettingId = this.iverksettingId,
            forrigeBehandlingId = this.forrigeIverksetting?.behandlingId,
            forrigeIverksettingId = this.forrigeIverksetting?.iverksettingId,
        )

    internal fun List<UtbetalingV2Dto>.tilTilkjentYtelse(): TilkjentYtelse =
        TilkjentYtelse(andelerTilkjentYtelse = this.map { it.tilAndel() })

    private fun UtbetalingV2Dto.tilAndel(): AndelTilkjentYtelse =
        AndelTilkjentYtelse(
            beløp = this.beløp.toInt(),
            satstype = this.satstype,
            periode = Periode(this.fraOgMedDato, this.tilOgMedDato),
            stønadsdata =
                when (this.stønadsdata) {
                    is StønadsdataDagpengerDto -> (this.stønadsdata as StønadsdataDagpengerDto).tilDomene()
                    is StønadsdataTilleggsstønaderDto -> (this.stønadsdata as StønadsdataTilleggsstønaderDto).tilDomene()
                    is StønadsdataTiltakspengerDto -> (this.stønadsdata as StønadsdataTiltakspengerDto).tilDomene()
                    is StønadsdataTiltakspengerV2Dto -> (this.stønadsdata as StønadsdataTiltakspengerV2Dto).tilDomene()
                },
        )
}

private fun StønadsdataDagpengerDto.tilDomene(): StønadsdataDagpenger =
    StønadsdataDagpenger(stønadstype = this.stønadstype, ferietillegg = this.ferietillegg)

private fun StønadsdataTilleggsstønaderDto.tilDomene(): StønadsdataTilleggsstønader =
    StønadsdataTilleggsstønader(
        stønadstype = this.stønadstype,
        brukersNavKontor = this.brukersNavKontor?.let { BrukersNavKontor(enhet = it) },
    )

private fun StønadsdataTiltakspengerV2Dto.tilDomene(): StønadsdataTiltakspenger =
    StønadsdataTiltakspenger(
        stønadstype = this.stønadstype,
        barnetillegg = this.barnetillegg,
        brukersNavKontor = BrukersNavKontor(enhet = this.brukersNavKontor)
    )

private fun StønadsdataTiltakspengerDto.tilDomene(): StønadsdataTiltakspenger =
    StønadsdataTiltakspenger(
        stønadstype = this.stønadstype,
        barnetillegg = this.barnetillegg,
        brukersNavKontor = BrukersNavKontor(enhet = "DENNE_SKAL_FJERNES") // TODO: Fjern denne
    )
