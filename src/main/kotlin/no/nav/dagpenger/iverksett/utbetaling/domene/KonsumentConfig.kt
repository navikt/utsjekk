package no.nav.dagpenger.iverksett.utbetaling.domene

import no.nav.dagpenger.kontrakter.felles.Fagsystem
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "")
class KonsumentConfig {
    val konsumenter: Map<String, Konsument> = mutableMapOf()

    fun finnFagsystem(klientapp: String) = konsumenter.values.first { it.apper.split(",").contains(klientapp) }.fagsystem
}

data class Konsument(
    val fagsystem: Fagsystem,
    val apper: String = "",
)
