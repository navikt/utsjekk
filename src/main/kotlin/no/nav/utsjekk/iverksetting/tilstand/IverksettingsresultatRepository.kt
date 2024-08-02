package no.nav.utsjekk.iverksetting.tilstand

import no.nav.utsjekk.iverksetting.domene.GammelTilkjentYtelse
import no.nav.utsjekk.iverksetting.domene.GammeltIverksettingsresultat
import no.nav.utsjekk.iverksetting.domene.Iverksettingsresultat
import no.nav.utsjekk.iverksetting.domene.OppdragResultat
import no.nav.utsjekk.iverksetting.domene.TilkjentYtelse
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.objectMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Suppress("ktlint:standard:max-line-length")
@Repository
class IverksettingsresultatRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun insert(iverksettingsresultat: Iverksettingsresultat): Iverksettingsresultat {
        val sql =
            "insert into iverksettingsresultat (fagsystem, sakId, behandling_id, iverksetting_id, tilkjentytelseforutbetaling) " +
                "values (?,?,?,?,to_json(?::json))"
        jdbcTemplate.update(
            sql,
            iverksettingsresultat.fagsystem.name,
            iverksettingsresultat.sakId,
            iverksettingsresultat.behandlingId,
            iverksettingsresultat.iverksettingId,
            objectMapper.writeValueAsString(iverksettingsresultat.tilkjentYtelseForUtbetaling),
        )
        return iverksettingsresultat
    }

    fun update(iverksettingsresultat: Iverksettingsresultat) {
        if (iverksettingsresultat.iverksettingId != null) {
            val sql =
                "update iverksettingsresultat set tilkjentytelseforutbetaling = to_json(?::json), oppdragresultat = to_json(?::json)" +
                    " where behandling_id = ? and fagsystem = ? and sakId = ? and iverksetting_id = ?"
            jdbcTemplate.update(
                sql,
                objectMapper.writeValueAsString(iverksettingsresultat.tilkjentYtelseForUtbetaling),
                objectMapper.writeValueAsString(iverksettingsresultat.oppdragResultat),
                iverksettingsresultat.behandlingId,
                iverksettingsresultat.fagsystem.name,
                iverksettingsresultat.sakId,
                iverksettingsresultat.iverksettingId,
            )
        } else {
            val sql =
                "update iverksettingsresultat set tilkjentytelseforutbetaling = to_json(?::json), oppdragresultat = to_json(?::json)" +
                    " where behandling_id = ? and fagsystem = ? and sakId = ? and iverksetting_id is null"
            jdbcTemplate.update(
                sql,
                objectMapper.writeValueAsString(iverksettingsresultat.tilkjentYtelseForUtbetaling),
                objectMapper.writeValueAsString(iverksettingsresultat.oppdragResultat),
                iverksettingsresultat.behandlingId,
                iverksettingsresultat.fagsystem.name,
                iverksettingsresultat.sakId,
            )
        }
    }

    fun findByIdOrNull(
        fagsystem: Fagsystem,
        sakId: String,
        behandlingId: String,
        iverksettingId: String? = null,
    ): Iverksettingsresultat? {
        val resultat =
            hentIverksettingsresultater(iverksettingId, fagsystem, sakId, behandlingId)
        return when (resultat.size) {
            0 -> null
            1 -> resultat.first()
            else -> throw IllegalStateException(
                "Fant flere iverksettingsresultat for fagsystem $fagsystem, sak $sakId, behandling $behandlingId og iverksetting $iverksettingId",
            )
        }
    }

    fun findByIdOrThrow(
        fagsystem: Fagsystem,
        sakId: String,
        behandlingId: String,
        iverksettingId: String?,
    ): Iverksettingsresultat {
        val resultat =
            hentIverksettingsresultater(
                iverksettingId = iverksettingId,
                fagsystem = fagsystem,
                sakId = sakId,
                behandlingId = behandlingId,
            )
        return when (resultat.size) {
            0 -> throw NoSuchElementException(
                "Fant ingen iverksettingsresultat for fagsystem $fagsystem, sak sakId, behandling $behandlingId og iverksetting $iverksettingId",
            )

            1 -> resultat.first()
            else -> throw IllegalStateException(
                "Fant flere iverksettingsresultat for fagsystem $fagsystem, sak $sakId, behandling $behandlingId og iverksetting $iverksettingId",
            )
        }
    }

    private fun hentIverksettingsresultater(
        iverksettingId: String?,
        fagsystem: Fagsystem,
        sakId: String,
        behandlingId: String,
    ): List<Iverksettingsresultat> =
        if (iverksettingId != null) {
            val sql =
                "select * from iverksettingsresultat where fagsystem = ? and sakId = ? and behandling_id = ? and iverksetting_id = ?"
            jdbcTemplate.query(
                sql,
                IverksettingsresultatRowMapper(),
                fagsystem.name,
                sakId,
                behandlingId,
                iverksettingId,
            )
        } else {
            val sql =
                "select * from iverksettingsresultat where fagsystem = ? and sakId = ? " +
                    "and behandling_id = ? and iverksetting_id is null"
            jdbcTemplate.query(sql, IverksettingsresultatRowMapper(), fagsystem.name, sakId, behandlingId)
        }

    fun hentGammelVersjon(): List<GammeltIverksettingsresultat> =
        jdbcTemplate.query(
            "select * from iverksettingsresultat",
            GammelIverksettingsresultatRowMapper(),
        )
}

internal class IverksettingsresultatRowMapper : RowMapper<Iverksettingsresultat> {
    override fun mapRow(
        resultSet: ResultSet,
        rowNum: Int,
    ): Iverksettingsresultat =
        Iverksettingsresultat(
            fagsystem = Fagsystem.valueOf(resultSet.getString("fagsystem")),
            sakId = resultSet.getString("sakId"),
            behandlingId = resultSet.getString("behandling_id"),
            iverksettingId = resultSet.getString("iverksetting_id"),
            tilkjentYtelseForUtbetaling =
                resultSet.getString("tilkjentytelseforutbetaling")?.let {
                    objectMapper.readValue(it, TilkjentYtelse::class.java)
                },
            oppdragResultat =
                resultSet
                    .getString("oppdragresultat")
                    ?.let { objectMapper.readValue(it, OppdragResultat::class.java) },
        )
}

internal class GammelIverksettingsresultatRowMapper : RowMapper<GammeltIverksettingsresultat> {
    override fun mapRow(
        resultSet: ResultSet,
        rowNum: Int,
    ): GammeltIverksettingsresultat =
        GammeltIverksettingsresultat(
            fagsystem = Fagsystem.valueOf(resultSet.getString("fagsystem")),
            sakId = resultSet.getString("sakId"),
            behandlingId = resultSet.getString("behandling_id"),
            iverksettingId = resultSet.getString("iverksetting_id"),
            tilkjentYtelseForUtbetaling =
                resultSet.getString("tilkjentytelseforutbetaling")?.let {
                    objectMapper.readValue(it, GammelTilkjentYtelse::class.java)
                },
            oppdragResultat =
                resultSet
                    .getString("oppdragresultat")
                    ?.let { objectMapper.readValue(it, OppdragResultat::class.java) },
        )
}
