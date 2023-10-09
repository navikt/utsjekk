package no.nav.dagpenger.iverksett.api.tilgangskontroll

import no.nav.dagpenger.kontrakter.iverksett.IverksettDto

interface TilgangskontrollService {
    fun valider(iverksett: IverksettDto)
}