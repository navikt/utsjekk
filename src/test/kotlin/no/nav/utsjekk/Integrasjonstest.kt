package no.nav.utsjekk

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.utsjekk.felles.Profiler
import no.nav.utsjekk.felles.oppdrag.konfig.RestTemplateAzure
import no.nav.utsjekk.initializers.DbContainerInitializer
import no.nav.utsjekk.initializers.KafkaContainerInitializer
import no.nav.utsjekk.util.TokenUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ConfigurableApplicationContext
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
@ActiveProfiles(Profiler.INTEGRASJONSTEST, Profiler.MOCK_OPPDRAG, Profiler.MOCK_OAUTH, Profiler.MOCK_SIMULERING)
@EnableMockOAuth2Server
abstract class Integrasjonstest {
    protected val restTemplate = TestRestTemplate(RestTemplateAzure().restTemplateBuilder())
    protected val headers = HttpHeaders()

    @Autowired
    private lateinit var applicationContext: ConfigurableApplicationContext

    @Autowired
    protected lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @LocalServerPort
    private var port: Int? = 0

    @AfterEach
    fun reset() {
        headers.clear()
        resetDatabase()
        resetWiremockServers()
        KafkaContainerInitializer.deleteAllRecords()
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

    protected fun localhostUrl(uri: String) = "http://localhost:${port}$uri"

    protected fun lokalTestToken(
        saksbehandler: String = "julenissen",
        grupper: List<String> = emptyList(),
        klientapp: String = "dev-gcp:tiltakspenger:tiltakspenger-vedtak",
    ) = TokenUtil.onBehalfOfToken(
        mockOAuth2Server = mockOAuth2Server,
        saksbehandler = saksbehandler,
        grupper = grupper,
        klientnavn = klientapp,
    )

    protected fun lokalClientCredentialsTestToken(
        accessAsApplication: Boolean,
        clientId: String = UUID.randomUUID().toString(),
        klientapp: String = "dev-gcp:tiltakspenger:tiltakspenger-vedtak",
    ) = TokenUtil.clientToken(
        mockOAuth2Server = mockOAuth2Server,
        accessAsApplication = accessAsApplication,
        clientId = clientId,
        klientnavn = klientapp,
    )
}
