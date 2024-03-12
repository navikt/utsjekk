package no.nav.dagpenger.iverksett.status

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.admin.OffsetSpec
import org.apache.kafka.clients.admin.RecordsToDelete
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        kafkaContainer.start().also {
            createTestTopic()
            updateTestProperties(applicationContext)
        }

        logger.info("Kafka startet lokalt på localhost:${kafkaContainer.firstMappedPort}")
    }

    private fun createTestTopic() {
        AdminClient.create(connectionConfig())
            .createTopics(listOf(NewTopic(TOPIC, 1, 1)))
    }

    private fun updateTestProperties(applicationContext: ConfigurableApplicationContext) {
        TestPropertyValues.of("kafka.brokers=${kafkaContainer.bootstrapServers}")
            .applyTo(applicationContext.environment)
        TestPropertyValues.of("kafka.config.topic=$TOPIC")
            .applyTo(applicationContext.environment)
    }

    companion object {
        fun connectionConfig(properties: Properties = Properties()) =
            properties.apply {
                put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.bootstrapServers)
                put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT")
                put(SaslConfigs.SASL_MECHANISM, "PLAIN")
            }

        private val kafkaContainer: KafkaContainer by lazy {
            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.1"))
        }

        fun deleteAllRecords() {
            val adminClient = AdminClient.create(connectionConfig())
            val partition = TopicPartition(TOPIC, 0)
            val offset = adminClient.listOffsets(mapOf(partition to OffsetSpec.latest())).all().get().values.first().offset()

            adminClient.deleteRecords(mapOf(partition to RecordsToDelete.beforeOffset(offset))).all().get()
            adminClient.close()
        }

        fun getAllRecords(): Iterable<ConsumerRecord<String, String>> =
            try {
                createConsumer().let { consumer ->
                    consumer.poll(Duration.ofSeconds(10)).also {
                        consumer.close()
                    }.records(TOPIC)
                }
            } catch (e: NoSuchElementException) {
                throw RuntimeException("Fant ingen meldinger på topic $TOPIC")
            }

        private fun createConsumer() =
            Properties().apply {
                put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
                put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString())
                put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            }.let { properties ->
                KafkaConsumer(connectionConfig(properties), StringDeserializer(), StringDeserializer()).also { consumer ->
                    consumer.subscribe(listOf(TOPIC))
                }
            }
    }
}
