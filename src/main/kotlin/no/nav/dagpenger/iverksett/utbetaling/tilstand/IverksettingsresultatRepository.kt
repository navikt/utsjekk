package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksettingsresultat
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.kontrakter.felles.objectMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

@Repository
class IverksettingsresultatRepository(private val jdbcTemplate: JdbcTemplate) {
    fun insert(iverksettingsresultat: Iverksettingsresultat): Iverksettingsresultat {
        val sql = "insert into iverksettingsresultat (behandling_id, tilkjentytelseforutbetaling) values (?,to_json(?::json))"
        jdbcTemplate.update(
            sql,
            iverksettingsresultat.behandlingId,
            objectMapper.writeValueAsString(iverksettingsresultat.tilkjentYtelseForUtbetaling),
        )
        return iverksettingsresultat
    }

    fun update(iverksettingsresultat: Iverksettingsresultat) {
        val sql = "update iverksettingsresultat set tilkjentytelseforutbetaling = to_json(?::json), oppdragresultat = to_json(?::json) where behandling_id = ?"
        jdbcTemplate.update(
            sql,
            objectMapper.writeValueAsString(iverksettingsresultat.tilkjentYtelseForUtbetaling),
            objectMapper.writeValueAsString(iverksettingsresultat.oppdragResultat),
            iverksettingsresultat.behandlingId,
        )
    }

    fun findByIdOrNull(behandlingId: UUID): Iverksettingsresultat? {
        val sql = "select * from iverksettingsresultat where behandling_id = ?"
        val resultat = jdbcTemplate.query(sql, IverksettingsresultatRowMapper(), behandlingId)
        return when (resultat.size) {
            0 -> null
            1 -> resultat.first()
            else -> throw IllegalStateException("Fant flere iverksettingsresultat for behandling $behandlingId")
        }
    }

    fun findByIdOrThrow(behandlingId: UUID): Iverksettingsresultat {
        val sql = "select * from iverksettingsresultat where behandling_id = ?"
        val resultat = jdbcTemplate.query(sql, IverksettingsresultatRowMapper(), behandlingId)
        return when (resultat.size) {
            0 -> throw NoSuchElementException("Fant ingen iverksettingsresultat for behandling $behandlingId")
            1 -> resultat.first()
            else -> throw IllegalStateException("Fant flere iverksettingsresultat for behandling $behandlingId")
        }
    }
}

internal class IverksettingsresultatRowMapper : RowMapper<Iverksettingsresultat> {
    override fun mapRow(
        resultSet: ResultSet,
        rowNum: Int,
    ): Iverksettingsresultat {
        return Iverksettingsresultat(
            behandlingId = UUID.fromString(resultSet.getString("behandling_id")),
            tilkjentYtelseForUtbetaling =
                resultSet.getString("tilkjentytelseforutbetaling")?.let {
                    objectMapper.readValue(it, TilkjentYtelse::class.java)
                },
            oppdragResultat = resultSet.getString("oppdragresultat")?.let { objectMapper.readValue(it, OppdragResultat::class.java) },
        )
    }
}
