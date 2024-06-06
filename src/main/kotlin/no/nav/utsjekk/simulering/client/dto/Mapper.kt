package no.nav.utsjekk.simulering.client.dto

import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.utsjekk.kontrakter.oppdrag.Utbetalingsperiode

object Mapper {
    fun Utbetalingsoppdrag.tilSimuleringRequest(): SimuleringRequest {
        return SimuleringRequest(
            fagsystemId = this.saksnummer,
            fagområde = this.fagsystem.kode,
            personident = Personident(this.aktør),
            mottaker = Personident(this.aktør),
            endringskode = if (this.erFørsteUtbetalingPåSak) "NY" else "ENDR",
            saksbehandler = this.saksbehandlerId,
            utbetalingsfrekvens = "MND",
            utbetalingslinjer = this.utbetalingsperiode.map { it.tilUtbetalingslinje(this.saksnummer) },
        )
    }

    private fun Utbetalingsperiode.tilUtbetalingslinje(sakId: String): Utbetalingslinje {
        return Utbetalingslinje(
            delytelseId = "$sakId#${this.periodeId}",
            endringskode = if (this.erEndringPåEksisterendePeriode) "ENDR" else "NY",
            klassekode = this.klassifisering,
            fom = this.fom,
            tom = this.tom,
            sats = this.sats.toInt(),
            satstype = this.satstype.name,
            refDelytelseId = "$sakId#${this.forrigePeriodeId}",
            refFagsystemId = sakId,
            datoStatusFom = this.opphør?.fom,
            statuskode = this.opphør?.let { "OPPH" },
            utbetalesTil = this.utbetalesTil,
        )
    }
}
