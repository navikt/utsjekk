package no.nav.utsjekk.simulering.client.dto

import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.felles.Satstype
import no.nav.utsjekk.kontrakter.oppdrag.Utbetalingsoppdrag
import no.nav.utsjekk.kontrakter.oppdrag.Utbetalingsperiode
import no.nav.utsjekk.simulering.domene.Fagområde
import no.nav.utsjekk.simulering.domene.Periode
import no.nav.utsjekk.simulering.domene.PosteringType
import no.nav.utsjekk.simulering.domene.SimuleringDetaljer
import no.nav.utsjekk.simulering.domene.SimulertPostering
import no.nav.utsjekk.simulering.domene.hentFagområdeKoderFor

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

    fun SimuleringResponse.tilSimuleringDetaljer(fagsystem: Fagsystem): SimuleringDetaljer {
        return SimuleringDetaljer(
            gjelderId = this.gjelderId,
            datoBeregnet = this.datoBeregnet,
            totalBeløp = this.totalBelop,
            perioder =
                this.perioder.map { periode ->
                    Periode(
                        fom = periode.fom,
                        tom = periode.tom,
                        posteringer = periode.utbetalinger.fjernAndreYtelser(fagsystem).tilPosteringer(),
                    )
                },
        )
    }

    private fun List<Utbetaling>.fjernAndreYtelser(fagsystem: Fagsystem): List<Utbetaling> {
        return this.filter {
            hentFagområdeKoderFor(fagsystem).map { fagområde -> fagområde.kode }.contains(it.fagområde)
        }
    }

    private fun List<Utbetaling>.tilPosteringer(): List<SimulertPostering> {
        return this.flatMap {
            it.detaljer.map { postering ->
                SimulertPostering(
                    fagområde = Fagområde.fraKode(it.fagområde),
                    sakId = it.fagSystemId,
                    fom = postering.faktiskFom,
                    tom = postering.faktiskTom,
                    beløp = postering.belop,
                    type = PosteringType.fraKode(postering.type),
                )
            }
        }
    }
}
