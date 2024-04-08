package no.nav.utsjekk.initializers

import org.slf4j.LoggerFactory
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.PostgreSQLContainer

class DbContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        postgres.start()

        logger.info("Database startet lokalt på ${postgres.jdbcUrl}")
        TestPropertyValues.of(
            "spring.datasource.url=${postgres.jdbcUrl}",
            "spring.datasource.username=${postgres.username}",
            "spring.datasource.password=${postgres.password}",
        ).applyTo(applicationContext.environment)
    }

    companion object {
        private val postgres: KPostgreSQLContainer by lazy {
            KPostgreSQLContainer("postgres:14.8")
                .withDatabaseName("dp-iverksett")
                .withUsername("postgres")
                .withPassword("test")
        }
    }
}

// Hack needed because testcontainers use of generics confuses Kotlin
class KPostgreSQLContainer(imageName: String) : PostgreSQLContainer<KPostgreSQLContainer>(imageName)
