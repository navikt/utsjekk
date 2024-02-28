package no.nav.dagpenger.iverksett

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.dagpenger.iverksett.felles.Profiler
import no.nav.dagpenger.iverksett.felles.database.DbContainerInitializer
import no.nav.dagpenger.iverksett.felles.oppdrag.konfig.RestTemplateAzure
import no.nav.dagpenger.iverksett.felles.util.TokenUtil
import no.nav.dagpenger.iverksett.status.KafkaContainerInitializer
import no.nav.dagpenger.iverksett.utbetaling.domene.KonsumentConfig
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID

@ExtendWith(SpringExtension::class)
@ContextConfiguration(initializers = [DbContainerInitializer::class, KafkaContainerInitializer::class])
@SpringBootTest(classes = [ApplicationLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Profiler.INTEGRASJONSTEST, Profiler.MOCK_OPPDRAG, Profiler.MOCK_OAUTH)
@EnableMockOAuth2Server
abstract class Integrasjonstest {
    protected val restTemplate = TestRestTemplate(RestTemplateAzure().restTemplateBuilder())
    protected val headers = HttpHeaders()

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    protected lateinit var konsumentConfig: KonsumentConfig

    @LocalServerPort
    private var port: Int? = 0

    @AfterEach
    fun reset() {
        headers.clear()
        resetDatabase()
        resetWiremockServers()
    }

    private fun resetWiremockServers() {
        applicationContext.getBeansOfType(WireMockServer::class.java).values.forEach(WireMockServer::resetRequests)
    }

    private fun resetDatabase() {
        namedParameterJdbcTemplate.update(
            "TRUNCATE TABLE iverksetting, iverksettingsresultat CASCADE",
            MapSqlParameterSource(),
        )
        namedParameterJdbcTemplate.update("TRUNCATE TABLE task, task_logg CASCADE", MapSqlParameterSource())
    }

    protected fun getPort(): String {
        return port.toString()
    }

    protected fun localhostUrl(uri: String): String {
        return "http://localhost:" + getPort() + uri
    }

    protected fun lokalTestToken(
        saksbehandler: String = "julenissen",
        grupper: List<String> = emptyList(),
    ): String {
        return TokenUtil.onBehalfOfToken(mockOAuth2Server, saksbehandler, grupper)
    }

    protected fun lokalClientCredentialsTestToken(
        accessAsApplication: Boolean,
        clientId: String = UUID.randomUUID().toString(),
    ): String {
        return TokenUtil.clientToken(mockOAuth2Server, accessAsApplication, clientId)
    }
}
