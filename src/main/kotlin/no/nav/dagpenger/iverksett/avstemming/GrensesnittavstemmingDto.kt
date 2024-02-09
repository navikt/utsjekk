package no.nav.dagpenger.iverksett.avstemming

import no.nav.dagpenger.kontrakter.felles.Fagsystem
import no.nav.dagpenger.kontrakter.felles.objectMapper
import no.nav.familie.prosessering.domene.Task
import java.time.LocalDate
import java.time.LocalDateTime

data class GrensesnittavstemmingDto(val fagsystem: Fagsystem, val fraDato: LocalDate, val triggerTid: LocalDateTime? = null)

fun GrensesnittavstemmingDto.tilTask(): Task {
    val nesteVirkedag: LocalDateTime = triggerTid ?: VirkedagerProvider.nesteVirkedag(fraDato).atTime(8, 0)
    val payload =
        objectMapper.writeValueAsString(
            GrensesnittavstemmingPayload(
                fraDato = this.fraDato,
                fagsystem = this.fagsystem,
            ),
        )

    return Task(
        type = GrensesnittavstemmingTask.TYPE,
        payload = payload,
        triggerTid = nesteVirkedag,
    )
}
