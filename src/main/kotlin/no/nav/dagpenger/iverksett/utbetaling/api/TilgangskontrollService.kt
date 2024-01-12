package no.nav.dagpenger.iverksett.utbetaling.api

import no.nav.dagpenger.kontrakter.iverksett.IverksettDto

interface TilgangskontrollService {
    fun valider(iverksett: IverksettDto)
}