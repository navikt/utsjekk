package no.nav.dagpenger.iverksett.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import no.nav.dagpenger.kontrakter.felles.StønadTypeDagpenger
import no.nav.dagpenger.kontrakter.iverksett.AvslagÅrsak
import no.nav.dagpenger.kontrakter.iverksett.BehandlingMetode
import no.nav.dagpenger.kontrakter.iverksett.BehandlingType
import no.nav.dagpenger.kontrakter.iverksett.BehandlingsstatistikkDto
import no.nav.dagpenger.kontrakter.iverksett.BehandlingÅrsak
import no.nav.dagpenger.kontrakter.iverksett.Hendelse
import no.nav.dagpenger.kontrakter.iverksett.Opplysningskilde
import no.nav.dagpenger.kontrakter.iverksett.Revurderingsårsak
import no.nav.dagpenger.kontrakter.iverksett.ÅrsakRevurderingDto

fun opprettBehandlingsstatistikkDto(behandlingId: UUID, hendelse: Hendelse, fortrolig: Boolean): BehandlingsstatistikkDto {
    return BehandlingsstatistikkDto(
        behandlingId = behandlingId,
        eksternBehandlingId = 654L,
        personIdent = "aktor",
        gjeldendeSaksbehandlerId = "saksbehandler",
        beslutterId = "beslutterId",
        eksternFagsakId = 123L,
        hendelseTidspunkt = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("UTC")),
        hendelse = hendelse,
        behandlingResultat = "",
        resultatBegrunnelse = "",
        ansvarligEnhet = "ansvarligEnhet",
        opprettetEnhet = "opprettetEnhet",
        strengtFortroligAdresse = fortrolig,
        stønadstype = StønadTypeDagpenger.DAGPENGER_ARBEIDSSOKER_ORDINAER,
        behandlingstype = BehandlingType.FØRSTEGANGSBEHANDLING,
        relatertBehandlingId = UUID.randomUUID(),
        relatertEksternBehandlingId = null,
        behandlingMetode = BehandlingMetode.MANUELL,
        behandlingÅrsak = BehandlingÅrsak.SØKNAD,
        kravMottatt = LocalDate.of(2021, 3, 1),
        årsakRevurdering = ÅrsakRevurderingDto(Opplysningskilde.MELDING_MODIA, Revurderingsårsak.ENDRING_INNTEKT),
        avslagÅrsak = AvslagÅrsak.BARN_OVER_ÅTTE_ÅR,
    )
}
