package no.nav.dagpenger.iverksett.utbetaling.tilstand

import no.nav.dagpenger.iverksett.utbetaling.domene.Iverksettingsresultat
import no.nav.dagpenger.iverksett.utbetaling.tilstand.konfig.InsertUpdateRepository
import no.nav.dagpenger.iverksett.utbetaling.tilstand.konfig.RepositoryInterface
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface IverksettingsresultatRepository : RepositoryInterface<Iverksettingsresultat, UUID>, InsertUpdateRepository<Iverksettingsresultat>
