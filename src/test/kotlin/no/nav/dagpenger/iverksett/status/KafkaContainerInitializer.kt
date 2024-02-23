package no.nav.dagpenger.iverksett.status

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

class KafkaContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        kafkaContainer.start()
        kafkaContainer.bootstrapServers
    }

    companion object {
        private val imageName = DockerImageName.parse("confluentinc/cp-kafka:6.2.1")
        private val kafkaContainer = KafkaContainer(imageName)
    }
}
