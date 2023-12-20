package no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.cucumber.domeneparser

import io.cucumber.datatable.DataTable
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.cucumber.domeneparser.DomeneparserUtil.groupByBehandlingId
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.cucumber.domeneparser.IdTIlUUIDHolder.behandlingIdTilUUID
import no.nav.dagpenger.iverksett.konsumenter.økonomi.utbetalingsoppdrag.domene.AndelData
import no.nav.dagpenger.kontrakter.felles.StønadType
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.dagpenger.kontrakter.oppdrag.Utbetalingsperiode
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate
import java.util.UUID
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataDagpenger

object OppdragParser {

    fun mapAndeler(dataTable: DataTable): Map<UUID, List<AndelData>> {
        var index = 0L
        return dataTable.groupByBehandlingId().map { (behandlingId, rader) ->
            val andeler = parseAndelder(rader, index)
            index += andeler.size
            behandlingIdTilUUID[behandlingId.toInt()]!! to andeler
        }.toMap()
    }

    private fun parseAndelder(
        rader: List<Map<String, String>>,
        forrigeAndelId: Long,
    ): List<AndelData> {
        val erUtenAndeler = (parseValgfriBoolean(DomenebegrepAndeler.UTEN_ANDELER, rader.first()) ?: false)
        var andelId = forrigeAndelId
        return if (erUtenAndeler) {
            emptyList()
        } else {
            rader.map { mapAndelTilkjentYtelse(it, andelId++) }
        }
    }

    private fun mapAndelTilkjentYtelse(
        rad: Map<String, String>,
        andelId: Long,
    ): AndelData {
        val stønadsdataDagpenger = StønadsdataDagpenger(
            stønadstype = parseValgfriEnum<StønadTypeDagpenger>(DomenebegrepAndeler.YTELSE_TYPE, rad)
                ?: StønadTypeDagpenger.DAGPENGER_ARBEIDSSOKER_ORDINAER,
            ferietillegg = null,
        )
        return AndelData(
            id = andelId.toString(),
            fom = parseDato(Domenebegrep.FRA_DATO, rad),
            tom = parseDato(Domenebegrep.TIL_DATO, rad),
            beløp = parseInt(DomenebegrepAndeler.BELØP, rad),
            stønadsdata = stønadsdataDagpenger,
            periodeId = parseValgfriLong(DomenebegrepUtbetalingsoppdrag.PERIODE_ID, rad),
            forrigePeriodeId = parseValgfriLong(DomenebegrepUtbetalingsoppdrag.FORRIGE_PERIODE_ID, rad),
        )
    }

    fun mapForventetUtbetalingsoppdrag(
        dataTable: DataTable,
    ): List<ForventetUtbetalingsoppdrag> {
        return dataTable.groupByBehandlingId().map { (behandlingId, rader) ->
            val rad = rader.first()
            validerAlleKodeEndringerLike(rader)
            ForventetUtbetalingsoppdrag(
                behandlingId = behandlingId,
                kodeEndring = parseEnum(DomenebegrepUtbetalingsoppdrag.KODE_ENDRING, rad),
                utbetalingsperiode = rader.map { mapForventetUtbetalingsperiode(it) },
            )
        }
    }

    private fun mapForventetUtbetalingsperiode(it: Map<String, String>) =
        ForventetUtbetalingsperiode(
            erEndringPåEksisterendePeriode = parseBoolean(DomenebegrepUtbetalingsoppdrag.ER_ENDRING, it),
            periodeId = parseLong(DomenebegrepUtbetalingsoppdrag.PERIODE_ID, it),
            forrigePeriodeId = parseValgfriLong(DomenebegrepUtbetalingsoppdrag.FORRIGE_PERIODE_ID, it),
            sats = parseInt(DomenebegrepUtbetalingsoppdrag.BELØP, it),
            ytelse = parseValgfriEnum<StønadTypeDagpenger>(DomenebegrepUtbetalingsoppdrag.YTELSE_TYPE, it)
                ?: StønadTypeDagpenger.DAGPENGER_ARBEIDSSOKER_ORDINAER,
            fom = parseDato(Domenebegrep.FRA_DATO, it),
            tom = parseDato(Domenebegrep.TIL_DATO, it),
            opphør = parseValgfriDato(DomenebegrepUtbetalingsoppdrag.OPPHØRSDATO, it),
            satstype = parseValgfriEnum<Utbetalingsperiode.SatsType>(DomenebegrepAndeler.SATSTYPE, it)
                ?: Utbetalingsperiode.SatsType.DAG,
        )

    private fun validerAlleKodeEndringerLike(rader: List<Map<String, String>>) {
        rader.map { parseEnum<Utbetalingsoppdrag.KodeEndring>(DomenebegrepUtbetalingsoppdrag.KODE_ENDRING, it) }
            .zipWithNext().forEach {
                assertThat(it.first).isEqualTo(it.second)
                    .withFailMessage("Alle kodeendringer for en og samme oppdrag må være lik ${it.first} -> ${it.second}")
            }
    }

    private fun parseFødselsnummer(rad: Map<String, String>): String {
        val id = (parseValgfriInt(DomenebegrepAndeler.IDENT, rad) ?: 1).toString()
        return id.padStart(11, '0')
    }
}

enum class DomenebegrepBehandlingsinformasjon(override val nøkkel: String) : Domenenøkkel {
    OPPHØR_FRA("Opphør fra"),
    YTELSE("Ytelse"),
}

enum class DomenebegrepAndeler(override val nøkkel: String) : Domenenøkkel {
    YTELSE_TYPE("Ytelse"),
    UTEN_ANDELER("Uten andeler"),
    BELØP("Beløp"),
    KILDEBEHANDLING_ID("Kildebehandling"),
    IDENT("Ident"),
    SATSTYPE("Satstype"),
}

enum class DomenebegrepUtbetalingsoppdrag(override val nøkkel: String) : Domenenøkkel {
    KODE_ENDRING("Kode endring"),
    ER_ENDRING("Er endring"),
    PERIODE_ID("Periode id"),
    FORRIGE_PERIODE_ID("Forrige periode id"),
    BELØP("Beløp"),
    YTELSE_TYPE("Ytelse"),
    OPPHØRSDATO("Opphørsdato"),
}

data class ForventetUtbetalingsoppdrag(
    val behandlingId: Long,
    val kodeEndring: Utbetalingsoppdrag.KodeEndring,
    val utbetalingsperiode: List<ForventetUtbetalingsperiode>,
)

data class ForventetUtbetalingsperiode(
    val erEndringPåEksisterendePeriode: Boolean,
    val periodeId: Long,
    val forrigePeriodeId: Long?,
    val sats: Int,
    val ytelse: StønadType,
    val fom: LocalDate,
    val tom: LocalDate,
    val opphør: LocalDate?,
    val satstype: Utbetalingsperiode.SatsType,
)
