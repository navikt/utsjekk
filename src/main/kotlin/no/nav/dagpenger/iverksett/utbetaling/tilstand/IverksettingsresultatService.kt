package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksettingsresultat
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class IverksettingsresultatService(private val iverksettingsresultatRepository: IverksettingsresultatRepository) {
    fun opprettTomtResultat(
        fagsystem: Fagsystem,
        behandlingId: UUID,
        iverksettingId: String? = null,
    ) {
        iverksettingsresultatRepository.insert(Iverksettingsresultat(fagsystem, behandlingId, iverksettingId))
    }

    fun oppdaterTilkjentYtelseForUtbetaling(
        fagsystem: Fagsystem,
        behandlingId: UUID,
        tilkjentYtelseForUtbetaling: TilkjentYtelse,
        iverksettingId: String? = null,
    ) {
        val iverksettResultat = iverksettingsresultatRepository.findByIdOrThrow(fagsystem, behandlingId, iverksettingId)
        iverksettingsresultatRepository.update(iverksettResultat.copy(tilkjentYtelseForUtbetaling = tilkjentYtelseForUtbetaling))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterOppdragResultat(
        fagsystem: Fagsystem,
        behandlingId: UUID,
        oppdragResultat: OppdragResultat,
        iverksettingId: String? = null,
    ) {
        val iverksettResultat = iverksettingsresultatRepository.findByIdOrThrow(fagsystem, behandlingId, iverksettingId)
        iverksettingsresultatRepository.update(iverksettResultat.copy(oppdragResultat = oppdragResultat))
    }

    fun hentTilkjentYtelse(
        fagsystem: Fagsystem,
        behandlingId: UUID,
        iverksettingId: String? = null,
    ): TilkjentYtelse? {
        return iverksettingsresultatRepository.findByIdOrNull(fagsystem, behandlingId, iverksettingId)?.tilkjentYtelseForUtbetaling
    }

    fun hentIverksettResultat(
        fagsystem: Fagsystem,
        behandlingId: UUID,
        iverksettingId: String? = null,
    ): Iverksettingsresultat? {
        return iverksettingsresultatRepository.findByIdOrNull(fagsystem, behandlingId, iverksettingId)
    }
}
