package no.nav.dagpenger.iverksett.kontrakter.infotrygd

import no.nav.dagpenger.kontrakter.utbetaling.StønadType

data class InfotrygdFinnesResponse(
    val vedtak: List<Vedtakstreff>,
    val saker: List<Saktreff>,
)

data class Vedtakstreff(val personIdent: String, val stønadType: StønadType, val harLøpendeVedtak: Boolean)
data class Saktreff(val personIdent: String, val stønadType: StønadType)
