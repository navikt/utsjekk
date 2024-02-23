package no.nav.dagpenger.iverksett.status

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.Properties

class KafkaContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        kafkaContainer.start()
        TestPropertyValues.of("KAFKA_BROKERS=${kafkaContainer.bootstrapServers}")
            .applyTo(applicationContext.environment)
    }

    companion object {
        fun connectionConfig(properties: Properties) =
            properties.apply {
                put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.bootstrapServers)
                put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT")
                put(SaslConfigs.SASL_MECHANISM, "PLAIN")
            }

        private val imageName = DockerImageName.parse("confluentinc/cp-kafka:6.2.1")
        private val kafkaContainer: KafkaContainer by lazy {
            KafkaContainer(imageName)
        }
    }
}
