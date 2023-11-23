package no.nav.dagpenger.iverksett.api.tilstand

import no.nav.dagpenger.iverksett.api.domene.Iverksettingsresultat
import no.nav.dagpenger.iverksett.infrastruktur.repository.InsertUpdateRepository
import no.nav.dagpenger.iverksett.infrastruktur.repository.RepositoryInterface
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface IverksettingsresultatRepository : RepositoryInterface<Iverksettingsresultat, UUID>, InsertUpdateRepository<Iverksettingsresultat>
