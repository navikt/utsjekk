package no.nav.dagpenger.iverksett.util

import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.Vedtaksdetaljer
import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerDagpenger
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottakere

fun IverksettDagpenger.copy(vedtak: Vedtaksdetaljer): IverksettDagpenger {
    return when (this) {
        is IverksettDagpenger -> this.copy(vedtak = vedtak as VedtaksdetaljerDagpenger)
        else -> error("Ingen støtte ennå")
    }
}

fun Vedtaksdetaljer.copy(brevmottakere: Brevmottakere): Vedtaksdetaljer {
    return when (this) {
        is VedtaksdetaljerDagpenger -> this.copy(brevmottakere = brevmottakere)
        else -> error("Ingen støtte ennå")
    }
}

fun Vedtaksdetaljer.copy(tilkjentYtelse: TilkjentYtelse): Vedtaksdetaljer {
    return when (this) {
        is VedtaksdetaljerDagpenger -> this.copy(tilkjentYtelse = tilkjentYtelse)
        else -> error("Ingen støtte ennå")
    }
}
