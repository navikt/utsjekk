package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.IverksettingEntitet
import no.nav.dagpenger.kontrakter.felles.objectMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

@Repository
class IverksettingRepository(private val jdbcTemplate: JdbcTemplate) {
    fun insert(iverksetting: IverksettingEntitet): IverksettingEntitet {
        val sql = "insert into iverksetting (behandling_id, data) values (?,to_json(?::json))"
        jdbcTemplate.update(sql, iverksetting.behandlingId, objectMapper.writeValueAsString(iverksetting.data))
        return iverksetting
    }

    fun findByIdOrThrow(behandlingId: UUID): IverksettingEntitet {
        val sql = "select behandling_id, data from iverksetting where behandling_id = ?"
        val resultat = jdbcTemplate.query(sql, IverksettingRowMapper(), behandlingId)
        return when (resultat.size) {
            1 -> resultat.first()
            0 -> throw NoSuchElementException("Fant ingen iverksettinger for behandling $behandlingId")
            else -> throw IllegalStateException("Fant flere iverksettinger for behandling $behandlingId")
        }
    }

    fun findByFagsakId(fagsakId: String): List<IverksettingEntitet> {
        val sql = "select behandling_id, data from iverksetting where data -> 'fagsak' -> 'fagsakId' ->> 'id' = ?"
        return jdbcTemplate.query(sql, IverksettingRowMapper(), fagsakId)
    }

    fun findByBehandlingAndIverksetting(
        behandlingId: UUID,
        iverksettingId: String?,
    ): IverksettingEntitet? {
        return if (iverksettingId != null) {
            findByBehandlingIdAndIverksettingId(behandlingId, iverksettingId)
        } else {
            findByBehandlingId(behandlingId)
        }
    }

    private fun findByBehandlingIdAndIverksettingId(
        behandlingId: UUID,
        iverksettingId: String,
    ): IverksettingEntitet? {
        val sql = "select behandling_id, data from iverksetting where behandling_id = ? and data -> 'behandling' ->> 'iverksettingId' = ?"
        val resultat = jdbcTemplate.query(sql, IverksettingRowMapper(), behandlingId, iverksettingId)
        return when (resultat.size) {
            0 -> null
            1 -> resultat.first()
            else -> throw IllegalStateException("Fant flere iverksettinger for behandling $behandlingId og iverksetting $iverksettingId")
        }
    }

    private fun findByBehandlingId(behandlingId: UUID): IverksettingEntitet? {
        val sql =
            "select behandling_id, data from iverksetting where behandling_id = ? and data -> 'behandling' ->> 'iverksettingId' is null"
        val resultat = jdbcTemplate.query(sql, IverksettingRowMapper(), behandlingId)
        return when (resultat.size) {
            0 -> null
            1 -> resultat.first()
            else -> throw IllegalStateException("Fant flere iverksettinger for behandling $behandlingId")
        }
    }
}

internal class IverksettingRowMapper : RowMapper<IverksettingEntitet> {
    override fun mapRow(
        resultSet: ResultSet,
        rowNum: Int,
    ): IverksettingEntitet {
        return IverksettingEntitet(
            behandlingId = UUID.fromString(resultSet.getString("behandling_id")),
            data = objectMapper.readValue(resultSet.getString("data"), Iverksetting::class.java),
        )
    }
}
