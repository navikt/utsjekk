package no.nav.dagpenger.iverksett.konsumenter.brev.frittstående

import no.nav.dagpenger.iverksett.infrastruktur.repository.InsertUpdateRepository
import no.nav.dagpenger.iverksett.infrastruktur.repository.RepositoryInterface
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.DistribuerBrevResultatMap
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.FrittståendeBrev
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.JournalpostResultatMap
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
interface FrittståendeBrevRepository :
    RepositoryInterface<FrittståendeBrev, UUID>, InsertUpdateRepository<FrittståendeBrev> {

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE frittstaende_brev SET journalpost_resultat=:journalpostresultat WHERE id=:id")
    fun oppdaterJournalpostResultat(id: UUID, journalpostresultat: JournalpostResultatMap)

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE frittstaende_brev SET distribuer_brev_resultat=:distribuerBrevResultat WHERE id=:id")
    fun oppdaterDistribuerBrevResultat(id: UUID, distribuerBrevResultat: DistribuerBrevResultatMap)
}
