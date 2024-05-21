package no.nav.utsjekk.task

import no.nav.familie.prosessering.domene.Status
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class UtsjekkTaskService(
    private val repository: UtsjekkTaskRepository,
) {
    fun tasks(
        statuser: List<Status>,
        type: String? = null,
        callId: String? = null,
        tasksPerPage: Int,
        page: Int? = null
    ): PaginatedTaskResponse {
        val tasks = repository.tasks(statuser, type, callId, tasksPerPage, page)
        val totaltAntallTasks = repository.antallTasks(statuser, type, callId)

        return PaginatedTaskResponse(
            tasks = tasks,
            totalTasks = totaltAntallTasks,
            pages = ceil(totaltAntallTasks.toDouble() / tasksPerPage.toDouble()).toInt(),
            currentPage = page ?: 1,
        )
    }

    fun logs(taskId: Long) = repository.logs(taskId)
}

data class PaginatedTaskResponse(
    val tasks: List<TaskDto>,
    val totalTasks: Int,
    val pages: Int,
    val currentPage: Int,
)