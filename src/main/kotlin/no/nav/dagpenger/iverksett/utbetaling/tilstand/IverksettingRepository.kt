package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.utbetaling.domene.IverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.tilstand.konfig.InsertUpdateRepository
import no.nav.dagpenger.iverksett.utbetaling.tilstand.konfig.RepositoryInterface
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface IverksettingRepository : RepositoryInterface<IverksettingEntitet, UUID>, InsertUpdateRepository<IverksettingEntitet> {

    @Query("select behandling_id, data from iverksetting where data -> 'søker' ->> 'personident' = :personId")
    fun findByPersonId(@Param("personId") personId: String): List<IverksettingEntitet>

    @Query("select behandling_id, data from iverksetting where data -> 'fagsak' -> 'fagsakId' ->> 'id' = :fagsakId")
    fun findByFagsakId(@Param("fagsakId") fagsakId: String): List<IverksettingEntitet>
}
