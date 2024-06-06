package no.nav.utsjekk.simulering.domene

import no.nav.utsjekk.simulering.api.SimuleringResponsDto
import no.nav.utsjekk.simulering.client.dto.SimuleringResponse

object OppsummeringGenerator {
    fun lagOppsummering(rådata: SimuleringResponse): SimuleringResponsDto {
        return SimuleringResponsDto(oppsummeringer = emptyList(), rådata = rådata)
    }
}
