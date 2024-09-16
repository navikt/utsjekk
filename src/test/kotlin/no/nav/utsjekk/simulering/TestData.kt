package no.nav.utsjekk.simulering

import no.nav.utsjekk.iverksetting.util.enUtbetalingV2Dto
import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataDto
import no.nav.utsjekk.simulering.api.SimuleringRequestV2Dto
import no.nav.utsjekk.simulering.client.dto.PosteringDto
import no.nav.utsjekk.simulering.client.dto.SimuleringResponse
import no.nav.utsjekk.simulering.client.dto.SimulertPeriode
import no.nav.utsjekk.simulering.client.dto.Utbetaling
import java.time.LocalDate

fun enSimulertPeriode(
    fom: LocalDate = LocalDate.of(2024, 5, 1),
    tom: LocalDate = LocalDate.of(2024, 5, 1),
    utbetalesTilId: String = "15507600333",
) = SimulertPeriode(
    fom = fom,
    tom = tom,
    utbetalinger =
        listOf(
            Utbetaling(
                fagområde = "et-fagområde",
                fagSystemId = "en-sakid",
                utbetalesTilId = utbetalesTilId,
                forfall = tom.plusMonths(1),
                feilkonto = false,
                detaljer =
                    listOf(
                        PosteringDto(
                            type = "YTEL",
                            faktiskFom = fom,
                            faktiskTom = tom,
                            belop = 800,
                            sats = 800.0,
                            satstype = "DAG",
                            klassekode = "en-klassekode",
                            trekkVedtakId = null,
                            refunderesOrgNr = null,
                        ),
                    ),
            ),
        ),
)

fun enSimuleringResponse(
    gjelderId: String = "15507600333",
    datoBeregnet: LocalDate = LocalDate.of(2024, 5, 28),
    perioder: List<SimulertPeriode> = listOf(enSimulertPeriode(utbetalesTilId = gjelderId)),
) = SimuleringResponse(
    gjelderId = gjelderId,
    datoBeregnet = datoBeregnet,
    totalBelop = 800,
    perioder = perioder,
)

fun enSimuleringRequestV2Dto(stønadsdataDto: StønadsdataDto) =
    SimuleringRequestV2Dto(
        sakId = "en-sakid",
        behandlingId = "en-behandlingId",
        personident = Personident("15507600333"),
        saksbehandlerId = "A123456",
        utbetalinger =
            listOf(
                enUtbetalingV2Dto(
                    beløp = 100u,
                    fraOgMed = LocalDate.of(2020, 1, 1),
                    tilOgMed = LocalDate.of(2020, 1, 31),
                    stønadsdata = stønadsdataDto,
                ),
            ),
    )
