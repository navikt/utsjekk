package no.nav.utsjekk.simulering.client.dto

import no.nav.utsjekk.kontrakter.felles.Personident
import java.time.LocalDate

data class SimuleringRequest(
    val fagområde: String,
    val fagsystemId: String,
    val personident: Personident,
    val erFørsteUtbetalingPåSak: Boolean,
    val saksbehandler: String,
    val utbetalingsperioder: List<Utbetalingsperiode>,
)

data class Utbetalingsperiode(
    val periodeId: String,
    val forrigePeriodeId: String?,
    val erEndringPåEksisterendePeriode: Boolean,
    val klassekode: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val sats: Int,
    val satstype: String,
    val opphør: Opphør?,
    val utbetalesTil: String,
)

data class Opphør(val fom: LocalDate)
