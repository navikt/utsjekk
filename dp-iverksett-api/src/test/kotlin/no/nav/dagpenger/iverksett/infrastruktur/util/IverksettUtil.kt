package no.nav.dagpenger.iverksett.util

import no.nav.dagpenger.iverksett.api.domene.IverksettOvergangsstønad
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.api.domene.Vedtaksdetaljer
import no.nav.dagpenger.iverksett.api.domene.VedtaksdetaljerOvergangsstønad
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottakere

fun IverksettOvergangsstønad.copy(vedtak: Vedtaksdetaljer): IverksettOvergangsstønad {
    return when (this) {
        is IverksettOvergangsstønad -> this.copy(vedtak = vedtak as VedtaksdetaljerOvergangsstønad)
        else -> error("Ingen støtte ennå")
    }
}

fun Vedtaksdetaljer.copy(brevmottakere: Brevmottakere): Vedtaksdetaljer {
    return when (this) {
        is VedtaksdetaljerOvergangsstønad -> this.copy(brevmottakere = brevmottakere)
        else -> error("Ingen støtte ennå")
    }
}

fun Vedtaksdetaljer.copy(tilkjentYtelse: TilkjentYtelse): Vedtaksdetaljer {
    return when (this) {
        is VedtaksdetaljerOvergangsstønad -> this.copy(tilkjentYtelse = tilkjentYtelse)
        else -> error("Ingen støtte ennå")
    }
}
