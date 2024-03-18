package no.nav.dagpenger.iverksett.status

import no.nav.dagpenger.iverksett.felles.Profiler
import no.nav.dagpenger.iverksett.initializers.KafkaContainerInitializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.util.Properties

@Configuration
@Profile(Profiler.INTEGRASJONSTEST, Profiler.LOKAL)
class KafkaTestConfig {
    @Bean
    @Primary
    fun kafkaProducer(): KafkaProducer<String, String> =
        KafkaProducer(
            KafkaContainerInitializer.connectionConfig(
                Properties().apply {
                    put(ProducerConfig.ACKS_CONFIG, "all")
                    put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1")
                    put(ProducerConfig.LINGER_MS_CONFIG, "0")
                    put(ProducerConfig.RETRIES_CONFIG, "0")
                },
            ),
            StringSerializer(),
            StringSerializer(),
        )
}
