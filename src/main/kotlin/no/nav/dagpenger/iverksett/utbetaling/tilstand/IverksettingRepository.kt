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
        val sql = "insert into iverksetting (behandling_id, data, mottatt_tidspunkt) values (?,to_json(?::json), ?)"
        jdbcTemplate.update(
            sql,
            iverksetting.behandlingId,
            objectMapper.writeValueAsString(iverksetting.data),
            iverksetting.mottattTidspunkt,
        )
        return iverksetting
    }

    fun findByFagsakId(fagsakId: String): List<IverksettingEntitet> {
        val sql = "select behandling_id, data, mottatt_tidspunkt from iverksetting where data -> 'fagsak' -> 'fagsakId' ->> 'id' = ?"
        return jdbcTemplate.query(sql, IverksettingRowMapper(), fagsakId)
    }

    fun findByFagsakAndBehandlingAndIverksetting(
        fagsakId: String,
        behandlingId: UUID,
        iverksettingId: String?,
    ): List<IverksettingEntitet> {
        return if (iverksettingId != null) {
            findByFagsakIdAndBehandlingIdAndIverksettingId(fagsakId, behandlingId, iverksettingId)
        } else {
            findByFagsakIdAndBehandlingId(fagsakId, behandlingId)
        }
    }

    private fun findByFagsakIdAndBehandlingIdAndIverksettingId(
        fagsakId: String,
        behandlingId: UUID,
        iverksettingId: String,
    ): List<IverksettingEntitet> {
        val sql =
            "select behandling_id, data, mottatt_tidspunkt from iverksetting where behandling_id = ? " +
                "and data -> 'fagsak' -> 'fagsakId' ->> 'id' = ? and data -> 'behandling' ->> 'iverksettingId' = ?"
        return jdbcTemplate.query(sql, IverksettingRowMapper(), behandlingId, fagsakId, iverksettingId)
    }

    private fun findByFagsakIdAndBehandlingId(
        fagsakId: String,
        behandlingId: UUID,
    ): List<IverksettingEntitet> {
        val sql =
            "select behandling_id, data, mottatt_tidspunkt from iverksetting where behandling_id = ? " +
                "and data -> 'fagsak' -> 'fagsakId' ->> 'id' = ? and data -> 'behandling' ->> 'iverksettingId' is null"
        return jdbcTemplate.query(sql, IverksettingRowMapper(), behandlingId, fagsakId)
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
            mottattTidspunkt = resultSet.getTimestamp("mottatt_tidspunkt")?.toLocalDateTime(),
        )
    }
}
