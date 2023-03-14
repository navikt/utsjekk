package no.nav.dagpenger.iverksett.kontrakter.infotrygd

/**
 * @param personIdenter alle identer til personen
 */
data class InfotrygdSøkRequest(val personIdenter: Set<String>)
