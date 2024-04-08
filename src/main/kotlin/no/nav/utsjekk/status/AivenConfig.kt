package no.nav.utsjekk.status

import no.nav.utsjekk.felles.KjørerPåNais
import org.apache.kafka.clients.CommonClientConfigs
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
@KjørerPåNais
class AivenConfig(
    @Value("\${kafka.brokers}") private val kafkaBrokers: String,
    @Value("\${kafka.truststorePath}") private val truststorePath: String,
    @Value("\${kafka.credstorePassword}") private val credstorePassword: String,
    @Value("\${kafka.keystorePath}") private val keystorePath: String,
) {
    private val brokers = kafkaBrokers.split(',').map(String::trim)

    init {
        check(brokers.isNotEmpty())
    }

    @Bean
    fun kafkaProducer(): KafkaProducer<String, String> = KafkaProducer(producerConfig, StringSerializer(), StringSerializer())

    private val producerConfig get() =
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
            put(ProducerConfig.ACKS_CONFIG, "all")
            put(ProducerConfig.LINGER_MS_CONFIG, "0")
            put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1")
        }
}
