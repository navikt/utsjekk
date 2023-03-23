package no.nav.dagpenger.iverksett.infrastruktur.kafka

import org.slf4j.LoggerFactory
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

class KafkaContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        kafka.start()

        logger.info("Kafka startet lokalt med bootstrap-servere: ${kafka.bootstrapServers}")
        TestPropertyValues.of(
            "spring.kafka.bootstrap-servers=${kafka.bootstrapServers}",
        ).applyTo(applicationContext.environment)
    }

    companion object {

        // Lazy because we only want it to be initialized when accessed
        private val kafka: KafkaContainer by lazy {
            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"))
        }
    }
}
