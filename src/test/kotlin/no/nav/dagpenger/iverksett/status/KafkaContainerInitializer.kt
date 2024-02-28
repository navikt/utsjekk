package no.nav.dagpenger.iverksett.status

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.Properties
import java.util.UUID

private const val TOPIC = "test_topic"

class KafkaContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        kafkaContainer.start()
        TestPropertyValues.of("kafka.brokers=${kafkaContainer.bootstrapServers}")
            .applyTo(applicationContext.environment)
        TestPropertyValues.of("kafka.config.topic=$TOPIC")
            .applyTo(applicationContext.environment)
    }

    companion object {
        fun connectionConfig(properties: Properties) =
            properties.apply {
                put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.bootstrapServers)
                put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT")
                put(SaslConfigs.SASL_MECHANISM, "PLAIN")
            }

        private val kafkaContainer: KafkaContainer by lazy {
            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.1"))
                .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
        }

        val singleRecord: ConsumerRecord<String, String>?
            get(): ConsumerRecord<String, String>? {
                val properties =
                    Properties().apply {
                        put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
                        put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString())
                        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
                    }
                val consumer = KafkaConsumer(connectionConfig(properties), StringDeserializer(), StringDeserializer())

                consumer.subscribe(listOf(TOPIC))

                return consumer.poll(Duration.ofSeconds(1)).also {
                    consumer.close()
                }.records(TOPIC)?.first()
            }
    }
}
