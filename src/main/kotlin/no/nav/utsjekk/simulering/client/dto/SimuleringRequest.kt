package no.nav.utsjekk.simulering.client.dto

import no.nav.utsjekk.kontrakter.felles.Ident
import no.nav.utsjekk.kontrakter.felles.Personident
import java.time.LocalDate

data class SimuleringRequest(
    val fagområde: String,
    val fagsystemId: String,
    val personident: Personident,
    val mottaker: Ident,
    val endringskode: String,
    val saksbehandler: String,
    val utbetalingsfrekvens: String,
    val utbetalingslinjer: List<Utbetalingslinje>,
)

data class Utbetalingslinje(
    val delytelseId: String,
    val endringskode: String,
    val klassekode: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val sats: Int,
    val grad: Grad = Grad(type = GradType.UFOR, prosent = null),
    val refDelytelseId: String?,
    val refFagsystemId: String?,
    val datoStatusFom: LocalDate?,
    val statuskode: String?,
    val satstype: String,
    val utbetalesTil: String,
) {
    data class Grad(val type: GradType, val prosent: Int?)

    enum class GradType { UFOR }
}
