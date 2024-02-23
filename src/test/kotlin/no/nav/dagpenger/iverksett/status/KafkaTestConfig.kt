package no.nav.dagpenger.iverksett.status

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.util.Properties

@Configuration
@Profile("servertest", "local")
class KafkaTestConfig {
    @Bean
    @Primary
    fun kafkaProducer(): KafkaProducer<String, String> =
        KafkaProducer(
            KafkaContainerInitializer.connectionConfig(
                Properties(),
            ),
            StringSerializer(),
            StringSerializer(),
        )
}
