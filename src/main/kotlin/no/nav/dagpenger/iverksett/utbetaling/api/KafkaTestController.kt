package no.nav.dagpenger.iverksett.utbetaling.api

import no.nav.dagpenger.iverksett.status.AivenConfig
import no.nav.security.token.support.core.api.Unprotected
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@RestController
@Unprotected
@RequestMapping(
    path = ["/api/kafka"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class KafkaTestController {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private const val TOPIC = "iverksetting-status-v1"
    }

    @PostMapping
    fun produserMelding(): ResponseEntity<Unit> {
        val producer = KafkaProducer(AivenConfig.default.producerConfig(), StringSerializer(), StringSerializer())
        val metadata = producer.send(ProducerRecord(TOPIC, "hvasomhelst", "dette er en melding")).get()
        @Suppress("ktlint:standard:max-line-length")
        logger.info(
            "Publiserte ident kl ${metadata.timestamp()} til topic ${metadata.topic()} på partition ${metadata.partition()} med offset ${metadata.offset()}",
        )

        return ResponseEntity.status(HttpStatus.OK).build()
    }

    @GetMapping
    fun hentMeldinger(): ResponseEntity<List<String>> {
        val consumer =
            KafkaConsumer(AivenConfig.default.consumerConfig("gruppe"), StringDeserializer(), StringDeserializer())
        consumer.subscribe(listOf(TOPIC))
        val records = consumer.poll(Duration.ofSeconds(1)).map { it.value() }

        return ResponseEntity(records, HttpStatus.OK)
    }
}
