package no.nav.dagpenger.iverksett.api

import no.nav.dagpenger.iverksett.api.domene.Iverksett
import no.nav.dagpenger.iverksett.infrastruktur.repository.InsertUpdateRepository
import no.nav.dagpenger.iverksett.infrastruktur.repository.RepositoryInterface
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IverksettingRepository : RepositoryInterface<Iverksett, UUID>, InsertUpdateRepository<Iverksett> {

    @Query("SELECT behandling_id from iverksett")
    fun finnAlleIder(): List<UUID>

    fun findByEksternId(eksternId: Long): Iverksett

    @Query("select behandling_id, data, ekstern_id, person_id from iverksett where data -> 'søker' ->> 'personIdent' = :personId")
    fun findByPersonId(@Param("personId") personId: String): List<Iverksett>
}
