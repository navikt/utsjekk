package no.nav.dagpenger.iverksett.utbetaling.api

import no.nav.dagpenger.kontrakter.felles.SakIdentifikator

interface TilgangskontrollService {
    fun valider(sakId: SakIdentifikator)
}