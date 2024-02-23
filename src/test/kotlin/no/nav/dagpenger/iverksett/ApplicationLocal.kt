package no.nav.dagpenger.iverksett

import no.nav.dagpenger.iverksett.felles.ApplicationConfig
import no.nav.dagpenger.iverksett.felles.database.DbContainerInitializer
import no.nav.dagpenger.iverksett.status.KafkaContainerInitializer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
class ApplicationLocal

fun main(args: Array<String>) {
    SpringApplicationBuilder(ApplicationConfig::class.java)
        .initializers(DbContainerInitializer(), KafkaContainerInitializer())
        .profiles("local", "mock-oppdrag")
        .run(*args)
}
