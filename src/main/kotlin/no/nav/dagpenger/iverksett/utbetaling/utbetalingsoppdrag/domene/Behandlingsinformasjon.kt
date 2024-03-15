package no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.domene

import no.nav.dagpenger.kontrakter.felles.BrukersNavKontor
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import java.time.LocalDate

data class Behandlingsinformasjon(
    val saksbehandlerId: String,
    val beslutterId: String,
    val fagsakId: String,
    val fagsystem: Fagsystem,
    val behandlingId: String,
    val personident: String,
    val vedtaksdato: LocalDate,
    val brukersNavKontor: BrukersNavKontor? = null,
    val iverksettingId: String?,
)
