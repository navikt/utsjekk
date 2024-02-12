package no.nav.dagpenger.iverksett.utbetaling.utbetalingsoppdrag.domene

import no.nav.dagpenger.kontrakter.felles.BrukersNavKontor
import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.GeneriskId
import java.time.LocalDate

data class Behandlingsinformasjon(
    val saksbehandlerId: String,
    val fagsakId: GeneriskId,
    val fagsystem: Fagsystem,
    val behandlingId: GeneriskId,
    val personident: String,
    val vedtaksdato: LocalDate,
    val brukersNavKontor: BrukersNavKontor? = null,
)
