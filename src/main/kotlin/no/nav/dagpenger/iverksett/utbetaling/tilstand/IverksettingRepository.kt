package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.utbetaling.domene.IverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.tilstand.konfig.InsertUpdateRepository
import no.nav.dagpenger.iverksett.utbetaling.tilstand.konfig.RepositoryInterface
import no.nav.dagpenger.kontrakter.felles.SakIdentifikator
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IverksettingRepository : RepositoryInterface<IverksettingEntitet, UUID>, InsertUpdateRepository<IverksettingEntitet> {

    @Query("select behandling_id, data from iverksetting where data -> 'søker' ->> 'personident' = :personId")
    fun findByPersonId(@Param("personId") personId: String): List<IverksettingEntitet>

    @Query("select behandling_id, data from iverksetting where data -> 'fagsak' ->> 'fagsakId' = :fagsakId::text")
    fun findByFagsakId(@Param("fagsakId") fagsakId: UUID): List<IverksettingEntitet>

    @Query(
        """
            select behandling_id, data from iverksetting 
            where data -> 'fagsak' ->> 'saksreferanse' = :saksreferanse
        """
    )
    fun findBySaksreferanse(
        @Param("saksreferanse") saksreferanse: String,
    ): List<IverksettingEntitet>
}

fun IverksettingRepository.findBySakIdentifikator(
    sakIdentifikator: SakIdentifikator
): List<IverksettingEntitet> {
    return if (sakIdentifikator.sakId != null) {
        this.findByFagsakId(sakIdentifikator.sakId!!)
    } else {
        this.findBySaksreferanse(sakIdentifikator.saksreferanse!!)
    }
}
