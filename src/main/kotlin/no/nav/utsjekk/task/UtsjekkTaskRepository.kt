package no.nav.utsjekk.task

import no.nav.familie.prosessering.domene.Status
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class UtsjekkTaskRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    fun antallTasks(
        statuser: List<Status>,
        type: String? = null,
        callId: String? = null,
    ): Int =
        jdbcTemplate.query(
            """
            select count(*)
            from task T
            where status in (:statuser)
                and (:type::text is null or type = :type::text)
                and (:callId::text is null or metadata like concat('%callId=', concat(:callId::text, '%')))
            """.trimIndent(),
            mapOf(
                "statuser" to statuser.map { it.name },
                "type" to type,
                "callId" to callId,
            )
        ) { rs, _ ->
            rs.getInt("count")
        }.single()

    fun tasks(
        statuser: List<Status>,
        type: String? = null,
        callId: String? = null,
        tasksPerPage: Int? = null,
        page: Int? = null
    ): List<TaskDto> =
        jdbcTemplate.query(
            """
            select T.id, T.type, status, T.opprettet_tid, trigger_tid, (select count(*) from task_logg L where T.id = L.task_id) as antall_logger, T.metadata
            from task T
            where status in (:statuser)
                and (:type::text is null or type = :type::text)
                and (:callId::text is null or metadata like concat('%callId=', concat(:callId::text, '%')))
            offset :offset
                limit :limit
        """.trimIndent(),
            mapOf(
                "statuser" to statuser.map { it.name },
                "type" to type,
                "offset" to (((page ?: 1) - 1) * (tasksPerPage ?: 0)),
                "limit" to tasksPerPage,
                "callId" to callId,
            )
        ) { rs, _ ->
            TaskDto(
                id = rs.getLong("id"),
                type = rs.getString("type"),
                status = Status.valueOf(rs.getString("status")),
                opprettetTidspunkt = rs.getTimestamp("opprettet_tid").toLocalDateTime(),
                triggerTid = rs.getTimestamp("trigger_tid").toLocalDateTime(),
                antallLogger = rs.getInt("antall_logger"),
                metadata = rs.getString("metadata").split("\n").let { metadata ->
                    TaskDto.Metadata(
                        callId = metadata.find { it.contains("callId") }!!.split("=")[1]
                    )
                }
            )
        }

    fun logs(taskId: Long): List<LogDto> = jdbcTemplate.query("""
        select * from task_logg where task_id = :taskId
    """.trimIndent(),
        mapOf("taskId" to taskId)
    ) { rs, _ ->
        LogDto(
            id = rs.getLong("id"),
            type = rs.getString("type"),
            pod = rs.getString("node"),
            opprettetTidspunkt = rs.getTimestamp("opprettet_tid").toLocalDateTime(),
            melding = rs.getString("melding"),
            endretAv = rs.getString("endret_av"),
        )
    }
}

data class TaskDto(
    val id: Long,
    val type: String,
    val status: Status,
    val opprettetTidspunkt: LocalDateTime,
    val triggerTid: LocalDateTime,
    val antallLogger: Int,
    val metadata: Metadata,
) {
    data class Metadata(
        val callId: String,
    )
}

data class LogDto(
    val id: Long,
    val type: String,
    val pod: String,
    val opprettetTidspunkt: LocalDateTime,
    val melding: String?,
    val endretAv: String,
)