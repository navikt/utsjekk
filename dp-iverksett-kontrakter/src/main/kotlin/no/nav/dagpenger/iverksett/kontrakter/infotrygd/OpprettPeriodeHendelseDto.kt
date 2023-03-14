package no.nav.dagpenger.iverksett.kontrakter.infotrygd

import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import java.time.LocalDate

/**
 * @param personIdenter alle identer til personen
 */
data class OpprettPeriodeHendelseDto(
    val personIdenter: Set<String>,
    val type: StønadType,
    val perioder: List<Periode>,
)

data class Periode(val startdato: LocalDate, val sluttdato: LocalDate, val fullOvergangsstønad: Boolean)
