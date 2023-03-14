package no.nav.dagpenger.iverksett.kontrakter.infotrygd

import no.nav.dagpenger.iverksett.kontrakter.felles.StønadType
import java.time.LocalDate

/**
 * @param personIdenter alle identer til personen
 */
data class OpprettVedtakHendelseDto(
    val personIdenter: Set<String>,
    val type: StønadType,
    val startdato: LocalDate,
)
