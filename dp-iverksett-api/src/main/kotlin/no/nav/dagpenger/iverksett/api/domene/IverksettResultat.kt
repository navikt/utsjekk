package no.nav.dagpenger.iverksett.api.domene

import no.nav.dagpenger.iverksett.konsumenter.brev.domain.DistribuerBrevResultatMap
import no.nav.dagpenger.iverksett.konsumenter.brev.domain.JournalpostResultatMap
import no.nav.dagpenger.kontrakter.iverksett.oppdrag.OppdragStatus
import no.nav.dagpenger.kontrakter.iverksett.tilbakekreving.OpprettTilbakekrevingRequest
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.UUID

data class IverksettResultat(
    @Id
    val behandlingId: UUID,
    @Column("tilkjentytelseforutbetaling")
    val tilkjentYtelseForUtbetaling: TilkjentYtelse? = null,
    @Column("oppdragresultat")
    val oppdragResultat: OppdragResultat? = null,
    @Column("journalpostresultat")
    val journalpostResultat: JournalpostResultatMap = JournalpostResultatMap(),
    @Column("vedtaksbrevresultat")
    val vedtaksbrevResultat: DistribuerBrevResultatMap = DistribuerBrevResultatMap(),
    @Column("tilbakekrevingresultat")
    val tilbakekrevingResultat: TilbakekrevingResultat? = null,
)

data class OppdragResultat(val oppdragStatus: OppdragStatus, val oppdragStatusOppdatert: LocalDateTime = LocalDateTime.now())

data class TilbakekrevingResultat(
    val opprettTilbakekrevingRequest: OpprettTilbakekrevingRequest,
    val tilbakekrevingOppdatert: LocalDateTime = LocalDateTime.now(),
)
