package no.nav.dagpenger.iverksett.status

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Properties

@Configuration
class AivenConfig(
    @Value("\${KAFKA_BROKERS}") private val kafkaBrokers: String,
    @Value("\${KAFKA_TRUSTSTORE_PATH}") private val truststorePath: String,
    @Value("\${KAFKA_CREDSTORE_PASSWORD}") private val credstorePassword: String,
    @Value("\${KAFKA_KEYSTORE_PATH}") private val keystorePath: String,
) {
    private val brokers = kafkaBrokers.split(',').map(String::trim)

    init {
        check(brokers.isNotEmpty())
    }

    @Bean
    fun kafkaProducer(): KafkaProducer<String, String> = KafkaProducer(producerConfig(), StringSerializer(), StringSerializer())

    fun producerConfig(properties: Properties? = null) =
        Properties().apply {
            putAll(kafkaBaseConfig())
            put(ProducerConfig.ACKS_CONFIG, "all")
            put(ProducerConfig.LINGER_MS_CONFIG, "0")
            put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1")
            properties?.let { putAll(it) }
        }

    fun consumerConfig(
        groupId: String,
        properties: Properties? = null,
    ) = Properties().apply {
        putAll(kafkaBaseConfig())
        put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
        properties?.let { putAll(it) }
        put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    }

    private fun kafkaBaseConfig() =
        Properties().apply {
            put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, brokers)
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name)
            put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "")
            put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "jks")
            put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12")
            put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststorePath)
            put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, credstorePassword)
            put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystorePath)
            put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, credstorePassword)
        }
}
