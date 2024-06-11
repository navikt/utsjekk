package no.nav.utsjekk.simulering.client.dto

import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.felles.Satstype
import no.nav.utsjekk.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.utsjekk.kontrakter.oppdrag.Utbetalingsperiode

object Mapper {
    fun Utbetalingsoppdrag.tilSimuleringRequest(): SimuleringRequest {
        return SimuleringRequest(
            sakId = this.saksnummer,
            fagområde = this.fagsystem.kode,
            personident = Personident(this.aktør),
            erFørsteUtbetalingPåSak = this.erFørsteUtbetalingPåSak,
            saksbehandler = this.saksbehandlerId,
            utbetalingsperioder = this.utbetalingsperiode.map { it.tilUtbetalingslinje() },
        )
    }

    private fun Utbetalingsperiode.tilUtbetalingslinje(): no.nav.utsjekk.simulering.client.dto.Utbetalingsperiode {
        return Utbetalingsperiode(
            periodeId = this.periodeId.toString(),
            forrigePeriodeId = this.forrigePeriodeId?.toString(),
            erEndringPåEksisterendePeriode = this.erEndringPåEksisterendePeriode,
            klassekode = this.klassifisering,
            fom = this.fom,
            tom = this.tom,
            sats = this.sats.toInt(),
            satstype = this.satstype.tilSimuleringFormat(),
            opphør = this.opphør?.let { Opphør(it.fom) },
            utbetalesTil = this.utbetalesTil,
        )
    }

    private fun Satstype.tilSimuleringFormat(): String =
        when (this) {
            Satstype.DAGLIG -> "DAG"
            Satstype.MÅNEDLIG -> "MND"
            Satstype.ENGANGS -> "ENG"
        }
}
