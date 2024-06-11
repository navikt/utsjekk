package no.nav.utsjekk.simulering.domene

import no.nav.utsjekk.simulering.api.SimuleringResponsDto

object OppsummeringGenerator {
    fun lagOppsummering(detaljer: SimuleringDetaljer): SimuleringResponsDto {
        return SimuleringResponsDto(oppsummeringer = emptyList(), detaljer = detaljer)
    }
}
