package no.nav.dagpenger.iverksett.infrastruktur.configuration

import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.treeToValue
import no.nav.dagpenger.iverksett.api.domene.Fagsakdetaljer
import no.nav.dagpenger.iverksett.api.domene.IverksettDagpenger
import no.nav.dagpenger.iverksett.api.domene.OppdragResultat
import no.nav.dagpenger.iverksett.api.domene.TilbakekrevingResultat
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.Brevmottakere
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.DistribuerBrevResultatMap
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.JournalpostResultatMap
import no.nav.dagpenger.kontrakter.felles.StønadType
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
                OppdragResultatTilPGobjectConverter(),
                PGobjectTilOppdragResultatConverter(),
                JournalpostResultatMapTilPGobjectConverter(),
                PGobjectTilJournalpostResultatMapConverter(),
                VedtaksbrevResultatMapTilPGobjectConverter(),
                PGobjectTilVedtaksbrevResultatMapConverter(),
                TilbakekrevingResultatTilPGobjectConverter(),
                PGobjectTilTilbakekrevingResultatConverter(),
                IverksettDataTilPGobjectConverter(),
                PGobjectConverterTilIverksettData(),
                BrevmottakereTilStringConverter(),
                StringTilBrevmottakereConverter(),
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
    class JournalpostResultatMapTilPGobjectConverter : DomainTilPGobjectConverter<JournalpostResultatMap>()

    @ReadingConverter
    class PGobjectTilJournalpostResultatMapConverter : Converter<PGobject, JournalpostResultatMap> {

        override fun convert(pGobject: PGobject): JournalpostResultatMap {
            return objectMapper.readValue(pGobject.value!!)
        }
    }

    @WritingConverter
    class VedtaksbrevResultatMapTilPGobjectConverter : DomainTilPGobjectConverter<DistribuerBrevResultatMap>()

    @ReadingConverter
    class PGobjectTilVedtaksbrevResultatMapConverter : Converter<PGobject, DistribuerBrevResultatMap> {

        override fun convert(pGobject: PGobject): DistribuerBrevResultatMap {
            return objectMapper.readValue(pGobject.value!!)
        }
    }

    @WritingConverter
    class TilbakekrevingResultatTilPGobjectConverter : DomainTilPGobjectConverter<TilbakekrevingResultat>()

    @ReadingConverter
    class PGobjectTilTilbakekrevingResultatConverter : Converter<PGobject, TilbakekrevingResultat> {

        override fun convert(pGobject: PGobject): TilbakekrevingResultat {
            return objectMapper.readValue(pGobject.value!!)
        }
    }

    @WritingConverter
    class BrevmottakereTilStringConverter : Converter<Brevmottakere, PGobject> {

        override fun convert(data: Brevmottakere): PGobject =
            PGobject().apply {
                type = "json"
                value = objectMapper.writeValueAsString(data)
            }
    }

    @ReadingConverter
    class StringTilBrevmottakereConverter : Converter<PGobject, Brevmottakere> {

        override fun convert(pgObject: PGobject): Brevmottakere {
            return objectMapper.readValue(pgObject.value!!)
        }
    }

    @WritingConverter
    class IverksettDataTilPGobjectConverter : DomainTilPGobjectConverter<IverksettDagpenger>()

    @ReadingConverter
    class PGobjectConverterTilIverksettData : Converter<PGobject, IverksettDagpenger> {

        override fun convert(pGobject: PGobject): IverksettDagpenger {
            val fagsakNode = no.nav.familie.kontrakter.felles.objectMapper.readTree(pGobject.value).findValue("fagsak")
            val fagsakdetaljer: Fagsakdetaljer = no.nav.familie.kontrakter.felles.objectMapper.treeToValue(fagsakNode)
            return when (fagsakdetaljer.stønadstype) {
                StønadType.DAGPENGER_ARBEIDSSOKER_ORDINAER,
                StønadType.DAGPENGER_PERMITTERING_ORDINAER,
                StønadType.DAGPENGER_PERMITTERING_FISKEINDUSTRI,
                StønadType.DAGPENGER_EOS,
                -> no.nav.familie.kontrakter.felles.objectMapper
                    .readValue(pGobject.value, IverksettDagpenger::class.java)
            }
        }
    }
}
