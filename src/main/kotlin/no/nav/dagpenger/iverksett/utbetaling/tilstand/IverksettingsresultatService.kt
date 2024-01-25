package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksettingsresultat
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class IverksettingsresultatService(private val iverksettingsresultatRepository: IverksettingsresultatRepository) {
    fun opprettTomtResultat(behandlingId: UUID) {
        iverksettingsresultatRepository.insert(Iverksettingsresultat(behandlingId))
    }

    fun oppdaterTilkjentYtelseForUtbetaling(
        behandlingId: UUID,
        tilkjentYtelseForUtbetaling: TilkjentYtelse,
    ) {
        val iverksettResultat = iverksettingsresultatRepository.findByIdOrThrow(behandlingId)
        iverksettingsresultatRepository.update(iverksettResultat.copy(tilkjentYtelseForUtbetaling = tilkjentYtelseForUtbetaling))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterOppdragResultat(
        behandlingId: UUID,
        oppdragResultat: OppdragResultat,
    ) {
        val iverksettResultat = iverksettingsresultatRepository.findByIdOrThrow(behandlingId)
        iverksettingsresultatRepository.update(iverksettResultat.copy(oppdragResultat = oppdragResultat))
    }

    fun hentTilkjentYtelse(behandlingId: UUID): TilkjentYtelse? {
        return iverksettingsresultatRepository.findByIdOrNull(behandlingId)?.tilkjentYtelseForUtbetaling
    }

    fun hentIverksettResultat(behandlingId: UUID): Iverksettingsresultat? {
        return iverksettingsresultatRepository.findByIdOrNull(behandlingId)
    }
}
