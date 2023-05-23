package no.nav.dagpenger.iverksett.kontrakter.infotrygd

import no.nav.dagpenger.kontrakter.utbetaling.StønadType

/**
 * @param personIdenter alle identer til personen
 */
data class OpprettStartBehandlingHendelseDto(
    val personIdenter: Set<String>,
    val type: StønadType,
)
