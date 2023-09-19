package no.nav.dagpenger.iverksett.konsumenter.brev.frittstående

import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottakere
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.FrittståendeBrev
import no.nav.dagpenger.kontrakter.felles.BrevmottakerDto
import no.nav.dagpenger.kontrakter.felles.FrittståendeBrevType
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger

object FrittståendeBrevUtil {

    fun opprettFrittståendeBrev() = FrittståendeBrev(
        personIdent = "11",
        eksternFagsakId = 1L,
        journalførendeEnhet = "enhet",
        saksbehandlerIdent = "saksbehandlerIdent",
        stønadstype = StønadTypeDagpenger.DAGPENGER_ARBEIDSSOKER_ORDINAER,
        mottakere = Brevmottakere(
            listOf(
                no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottaker(
                    "11",
                    "navn1",
                    BrevmottakerDto.IdentType.PERSONIDENT,
                    BrevmottakerDto.MottakerRolle.BRUKER,
                ),
                no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottaker(
                    "22",
                    "navn2",
                    BrevmottakerDto.IdentType.PERSONIDENT,
                    BrevmottakerDto.MottakerRolle.BRUKER,
                ),
            ),
        ),
        fil = byteArrayOf(13),
        brevtype = FrittståendeBrevType.INFORMASJONSBREV,
    )
}
