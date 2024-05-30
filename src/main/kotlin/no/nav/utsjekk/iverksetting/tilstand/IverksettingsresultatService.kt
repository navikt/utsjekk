package no.nav.utsjekk.iverksetting.tilstand

import no.nav.utsjekk.iverksetting.domene.Iverksettingsresultat
import no.nav.utsjekk.iverksetting.domene.OppdragResultat
import no.nav.utsjekk.iverksetting.domene.TilkjentYtelse
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class IverksettingsresultatService(private val iverksettingsresultatRepository: IverksettingsresultatRepository) {
    fun opprettTomtResultat(
        fagsystem: Fagsystem,
        sakId: String,
        behandlingId: String,
        iverksettingId: String?,
    ) {
        iverksettingsresultatRepository.insert(Iverksettingsresultat(fagsystem, sakId, behandlingId, iverksettingId))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterTilkjentYtelseForUtbetaling(
        fagsystem: Fagsystem,
        sakId: String,
        behandlingId: String,
        tilkjentYtelseForUtbetaling: TilkjentYtelse,
        iverksettingId: String?,
    ) {
        val iverksettResultat = iverksettingsresultatRepository.findByIdOrThrow(fagsystem, sakId, behandlingId, iverksettingId)
        iverksettingsresultatRepository.update(iverksettResultat.copy(tilkjentYtelseForUtbetaling = tilkjentYtelseForUtbetaling))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterOppdragResultat(
        fagsystem: Fagsystem,
        sakId: String,
        behandlingId: String,
        oppdragResultat: OppdragResultat,
        iverksettingId: String?,
    ) {
        val iverksettResultat = iverksettingsresultatRepository.findByIdOrThrow(fagsystem, sakId, behandlingId, iverksettingId)
        iverksettingsresultatRepository.update(iverksettResultat.copy(oppdragResultat = oppdragResultat))
    }

    fun hentTilkjentYtelse(
        fagsystem: Fagsystem,
        sakId: String,
        behandlingId: String,
        iverksettingId: String?,
    ): TilkjentYtelse? {
        return iverksettingsresultatRepository.findByIdOrNull(fagsystem, sakId, behandlingId, iverksettingId)?.tilkjentYtelseForUtbetaling
    }

    fun hentIverksettingsresultat(
        fagsystem: Fagsystem,
        sakId: String,
        behandlingId: String,
        iverksettingId: String?,
    ): Iverksettingsresultat? {
        return iverksettingsresultatRepository.findByIdOrNull(fagsystem, sakId, behandlingId, iverksettingId)
    }
}
