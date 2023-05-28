package no.nav.dagpenger.iverksett.konsumenter.brev.domain

import no.nav.dagpenger.kontrakter.felles.BrevmottakerDto.IdentType
import no.nav.dagpenger.kontrakter.felles.BrevmottakerDto.MottakerRolle
import no.nav.dagpenger.kontrakter.felles.BrukerIdType

data class Brevmottakere(val mottakere: List<Brevmottaker>)
data class Brevmottaker(
    val ident: String,
    val navn: String,
    val identType: IdentType,
    val mottakerRolle: MottakerRolle,
)

fun IdentType.tilIdType(): BrukerIdType =
    when (this) {
        IdentType.ORGANISASJONSNUMMER -> BrukerIdType.ORGNR
        IdentType.PERSONIDENT -> BrukerIdType.FNR
    }
