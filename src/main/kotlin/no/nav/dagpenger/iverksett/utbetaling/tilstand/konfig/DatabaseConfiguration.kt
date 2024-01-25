package no.nav.dagpenger.iverksett.utbetaling.tilstand.konfig

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.familie.prosessering.PropertiesWrapperTilStringConverter
import no.nav.familie.prosessering.StringTilPropertiesWrapperConverter
import org.postgresql.util.PGobject
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

@Configuration
@EnableJdbcRepositories(
    "no.nav.familie.prosessering",
)
class DatabaseConfiguration : AbstractJdbcConfiguration() {
    @Bean
    fun namedParameterJdbcTemplate(dataSource: DataSource): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(dataSource)
    }

    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions {
        return JdbcCustomConversions(
            listOf(
                PropertiesWrapperTilStringConverter(),
                StringTilPropertiesWrapperConverter(),
                JsonNodeTilPGobjectConverter(),
                PGobjectTilJsonNodeConverter(),
            ),
        )
    }

    @WritingConverter
    class JsonNodeTilPGobjectConverter : Converter<JsonNode, PGobject> {
        override fun convert(data: JsonNode): PGobject =
            PGobject().apply {
                type = "json"
                value = objectMapper.writeValueAsString(data)
            }
    }

    @ReadingConverter
    class PGobjectTilJsonNodeConverter : Converter<PGobject, JsonNode?> {
        override fun convert(pGobject: PGobject): JsonNode? {
            return pGobject.value?.let { objectMapper.readValue(it) }
        }
    }
}
