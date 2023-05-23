package no.nav.dagpenger.iverksett.kontrakter.tilbakekreving

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.dagpenger.iverksett.kontrakter.felles.Språkkode
import no.nav.dagpenger.kontrakter.utbetaling.Fagsystem
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class ForhåndsvisVarselbrevRequest(
    val varseltekst: String? = null,
    val ytelsestype: Ytelsestype,
    val behandlendeEnhetId: String? = null,
    val behandlendeEnhetsNavn: String,
    val saksbehandlerIdent: String? = null,
    val språkkode: Språkkode = Språkkode.NB,
    val vedtaksdato: LocalDate? = null,
    val feilutbetaltePerioderDto: FeilutbetaltePerioderDto,
    val fagsystem: Fagsystem,
    val eksternFagsakId: String,
    val ident: String,
    val verge: Verge? = null,
    val fagsystemsbehandlingId: String? = null,
    val institusjon: Institusjon? = null,
)

data class FeilutbetaltePerioderDto(
    val sumFeilutbetaling: Long,
    val perioder: List<Periode>,
)
