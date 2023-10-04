package no.nav.dagpenger.iverksett

import no.nav.dagpenger.iverksett.infrastruktur.configuration.ApplicationConfig
import no.nav.dagpenger.iverksett.infrastruktur.database.DbContainerInitializer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
class ApplicationLocal

fun main(args: Array<String>) {
    SpringApplicationBuilder(ApplicationConfig::class.java)
        .initializers(DbContainerInitializer())
        .profiles(
            "local",
            // "mock-oppdrag",
        )
        .run(*args)
}
