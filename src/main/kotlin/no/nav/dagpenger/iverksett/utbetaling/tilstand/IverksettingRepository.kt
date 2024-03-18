package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksetting
import no.nav.dagpenger.iverksett.utbetaling.domene.IverksettingEntitet
import no.nav.dagpenger.iverksett.utbetaling.domene.sakId
import no.nav.dagpenger.kontrakter.felles.objectMapper
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class IverksettingRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {
    fun insert(iverksetting: IverksettingEntitet): IverksettingEntitet {
        val sql =
            """
            insert into iverksetting (behandling_id, data, mottatt_tidspunkt) 
            values (:behandlingId, to_json(:data::json), :mottattTidspunkt)
            """.trimIndent()
        jdbcTemplate.update(
            sql,
            mapOf(
                "behandlingId" to iverksetting.behandlingId,
                "data" to objectMapper.writeValueAsString(iverksetting.data),
                "mottattTidspunkt" to iverksetting.mottattTidspunkt,
            ),
        )
        return iverksetting
    }

    fun findByFagsakId(fagsakId: String): List<IverksettingEntitet> {
        val sql =
            """
            select behandling_id, data, mottatt_tidspunkt 
            from iverksetting 
            where data -> 'fagsak' ->> 'fagsakId' = :sakId 
                or data -> 'fagsak' -> 'fagsakId' ->> 'id' = :sakId;
            """.trimIndent()
        return jdbcTemplate.query(
            sql,
            mapOf("sakId" to fagsakId),
            IverksettingRowMapper(),
        )
    }

    fun findByFagsakAndBehandlingAndIverksetting(
        fagsakId: String,
        behandlingId: String,
        iverksettingId: String?,
    ) = if (iverksettingId != null) {
        findByFagsakIdAndBehandlingIdAndIverksettingId(fagsakId, behandlingId, iverksettingId)
    } else {
        findByFagsakIdAndBehandlingId(fagsakId, behandlingId)
    }

    private fun findByFagsakIdAndBehandlingIdAndIverksettingId(
        fagsakId: String,
        behandlingId: String,
        iverksettingId: String,
    ): List<IverksettingEntitet> {
        val sql =
            """
            select behandling_id, data, mottatt_tidspunkt 
            from iverksetting 
            where behandling_id = :behandlingId 
                and (data -> 'fagsak' ->> 'fagsakId' = :sakId or data -> 'fagsak' -> 'fagsakId' ->> 'id' = :sakId) 
                and (data -> 'behandling' ->> 'iverksettingId' = :iverksettingId or data -> 'behandling' -> 'iverksettingId' ->> 'id' = :iverksettingId)
            """.trimIndent()
        return jdbcTemplate.query(
            sql,
            mapOf(
                "behandlingId" to behandlingId,
                "sakId" to fagsakId,
                "iverksettingId" to iverksettingId,
            ),
            IverksettingRowMapper(),
        )
    }

    private fun findByFagsakIdAndBehandlingId(
        fagsakId: String,
        behandlingId: String,
    ): List<IverksettingEntitet> {
        val sql =
            """
            select behandling_id, data, mottatt_tidspunkt 
            from iverksetting 
            where behandling_id = :behandlingId 
                and (data -> 'fagsak' ->> 'fagsakId' = :sakId or data -> 'fagsak' -> 'fagsakId' ->> 'id' = :sakId) 
                and (data -> 'behandling' ->> 'iverksettingId' is null and data -> 'behandling' -> 'iverksettingId' ->> 'id' is null)
            """.trimIndent()
        return jdbcTemplate.query(
            sql,
            mapOf("behandlingId" to behandlingId, "sakId" to fagsakId),
            IverksettingRowMapper(),
        )
    }

    fun findByEmptyMottattTidspunkt(): List<IverksettingEntitet> {
        val sql = "select behandling_id, data, mottatt_tidspunkt from iverksetting where mottatt_tidspunkt is null"
        return jdbcTemplate.query(sql, IverksettingRowMapper())
    }

    fun settMottattTidspunktForIverksetting(iverksetting: IverksettingEntitet) {
        if (iverksetting.data.behandling.iverksettingId == null) {
            val sql =
                "update iverksetting set mottatt_tidspunkt = ? where behandling_id = ? " +
                    "and data -> 'fagsak' -> 'fagsakId' ->> 'id' = ? and data -> 'behandling' ->> 'iverksettingId' is null"
            jdbcTemplate.update(
                sql,
                iverksetting.mottattTidspunkt,
                iverksetting.behandlingId,
                iverksetting.data.sakId,
            )
        } else {
            val sql =
                "update iverksetting set mottatt_tidspunkt = ? where behandling_id = ? " +
                    "and data -> 'fagsak' -> 'fagsakId' ->> 'id' = ? and data -> 'behandling' ->> 'iverksettingId' = ?"
            jdbcTemplate.update(
                sql,
                iverksetting.mottattTidspunkt,
                iverksetting.behandlingId,
                iverksetting.data.sakId,
                iverksetting.data.behandling.iverksettingId,
            )
        }
    }
}

internal class IverksettingRowMapper : RowMapper<IverksettingEntitet> {
    override fun mapRow(
        resultSet: ResultSet,
        rowNum: Int,
    ) = IverksettingEntitet(
        behandlingId = resultSet.getString("behandling_id"),
        data = objectMapper.readValue(resultSet.getString("data"), Iverksetting::class.java),
        mottattTidspunkt = resultSet.getTimestamp("mottatt_tidspunkt")?.toLocalDateTime(),
    )
}
