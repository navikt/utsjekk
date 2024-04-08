package no.nav.utsjekk.utbetaling.utbetalingsoppdrag.domene

import no.nav.utsjekk.kontrakter.felles.BrukersNavKontor
import no.nav.utsjekk.kontrakter.felles.Fagsystem
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
