package no.nav.utsjekk.iverksetting.tilstand

import no.nav.utsjekk.iverksetting.domene.Iverksetting
import no.nav.utsjekk.iverksetting.domene.IverksettingEntitet
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.objectMapper
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class IverksettingRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {
    fun insert(iverksetting: IverksettingEntitet) =
        jdbcTemplate.update(
            """
            insert into iverksetting (behandling_id, data, mottatt_tidspunkt) 
            values (:behandlingId, to_json(:data::json), :mottattTidspunkt)
            """.trimIndent(),
            mapOf(
                "behandlingId" to iverksetting.behandlingId,
                "data" to objectMapper.writeValueAsString(iverksetting.data),
                "mottattTidspunkt" to iverksetting.mottattTidspunkt,
            ),
        )

    fun findByFagsakId(fagsakId: String): List<IverksettingEntitet> =
        jdbcTemplate.query(
            """
            select behandling_id, data, mottatt_tidspunkt 
            from iverksetting 
            where data -> 'fagsak' ->> 'fagsakId' = :sakId 
                or data -> 'fagsak' -> 'fagsakId' ->> 'id' = :sakId;
            """.trimIndent(),
            mapOf("sakId" to fagsakId),
            IverksettingRowMapper(),
        )

    fun findByFagsakIdAndFagsystem(
        fagsakId: String,
        fagsystem: Fagsystem,
    ): List<IverksettingEntitet> =
        jdbcTemplate.query(
            """
            select behandling_id, data, mottatt_tidspunkt 
            from iverksetting 
            where data -> 'fagsak' ->> 'fagsakId' = :sakId and data -> 'fagsak' ->> 'fagsystem' = :fagsystem
            """.trimIndent(),
            mapOf("sakId" to fagsakId, "fagsystem" to fagsystem.name),
            IverksettingRowMapper(),
        )

    fun findByFagsakAndBehandlingAndIverksetting(
        fagsakId: String,
        behandlingId: String,
        iverksettingId: String?,
    ): List<IverksettingEntitet> =
        if (iverksettingId != null) {
            findByFagsakIdAndBehandlingIdAndIverksettingId(fagsakId, behandlingId, iverksettingId)
        } else {
            findByFagsakIdAndBehandlingId(fagsakId, behandlingId)
        }

    private fun findByFagsakIdAndBehandlingIdAndIverksettingId(
        fagsakId: String,
        behandlingId: String,
        iverksettingId: String,
    ): List<IverksettingEntitet> =
        jdbcTemplate.query(
            """
            select behandling_id, data, mottatt_tidspunkt 
            from iverksetting 
            where behandling_id = :behandlingId 
                and data -> 'fagsak' ->> 'fagsakId' = :sakId 
                and data -> 'behandling' ->> 'iverksettingId' = :iverksettingId
            """.trimIndent(),
            mapOf(
                "behandlingId" to behandlingId,
                "sakId" to fagsakId,
                "iverksettingId" to iverksettingId,
            ),
            IverksettingRowMapper(),
        )

    private fun findByFagsakIdAndBehandlingId(
        fagsakId: String,
        behandlingId: String,
    ): List<IverksettingEntitet> =
        jdbcTemplate.query(
            """
            select behandling_id, data, mottatt_tidspunkt 
            from iverksetting 
            where behandling_id = :behandlingId 
                and data -> 'fagsak' ->> 'fagsakId' = :sakId 
                and data -> 'behandling' ->> 'iverksettingId' is null
            """.trimIndent(),
            mapOf("behandlingId" to behandlingId, "sakId" to fagsakId),
            IverksettingRowMapper(),
        )
}

internal class IverksettingRowMapper : RowMapper<IverksettingEntitet> {
    override fun mapRow(
        resultSet: ResultSet,
        rowNum: Int,
    ) = IverksettingEntitet(
        behandlingId = resultSet.getString("behandling_id"),
        data = objectMapper.readValue(resultSet.getString("data"), Iverksetting::class.java),
        mottattTidspunkt = resultSet.getTimestamp("mottatt_tidspunkt").toLocalDateTime(),
    )
}
