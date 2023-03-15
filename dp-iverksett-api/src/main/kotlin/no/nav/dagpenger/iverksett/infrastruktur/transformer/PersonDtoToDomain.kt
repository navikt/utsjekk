package no.nav.dagpenger.iverksett.infrastruktur.transformer

import no.nav.dagpenger.iverksett.api.domene.Barn
import no.nav.dagpenger.iverksett.api.domene.Person
import no.nav.dagpenger.iverksett.kontrakter.iverksett.BarnDto
import no.nav.dagpenger.iverksett.kontrakter.iverksett.PersonDto

fun PersonDto.toDomain(): Person {
    return Person(this.personIdent)
}

fun BarnDto.toDomain(): Barn {
    return Barn(
        personIdent = this.personIdent,
        termindato = this.termindato,
    )
}
