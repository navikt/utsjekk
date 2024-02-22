package no.nav.dagpenger.iverksett.status

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import java.util.Properties

class AivenConfig(
    private val brokers: List<String>,
    private val truststorePath: String,
    private val truststorePw: String,
    private val keystorePath: String,
    private val keystorePw: String,
) {
    companion object {
        val default: AivenConfig
            get() {
                val env = System.getenv()
                return AivenConfig(
                    brokers = env.getValue("KAFKA_BROKERS").split(',').map(String::trim),
                    truststorePath = env.getValue("KAFKA_TRUSTSTORE_PATH"),
                    truststorePw = env.getValue("KAFKA_CREDSTORE_PASSWORD"),
                    keystorePath = env.getValue("KAFKA_KEYSTORE_PATH"),
                    keystorePw = env.getValue("KAFKA_CREDSTORE_PASSWORD"),
                )
            }
    }

    init {
        check(brokers.isNotEmpty())
    }

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
            put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePw)
            put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystorePath)
            put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePw)
        }
}
