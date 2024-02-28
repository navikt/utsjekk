package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksettingsresultat
import no.nav.dagpenger.iverksett.utbetaling.domene.OppdragResultat
import no.nav.dagpenger.iverksett.utbetaling.domene.TilkjentYtelse
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.GeneriskId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class IverksettingsresultatService(private val iverksettingsresultatRepository: IverksettingsresultatRepository) {
    fun opprettTomtResultat(
        fagsystem: Fagsystem,
        sakId: GeneriskId,
        behandlingId: UUID,
        iverksettingId: String?,
    ) {
        iverksettingsresultatRepository.insert(Iverksettingsresultat(fagsystem, sakId, behandlingId, iverksettingId))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterTilkjentYtelseForUtbetaling(
        fagsystem: Fagsystem,
        sakId: GeneriskId,
        behandlingId: UUID,
        tilkjentYtelseForUtbetaling: TilkjentYtelse,
        iverksettingId: String?,
    ) {
        val iverksettResultat = iverksettingsresultatRepository.findByIdOrThrow(fagsystem, sakId, behandlingId, iverksettingId)
        iverksettingsresultatRepository.update(iverksettResultat.copy(tilkjentYtelseForUtbetaling = tilkjentYtelseForUtbetaling))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterOppdragResultat(
        fagsystem: Fagsystem,
        sakId: GeneriskId,
        behandlingId: UUID,
        oppdragResultat: OppdragResultat,
        iverksettingId: String?,
    ) {
        val iverksettResultat = iverksettingsresultatRepository.findByIdOrThrow(fagsystem, sakId, behandlingId, iverksettingId)
        iverksettingsresultatRepository.update(iverksettResultat.copy(oppdragResultat = oppdragResultat))
    }

    fun hentTilkjentYtelse(
        fagsystem: Fagsystem,
        sakId: GeneriskId,
        behandlingId: UUID,
        iverksettingId: String?,
    ): TilkjentYtelse? {
        return iverksettingsresultatRepository.findByIdOrNull(fagsystem, sakId, behandlingId, iverksettingId)?.tilkjentYtelseForUtbetaling
    }

    fun hentIverksettingsresultat(
        fagsystem: Fagsystem,
        sakId: GeneriskId,
        behandlingId: UUID,
        iverksettingId: String?,
    ): Iverksettingsresultat? {
        return iverksettingsresultatRepository.findByIdOrNull(fagsystem, sakId, behandlingId, iverksettingId)
    }
}
