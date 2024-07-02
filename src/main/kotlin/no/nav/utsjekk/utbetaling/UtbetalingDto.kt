package no.nav.utsjekk.utbetaling

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.utsjekk.felles.http.advice.ApiFeil
import no.nav.utsjekk.kontrakter.felles.GyldigBehandlingId
import no.nav.utsjekk.kontrakter.felles.GyldigSakId
import no.nav.utsjekk.kontrakter.felles.Satstype
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

data class UtbetalingDto(
    val sakId: String,
    val behandlingId: String,
    val personident: String,
    val vedtak: VedtakDto,
) {
    init {
        validerSakId(sakId)
        validerBehandlingId(behandlingId)
    }

    data class VedtakDto(
        val vedtakstidspunkt: LocalDateTime,
        val saksbehandlerId: String,
        val beslutterId: String,
        val utbetalinger: List<SatsDto>,
    ) {
        init {
            if (utbetalinger.overlapper()) {
                throw ApiFeil(
                    "Utbetalingen inneholder perioder som overlapper i tid",
                    HttpStatus.BAD_REQUEST,
                )
            }
        }

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
        @JsonSubTypes(
            JsonSubTypes.Type(value = DagsatsDto::class, name = "DAGLIG"),
            JsonSubTypes.Type(value = MånedsatsDto::class, name = "MÅNEDLIG"),
            JsonSubTypes.Type(value = EngangssatsDto::class, name = "ENGANGS"),
        )
        sealed class SatsDto(
            open val beløp: UInt,
            open val type: Satstype,
            open val stønadstype: String,
            open val brukersNavKontor: String,
        )

        data class DagsatsDto(
            override val beløp: UInt,
            override val stønadstype: String,
            override val brukersNavKontor: String,
            val dato: LocalDate,
        ) : SatsDto(
            beløp = beløp,
            type = Satstype.DAGLIG,
            stønadstype = stønadstype,
            brukersNavKontor = brukersNavKontor
        )

        data class MånedsatsDto(
            override val beløp: UInt,
            override val stønadstype: String,
            override val brukersNavKontor: String,
            val måned: YearMonth,
        ) : SatsDto(
            beløp = beløp,
            type = Satstype.MÅNEDLIG,
            stønadstype = stønadstype,
            brukersNavKontor = brukersNavKontor
        )

        data class EngangssatsDto(
            override val beløp: UInt,
            override val stønadstype: String,
            override val brukersNavKontor: String,
            val fom: LocalDate,
            val tom: LocalDate,
        ) : SatsDto(
            beløp = beløp,
            type = Satstype.ENGANGS,
            stønadstype = stønadstype,
            brukersNavKontor = brukersNavKontor
        ) {
            init {
                if (tom.isBefore(fom)) {
                    throw ApiFeil(
                        "Utbetalingen inneholder periode der tom er før fom",
                        HttpStatus.BAD_REQUEST,
                    )
                }
            }
        }

        private data class Periode(
            val fom: LocalDate,
            val tom: LocalDate,
        )

        private fun List<SatsDto>.overlapper() =
            map {
                when (it) {
                    is DagsatsDto -> Periode(fom = it.dato, tom = it.dato)
                    is MånedsatsDto -> Periode(fom = it.måned.atDay(1), tom = it.måned.atEndOfMonth())
                    is EngangssatsDto -> Periode(fom = it.fom, tom = it.tom)
                }
            }
            .sortedBy { it.fom }
            .windowed(2, 1, false)
            .all { (a, b) -> a.tom.isBefore(b.fom) }
            .not()
    }
}

private fun validerSakId(sakId: String) {
    if (sakId.length !in 1..GyldigSakId.MAKSLENGDE) {
        throw ApiFeil(
            "SakId må være mellom 1 og ${GyldigSakId.MAKSLENGDE} tegn lang",
            HttpStatus.BAD_REQUEST,
        )
    }
}

private fun validerBehandlingId(behandlingId: String) {
    if (behandlingId.length !in 1..GyldigBehandlingId.MAKSLENGDE) {
        throw ApiFeil(
            "BehandlingId må være mellom 1 og ${GyldigBehandlingId.MAKSLENGDE} tegn lang",
            HttpStatus.BAD_REQUEST,
        )
    }
}
