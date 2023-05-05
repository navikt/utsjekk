package no.nav.dagpenger.iverksett.konsumenter.arena

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import no.nav.dagpenger.iverksett.util.opprettIverksettDagpenger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.jms.core.JmsTemplate
import java.time.format.DateTimeFormatter
import java.util.UUID

class VedtakhendelseProducerTest {

    private var jmsTemplate: JmsTemplate = mockk(relaxed = true)
    private val vedtakhendelseProducer = VedtakhendelseProducer(jmsTemplate)

    @Test
    fun testVedtakhendelseProducer() {
        val vedtakHendelseXmlSlot = slot<String>()
        every { jmsTemplate.convertAndSend(capture(vedtakHendelseXmlSlot)) } just Runs

        val fagsakId = UUID.randomUUID()
        val iverksett = opprettIverksettDagpenger(fagsakId = fagsakId)
        val vedtakHendelser = mapIverksettTilVedtakHendelser(iverksett, "a123")
        vedtakhendelseProducer.produce(vedtakHendelser)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val forventetXML = forventetXML(
            fagsakId = fagsakId,
            hendelsesTidspunkt = vedtakHendelser.hendelsesTidspunkt.format(formatter),
        )
        assertThat(vedtakHendelseXmlSlot.captured).isEqualTo(forventetXML)
    }
}

private fun forventetXML(fagsakId: UUID, hendelsesTidspunkt: String): String {
    return """
        <vedtakHendelser xmlns="http://nav.no/melding/virksomhet/vedtakHendelser/v1/vedtakHendelser">
            <aktoerID xmlns="">a123</aktoerID>
            <avslutningsstatus xmlns="">innvilget</avslutningsstatus>
            <behandlingstema xmlns="">abXXXX</behandlingstema>
            <hendelsesprodusentREF xmlns="">DP</hendelsesprodusentREF>
            <applikasjonSakREF xmlns="">$fagsakId</applikasjonSakREF>
            <hendelsesTidspunkt xmlns="">$hendelsesTidspunkt</hendelsesTidspunkt>
        </vedtakHendelser>
    """.trim()
        .replace("\n", "")
        .replace("""> *<""".toRegex(), "><")
}
