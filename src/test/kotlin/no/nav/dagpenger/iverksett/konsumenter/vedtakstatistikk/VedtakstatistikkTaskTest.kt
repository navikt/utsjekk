package no.nav.dagpenger.iverksett.konsumenter.vedtakstatistikk

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.iverksett.api.IverksettingRepository
import no.nav.dagpenger.iverksett.api.tilstand.IverksettResultatService
import no.nav.dagpenger.iverksett.infrastruktur.repository.findByIdOrThrow
import no.nav.dagpenger.iverksett.infrastruktur.transformer.toDomain
import no.nav.dagpenger.iverksett.lagIverksett
import no.nav.dagpenger.iverksett.util.opprettIverksettDto
import no.nav.familie.prosessering.domene.Task
import org.junit.jupiter.api.Test
import java.util.Properties
import java.util.UUID

internal class VedtakstatistikkTaskTest {

    val iverksettingRepository = mockk<IverksettingRepository>()
    private val vedtakstatistikkService = mockk<VedtakstatistikkService>()
    val iverksettResultatService = mockk<IverksettResultatService>()
    private val vedtakstatistikkTask =
        VedtakstatistikkTask(iverksettingRepository, vedtakstatistikkService)
    val behandlingId: UUID = UUID.randomUUID()

    @Test
    fun `skal sende vedtaksstatistikk til DVH`() {
        val behandlingIdString = behandlingId.toString()
        val returnIverksetting = lagIverksett(opprettIverksettDto(behandlingId = behandlingId).toDomain())

        every { vedtakstatistikkService.sendTilKafka(returnIverksetting.data, null) } just Runs
        every { iverksettingRepository.findByIdOrThrow(behandlingId) }
            .returns(returnIverksetting)

        vedtakstatistikkTask.doTask(Task(VedtakstatistikkTask.TYPE, behandlingIdString, Properties()))
        verify(exactly = 1) { vedtakstatistikkService.sendTilKafka(returnIverksetting.data, null) }
    }
}