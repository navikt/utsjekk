package no.nav.dagpenger.iverksett.infrastruktur.configuration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.treeToValue
import no.nav.dagpenger.iverksett.api.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.api.domene.Iverksett
import no.nav.dagpenger.iverksett.api.domene.OppdragResultat
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
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
    "no.nav.dagpenger",
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
                TilkjentYtelseTilPGobjectConverter(),
                PGobjectTilTilkjentYtelseConverter(),
                JsonNodeTilPGobjectConverter(),
                PGobjectTilJsonNodeConverter(),
                OppdragResultatTilPGobjectConverter(),
                PGobjectTilOppdragResultatConverter(),
                IverksettDataTilPGobjectConverter(),
                PGobjectConverterTilIverksettData(),
            ),
        )
    }

    open class DomainTilPGobjectConverter<T : Any> : Converter<T, PGobject> {

        override fun convert(data: T): PGobject =
            PGobject().apply {
                type = "json"
                value = objectMapper.writeValueAsString(data)
            }
    }

    @WritingConverter
    class TilkjentYtelseTilPGobjectConverter : DomainTilPGobjectConverter<TilkjentYtelse>()

    @ReadingConverter
    class PGobjectTilTilkjentYtelseConverter : Converter<PGobject, TilkjentYtelse> {

        override fun convert(pGobject: PGobject): TilkjentYtelse {
            return objectMapper.readValue(pGobject.value!!)
        }
    }

    @WritingConverter
    class OppdragResultatTilPGobjectConverter : DomainTilPGobjectConverter<OppdragResultat>()

    @ReadingConverter
    class PGobjectTilOppdragResultatConverter : Converter<PGobject, OppdragResultat> {

        override fun convert(pGobject: PGobject): OppdragResultat {
            return objectMapper.readValue(pGobject.value!!)
        }
    }

    @WritingConverter
    class JsonNodeTilPGobjectConverter : DomainTilPGobjectConverter<JsonNode>()

    @ReadingConverter
    class PGobjectTilJsonNodeConverter : Converter<PGobject, JsonNode?> {

        override fun convert(pGobject: PGobject): JsonNode? {
            return null
        }
    }

    @WritingConverter
    class IverksettDataTilPGobjectConverter : DomainTilPGobjectConverter<Iverksett>()

    @ReadingConverter
    class PGobjectConverterTilIverksettData : Converter<PGobject, Iverksett> {

        override fun convert(pGobject: PGobject): Iverksett {
            val fagsakNode = objectMapper.readTree(pGobject.value).findValue("fagsak")
            val fagsakdetaljer: Fagsakdetaljer = objectMapper.treeToValue(fagsakNode)
            return when (fagsakdetaljer.stønadstype) {
                StønadTypeDagpenger.DAGPENGER_ARBEIDSSOKER_ORDINAER,
                StønadTypeDagpenger.DAGPENGER_PERMITTERING_ORDINAER,
                StønadTypeDagpenger.DAGPENGER_PERMITTERING_FISKEINDUSTRI,
                StønadTypeDagpenger.DAGPENGER_EOS
                -> objectMapper
                    .readValue(pGobject.value, Iverksett::class.java)
                else
                -> objectMapper
                    .readValue(pGobject.value, Iverksett::class.java)
            }
        }
    }
}
