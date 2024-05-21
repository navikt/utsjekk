package no.nav.utsjekk.task

import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.utsjekk.Integrasjonstest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UtsjekkTaskServiceTest : Integrasjonstest() {
    @Autowired
    lateinit var taskService: TaskService

    lateinit var utsjekkTaskService: UtsjekkTaskService

    @BeforeAll
    fun setup() {
        utsjekkTaskService = UtsjekkTaskService(UtsjekkTaskRepository(namedParameterJdbcTemplate))
    }

    @Test
    fun `finner tasks med type`() {
        taskService.saveAll(
            listOf(
                enTask("EN_TASK"),
                enTask("EN_ANNEN_TASK"),
                enTask("EN_TREDJE_TASK"),
            )
        )

        val response = utsjekkTaskService.tasks(listOf(Status.UBEHANDLET), type = "EN_ANNEN_TASK", null, 20, null)

        assertEquals(1, response.tasks.size)
        assertEquals("EN_ANNEN_TASK", response.tasks.first().type)
        assertEquals(1, response.tasks.first().antallLogger)
        assertEquals(1, response.currentPage)
        assertEquals(1, response.pages)
        assertEquals(1, response.totalTasks)
    }

    @Test
    fun `finner tasks med status`() {
        taskService.saveAll(
            listOf(
                enTask("EN_TASK"),
                enTask("EN_ANNEN_TASK"),
                enTask("EN_TREDJE_TASK"),
            )
        )

        val response = utsjekkTaskService.tasks(listOf(Status.UBEHANDLET), null, null, 20, null)

        assertEquals(3, response.tasks.size)
        assertEquals(1, response.tasks[0].antallLogger)
        assertEquals(1, response.tasks[1].antallLogger)
        assertEquals(1, response.tasks[2].antallLogger)
        assertEquals(1, response.currentPage)
        assertEquals(1, response.pages)
        assertEquals(3, response.totalTasks)
    }

    @Test
    fun `finner tasks med callId`() {
        val tasks = taskService.saveAll(
            listOf(
                enTask("EN_TASK"),
                enTask("EN_ANNEN_TASK"),
                enTask("EN_TREDJE_TASK"),
            )
        )

        val callId = tasks[1].callId

        utsjekkTaskService.tasks(listOf(Status.UBEHANDLET), callId = callId, tasksPerPage = 20).also { response ->
            assertEquals(1, response.tasks.size)
            assertEquals("EN_ANNEN_TASK", response.tasks.first().type)
            assertEquals(callId, response.tasks.first().metadata.callId)
            assertEquals(1, response.currentPage)
            assertEquals(1, response.pages)
            assertEquals(1, response.totalTasks)
        }
    }

    @Test
    fun `paginerer tasks`() {
        taskService.saveAll(
            (1..20).map { enTask() }
        )

        val response = utsjekkTaskService.tasks(listOf(Status.UBEHANDLET), tasksPerPage = 7, page = 2)
        assertEquals(7, response.tasks.size)
        assertEquals(2, response.currentPage)
        assertEquals(3, response.pages)
        assertEquals(20, response.totalTasks)
    }

    @Test
    fun `tillater ikke å hente sider som ikke finnes`() {
        taskService.saveAll(
            (1..20).map { enTask() }
        )

        val response = utsjekkTaskService.tasks(listOf(Status.UBEHANDLET), tasksPerPage = 7, page = 4)
        assertEquals(0, response.tasks.size)
        assertEquals(4, response.currentPage)
        assertEquals(3, response.pages)
        assertEquals(20, response.totalTasks)
    }

    private fun enTask(type: String = UUID.randomUUID().toString(), property: String? = ""): Task {
        return Task(
            type = type,
            payload = "{\"property\":\"${property}\"}",
        )
    }
}

