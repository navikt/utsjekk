package no.nav.utsjekk

import no.nav.utsjekk.felles.ApplicationConfig
import no.nav.utsjekk.felles.Profiler
import no.nav.utsjekk.initializers.DbContainerInitializer
import no.nav.utsjekk.initializers.KafkaContainerInitializer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
class ApplicationLocal

fun main(args: Array<String>) {
    SpringApplicationBuilder(ApplicationConfig::class.java)
        .initializers(DbContainerInitializer(), KafkaContainerInitializer())
        .profiles(Profiler.LOKAL, Profiler.MOCK_OPPDRAG, Profiler.MOCK_SIMULERING)
        .run(*args)
}
