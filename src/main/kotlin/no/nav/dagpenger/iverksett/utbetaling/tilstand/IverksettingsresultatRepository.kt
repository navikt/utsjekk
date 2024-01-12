package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksettingsresultat
import no.nav.dagpenger.iverksett.felles.repository.InsertUpdateRepository
import no.nav.dagpenger.iverksett.felles.repository.RepositoryInterface
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface IverksettingsresultatRepository : RepositoryInterface<Iverksettingsresultat, UUID>, InsertUpdateRepository<Iverksettingsresultat>
