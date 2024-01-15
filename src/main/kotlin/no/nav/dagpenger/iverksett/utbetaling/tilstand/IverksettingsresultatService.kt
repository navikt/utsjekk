package no.nav.dagpenger.iverksett.utbetaling.tilstand

import java.util.UUID
import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksettingsresultat
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.utbetaling.tilstand.konfig.findByIdOrThrow
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class IverksettingsresultatService(private val iverksettingsresultatRepository: IverksettingsresultatRepository) {

    fun opprettTomtResultat(behandlingId: UUID) {
        iverksettingsresultatRepository.insert(Iverksettingsresultat(behandlingId))
    }

    fun oppdaterTilkjentYtelseForUtbetaling(behandlingId: UUID, tilkjentYtelseForUtbetaling: TilkjentYtelse) {
        val iverksettResultat = iverksettingsresultatRepository.findByIdOrThrow(behandlingId)
        iverksettingsresultatRepository.update(iverksettResultat.copy(tilkjentYtelseForUtbetaling = tilkjentYtelseForUtbetaling))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterOppdragResultat(behandlingId: UUID, oppdragResultat: OppdragResultat) {
        val iverksettResultat = iverksettingsresultatRepository.findByIdOrThrow(behandlingId)
        iverksettingsresultatRepository.update(iverksettResultat.copy(oppdragResultat = oppdragResultat))
    }

    fun hentTilkjentYtelse(behandlingId: UUID): TilkjentYtelse? {
        return iverksettingsresultatRepository.findByIdOrNull(behandlingId)?.tilkjentYtelseForUtbetaling
    }

    fun hentTilkjentYtelse(behandlingId: Set<UUID>): Map<UUID, TilkjentYtelse> {
        val iverksettResultater = iverksettingsresultatRepository.findAllById(behandlingId)
        val tilkjenteYtelser = iverksettResultater.filter { it.tilkjentYtelseForUtbetaling != null }
            .associate { it.behandlingId to it.tilkjentYtelseForUtbetaling!! }
        if (behandlingId.size > tilkjenteYtelser.size) {
            error("Finner ikke tilkjent ytelse til behandlingIder=${behandlingId.minus(tilkjenteYtelser.keys)}}")
        }
        return tilkjenteYtelser
    }

    fun hentIverksettResultat(behandlingId: UUID): Iverksettingsresultat? {
        return iverksettingsresultatRepository.findByIdOrNull(behandlingId)
    }
}
