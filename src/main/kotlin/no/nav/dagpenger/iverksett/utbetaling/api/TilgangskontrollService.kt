package no.nav.dagpenger.iverksett.utbetaling.api

import no.nav.dagpenger.iverksett.utbetaling.domene.Fagsakdetaljer

interface TilgangskontrollService {
    fun valider(fagsakdetaljer: Fagsakdetaljer)
}
