package no.nav.dagpenger.iverksett.kontrakter.simulering

data class BeriketSimuleringsresultat(
    val detaljer: DetaljertSimuleringResultat,
    val oppsummering: Simuleringsoppsummering
)
