package no.nav.utsjekk.task

import no.nav.familie.prosessering.domene.Status
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/tasks")
@ProtectedWithClaims(issuer = "azuread")
class UtsjekkTaskController(
    private val taskService: UtsjekkTaskService,
) {
    @GetMapping
    fun tasks(
        @RequestParam status: Status?,
        @RequestParam type: String?,
        @RequestParam callId: String?,
        @RequestParam tasksPerPage: Int?,
        @RequestParam page: Int?
    ): ResponseEntity<TaskResponseBody> {
        val statuser: List<Status> = status?.let { listOf(it) } ?: Status.entries
        val paginatedResult = taskService.tasks(statuser, type, callId, tasksPerPage ?: 20, page)

        return ResponseEntity(
            TaskResponseBody(
                tasks = paginatedResult.tasks,
                totaltAntallTasks = paginatedResult.totalTasks,
                pages = paginatedResult.pages,
                currentPage = paginatedResult.currentPage,
            ),
            HttpStatus.OK
        )
    }

    @GetMapping("{taskId}/logs")
    fun logs(@PathVariable taskId: Long): ResponseEntity<LogsResponseBody> {
        val logs = taskService.logs(taskId)

        return ResponseEntity(LogsResponseBody(logs), HttpStatus.OK)
    }
}

data class TaskResponseBody(
    val tasks: List<TaskDto>,
    val totaltAntallTasks: Int,
    val pages: Int,
    val currentPage: Int,
)

data class LogsResponseBody(
    val logs: List<LogDto>
)