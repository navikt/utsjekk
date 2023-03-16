package no.nav.dagpenger.iverksett.konsumenter

import no.nav.dagpenger.iverksett.konsumenter.arbeidsoppfolging.SendVedtakTilArbeidsoppfølgingTask
import no.nav.dagpenger.iverksett.konsumenter.arena.SendFattetVedtakTilArenaTask
import no.nav.dagpenger.iverksett.konsumenter.brev.DistribuerVedtaksbrevTask
import no.nav.dagpenger.iverksett.konsumenter.brev.JournalførVedtaksbrevTask
import no.nav.dagpenger.iverksett.konsumenter.oppgave.OpprettOppfølgingsOppgaveForOvergangsstønadTask
import no.nav.dagpenger.iverksett.konsumenter.tilbakekreving.OpprettTilbakekrevingTask
import no.nav.dagpenger.iverksett.konsumenter.vedtak.PubliserVedtakTilKafkaTask
import no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk.VedtakstatistikkTask
import no.nav.dagpenger.iverksett.konsumenter.økonomi.IverksettMotOppdragTask
import no.nav.dagpenger.iverksett.konsumenter.økonomi.VentePåStatusFraØkonomiTask
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.Properties

class TaskTypeTest {

    @Test
    fun `test taskflyt`() {
        val opprettTilbakekrevingTask = Task(OpprettTilbakekrevingTask.TYPE, "", Properties())
        assertThat(opprettTilbakekrevingTask.type).isEqualTo(OpprettTilbakekrevingTask.TYPE)
        assertThat(opprettTilbakekrevingTask.triggerTid).isBefore(LocalDateTime.now().plusSeconds(1))

        val iverksettMotOppdragTask = opprettTilbakekrevingTask.opprettNesteTask()
        assertThat(iverksettMotOppdragTask.type).isEqualTo(IverksettMotOppdragTask.TYPE)
        assertThat(iverksettMotOppdragTask.triggerTid).isBefore(LocalDateTime.now().plusSeconds(1))

        val ventePåStatusFraØkonomiTask = iverksettMotOppdragTask.opprettNesteTask()
        assertThat(ventePåStatusFraØkonomiTask.type).isEqualTo(VentePåStatusFraØkonomiTask.TYPE)
        assertThat(ventePåStatusFraØkonomiTask.triggerTid).isAfter(LocalDateTime.now().plusSeconds(2))

        val journalførVedtaksbrevTask = ventePåStatusFraØkonomiTask.opprettNesteTask()
        assertThat(journalførVedtaksbrevTask.type).isEqualTo(JournalførVedtaksbrevTask.TYPE)
        assertThat(journalførVedtaksbrevTask.triggerTid).isBefore(LocalDateTime.now().plusSeconds(1))

        val distribuerVedtaksbrevTask = journalførVedtaksbrevTask.opprettNesteTask()
        assertThat(distribuerVedtaksbrevTask.type).isEqualTo(DistribuerVedtaksbrevTask.TYPE)
        assertThat(distribuerVedtaksbrevTask.triggerTid).isBefore(LocalDateTime.now().plusSeconds(1))
    }

    @Test
    fun `test publiseringTaskflyt`() {
        val sendFattetVedtakTilArenaTask = Task(SendFattetVedtakTilArenaTask.TYPE, "", Properties())
        assertThat(sendFattetVedtakTilArenaTask.type).isEqualTo(SendFattetVedtakTilArenaTask.TYPE)
        assertThat(sendFattetVedtakTilArenaTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))

        val publiserVedtakTilKafkaTask = sendFattetVedtakTilArenaTask.opprettNestePubliseringTask()
        assertThat(publiserVedtakTilKafkaTask.type).isEqualTo(PubliserVedtakTilKafkaTask.TYPE)
        assertThat(publiserVedtakTilKafkaTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))

        val sendVedtakTilArbeidsoppfølgingTask = publiserVedtakTilKafkaTask.opprettNestePubliseringTask()
        assertThat(sendVedtakTilArbeidsoppfølgingTask.type).isEqualTo(SendVedtakTilArbeidsoppfølgingTask.TYPE)
        assertThat(sendVedtakTilArbeidsoppfølgingTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))

        val opprettOppgaveTask = sendVedtakTilArbeidsoppfølgingTask.opprettNestePubliseringTask()
        assertThat(opprettOppgaveTask.type).isEqualTo(OpprettOppfølgingsOppgaveForOvergangsstønadTask.TYPE)
        assertThat(opprettOppgaveTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))

        val vedtaksstatistikkTask = opprettOppgaveTask.opprettNestePubliseringTask()
        assertThat(vedtaksstatistikkTask.type).isEqualTo(VedtakstatistikkTask.TYPE)
        assertThat(vedtaksstatistikkTask.triggerTid).isBefore(LocalDateTime.now().plusMinutes(1))
    }

    @Test
    internal fun `skal ikke opprette task etter opprettet DistribuerVedtaksbrevTask`() {
        val task = Task(DistribuerVedtaksbrevTask.TYPE, "", Properties())
        assertThrows<NoSuchElementException> {
            task.opprettNesteTask()
        }
    }
}
