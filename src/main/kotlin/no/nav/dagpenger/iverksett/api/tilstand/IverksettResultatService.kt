package no.nav.dagpenger.iverksett.api.tilstand

import java.util.UUID
import no.nav.dagpenger.iverksett.api.domene.IverksettResultat
import no.nav.dagpenger.iverksett.api.domene.OppdragResultat
import no.nav.dagpenger.iverksett.api.domene.TilkjentYtelse
import no.nav.dagpenger.iverksett.infrastruktur.repository.findByIdOrThrow
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class IverksettResultatService(private val iverksettResultatRepository: IverksettResultatRepository) {

    fun opprettTomtResultat(behandlingId: UUID) {
        iverksettResultatRepository.insert(IverksettResultat(behandlingId))
    }

    fun oppdaterTilkjentYtelseForUtbetaling(behandlingId: UUID, tilkjentYtelseForUtbetaling: TilkjentYtelse) {
        val iverksettResultat = iverksettResultatRepository.findByIdOrThrow(behandlingId)
        iverksettResultatRepository.update(iverksettResultat.copy(tilkjentYtelseForUtbetaling = tilkjentYtelseForUtbetaling))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun oppdaterOppdragResultat(behandlingId: UUID, oppdragResultat: OppdragResultat) {
        val iverksettResultat = iverksettResultatRepository.findByIdOrThrow(behandlingId)
        iverksettResultatRepository.update(iverksettResultat.copy(oppdragResultat = oppdragResultat))
    }

    fun hentTilkjentYtelse(behandlingId: UUID): TilkjentYtelse? {
        return iverksettResultatRepository.findByIdOrNull(behandlingId)?.tilkjentYtelseForUtbetaling
    }

    fun hentTilkjentYtelse(behandlingId: Set<UUID>): Map<UUID, TilkjentYtelse> {
        val iverksettResultater = iverksettResultatRepository.findAllById(behandlingId)
        val tilkjenteYtelser = iverksettResultater.filter { it.tilkjentYtelseForUtbetaling != null }
            .associate { it.behandlingId to it.tilkjentYtelseForUtbetaling!! }
        if (behandlingId.size > tilkjenteYtelser.size) {
            error("Finner ikke tilkjent ytelse til behandlingIder=${behandlingId.minus(tilkjenteYtelser.keys)}}")
        }
        return tilkjenteYtelser
    }

    fun hentIverksettResultat(behandlingId: UUID): IverksettResultat? {
        return iverksettResultatRepository.findByIdOrNull(behandlingId)
    }
}
