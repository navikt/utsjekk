package no.nav.dagpenger.iverksett.utbetaling.api

import no.nav.dagpenger.kontrakter.felles.GeneriskId

interface TilgangskontrollService {
    fun valider(sakId: GeneriskId)
}