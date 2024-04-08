package no.nav.utsjekk.avstemming

import no.nav.familie.prosessering.domene.Task
import no.nav.utsjekk.kontrakter.felles.Fagsystem
import no.nav.utsjekk.kontrakter.felles.objectMapper
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
